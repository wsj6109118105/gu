package com.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.exception.NoStockException;
import com.common.to.mq.OrderTo;
import com.common.to.mq.SeckillOrderTo;
import com.common.utils.R;
import com.common.vo.MemberResponseVo;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.order.constant.OrderConstant;
import com.order.dao.OrderItemDao;
import com.order.entity.OrderItemEntity;
import com.order.entity.PaymentInfoEntity;
import com.order.enume.OrderStatusEnum;
import com.order.feign.CartFeignService;
import com.order.feign.MemberFeignService;
import com.order.feign.ProductFeignService;
import com.order.feign.WmsFeignService;
import com.order.interceptor.LoginUser;
import com.order.service.OrderItemService;
import com.order.service.PaymentInfoService;
import com.order.to.OrderCreateTo;
import com.order.vo.*;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.utils.PageUtils;
import com.common.utils.Query;

import com.order.dao.OrderDao;
import com.order.entity.OrderEntity;
import com.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {


    private ThreadLocal<OrderSubmitVo> submitVoThreadLocal = new ThreadLocal<>();

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    WmsFeignService wmsFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    PaymentInfoService paymentInfoService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * ????????????????????????
     * @return ???????????????????????????????????????
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberResponseVo memberResponseVo = LoginUser.loginUser.get();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        // 1. ???????????????????????????????????????
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddress> address = memberFeignService.getAddress(memberResponseVo.getId());
            confirmVo.setAddresses(address);
        }, executor);

        // 2. ?????????????????????????????????????????????
        CompletableFuture<Void> cartItemsFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> cartItems = cartFeignService.getCartItems();
            confirmVo.setItems(cartItems);
        }, executor).thenRunAsync(()->{
            List<OrderItemVo> items = confirmVo.getItems();
            List<Long> collect = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            R hasStock = wmsFeignService.getHasStock(collect);
            List<SkuStockVo> data = hasStock.getData(new TypeReference<List<SkuStockVo>>() {
            });
            if (data!=null) {
                Map<Long, Boolean> map = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                confirmVo.setStocks(map);
            }
        },executor);

        // 3. ??????????????????
        Integer integration = memberResponseVo.getIntegration();
        confirmVo.setIntegration(integration);


        //todo 4. ????????????
        String token = UUID.randomUUID().toString().replace("-", "");
        confirmVo.setOrderToken(token);
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX+memberResponseVo.getId(),token,30, TimeUnit.MINUTES);
        CompletableFuture.allOf(addressFuture,cartItemsFuture).get();
        return confirmVo;
    }

    /**
     * ????????????  ??????????????????????????????????????????
     * @param submitVo ???????????????
     * @return ?????????????????????????????????
     */
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo submitVo) {
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        MemberResponseVo memberResponseVo = LoginUser.loginUser.get();
        submitVoThreadLocal.set(submitVo);
        responseVo.setCode(0);
        // ??????????????????????????????????????????
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        String orderToken = submitVo.getOrderToken();
        // ????????????
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), List.of(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId()), orderToken);
        if (result==1) {
            // ??????????????????
            OrderCreateTo order = createOrder();
            BigDecimal payPrice = order.getPayPrice();
            BigDecimal subPayPrice = submitVo.getPayPrice();
            if (Math.abs(payPrice.subtract(subPayPrice).doubleValue())<0.01) {
                // ??????????????????,????????????
                saveOrder(order);
                //todo ????????????, ?????????????????????????????????
                // 1) ???????????????????????????(skuId,num,skuName)
                WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
                wareSkuLockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> collect = order.getOrderItems().stream().map(item -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setTitle(item.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList());
                wareSkuLockVo.setLocks(collect);
                // todo ???????????????
                R r = wmsFeignService.orderLockStock(wareSkuLockVo);
                if (r.getCode()==0) {
                    responseVo.setOrderEntity(order.getOrder());
                    rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",order.getOrder());
                    return responseVo;
                }else {
                    responseVo.setCode(3);
                    return responseVo;
                }
            }else {
                // ????????????
                responseVo.setCode(2);
                return responseVo;
            }
        }else {
            responseVo.setCode(1);
        }
        return responseVo;
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        return this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
    }

    /**
     * ??????????????????
     * @param entity ?????????????????????
     */
    @Override
    public void closeOrder(OrderEntity entity) {
        // ????????????????????????????????????????????????
        OrderEntity orderEntity = this.getById(entity.getId());
        if (Objects.equals(orderEntity.getStatus(), OrderStatusEnum.CREATE_NEW.getCode())) {
            // ??????
            orderEntity.setStatus(OrderStatusEnum.CANCLED.getCode());
            orderEntity.setId(orderEntity.getId());
            this.updateById(orderEntity);
            // ????????? MQ ??????????????????
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity,orderTo);
            try {
                // todo ????????????????????????,?????????????????????????????????,(????????????????????????????????????????????????),??????????????????????????????????????????????????????
                rabbitTemplate.convertAndSend("order-event-exchange","order.release.other",orderTo);
            }catch (Exception e) {
                // todo ?????????????????????????????????????????????
            }
        }
    }

    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo = new PayVo();
        OrderEntity orderByOrderSn = this.getOrderByOrderSn(orderSn);
        List<OrderItemEntity> order_sn = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        OrderItemEntity orderItemEntity = order_sn.get(0);

        payVo.setTotal_amount(orderByOrderSn.getPayAmount().setScale(2, RoundingMode.HALF_UP).toString());
        payVo.setOut_trade_no(orderSn);
        payVo.setSubject(orderItemEntity.getSkuName());
        payVo.setBody(orderItemEntity.getSkuAttrsVals());
        return payVo;
    }

    /**
     * ???????????????????????????????????????
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        MemberResponseVo memberResponseVo = LoginUser.loginUser.get();
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().eq("member_id",memberResponseVo.getId()).orderByDesc("id")
        );
        List<OrderEntity> orderSn = page.getRecords().stream().map(order -> {
            List<OrderItemEntity> order_sn = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", order.getOrderSn()));
            order.setItemEntities(order_sn);
            return order;
        }).collect(Collectors.toList());
        page.setRecords(orderSn);
        return new PageUtils(page);
    }

    /**
     * ??????????????????????????????
     * @param vo ??????????????????????????????
     * @return
     */
    @Override
    public String handlePayResult(PayAsyncVo vo) {
        // ??????????????????
        PaymentInfoEntity paymentInfoEntity = new PaymentInfoEntity();
        paymentInfoEntity.setAlipayTradeNo(vo.getTrade_no());
        paymentInfoEntity.setOrderSn(vo.getOut_trade_no());
        paymentInfoEntity.setPaymentStatus(vo.getTrade_status());
        paymentInfoEntity.setCallbackTime(vo.getNotify_time());
        paymentInfoService.save(paymentInfoEntity);
        // ???????????????????????????
        boolean b = vo.getTrade_status().equals("TRADE_SUCCESS")||vo.getTrade_status().equals("TRADE_FINISHED");
        if (b) {
            String orderSn = vo.getOut_trade_no();
            this.baseMapper.updateOrderStatus(orderSn,OrderStatusEnum.PAYED.getCode());
        }
        return null;
    }

    /**
     * ??????????????????
     * @param seckillOrderTo ??????????????????
     */
    @Override
    public void createSeckillOrder(SeckillOrderTo seckillOrderTo) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(seckillOrderTo.getOrderSn());
        orderEntity.setMemberId(seckillOrderTo.getMemberId());
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        BigDecimal multiply = seckillOrderTo.getSeckillPrice().multiply(new BigDecimal(seckillOrderTo.getNum()));
        orderEntity.setPayAmount(multiply);
        this.save(orderEntity);
        // todo ???????????????
        OrderItemEntity itemEntity = new OrderItemEntity();
        itemEntity.setOrderSn(seckillOrderTo.getOrderSn());
        itemEntity.setRealAmount(multiply);
        itemEntity.setSkuQuantity(seckillOrderTo.getNum());
        orderItemService.save(itemEntity);
    }

    /**
     * ????????????,?????????????????????
     * @param order ????????????
     */
    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);
        orderItemService.saveBatch(orderItems);
    }

    /**
     * ????????????
     * @return ????????????????????????
     */
    public OrderCreateTo createOrder() {
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        // ???????????????
        String orderSn = IdWorker.getTimeId();
        // ???????????????,?????????????????????
        OrderEntity orderEntity = buildOrder(orderSn);

        // ?????????????????????
        List<OrderItemEntity> orderItemEntity = buildOrderItems(orderSn);
        // ??????????????????
        if (orderItemEntity!=null) {
            computePrice(orderEntity,orderItemEntity);
        }
        orderCreateTo.setOrder(orderEntity);
        orderCreateTo.setOrderItems(orderItemEntity);
        orderCreateTo.setFare(orderEntity.getFreightAmount());
        orderCreateTo.setPayPrice(orderEntity.getPayAmount());
        return orderCreateTo;
    }

    /**
     * ??????????????????,????????????,?????????,??????
     * @param orderEntity
     * @param orderItemEntity
     */
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntity) {
        // ??????????????????
        BigDecimal total = new BigDecimal(0);

        // ????????????????????????
        BigDecimal reduceCoupon = new BigDecimal(0);
        BigDecimal reducePromotion = new BigDecimal(0);
        BigDecimal reduceIntegration = new BigDecimal(0);

        // ??????????????????????????????
        Integer GiftGrowth = 0;
        Integer GiftIntegration = 0;

        for (OrderItemEntity itemEntity : orderItemEntity) {
            total = total.add(itemEntity.getRealAmount());
            reducePromotion = reducePromotion.add(itemEntity.getPromotionAmount());
            reduceCoupon = reduceCoupon.add(itemEntity.getCouponAmount());
            reduceIntegration = reduceIntegration.add(itemEntity.getIntegrationAmount());
            GiftGrowth+=itemEntity.getGiftGrowth();
            GiftIntegration+=itemEntity.getGiftIntegration();
        }
        // ????????????
        orderEntity.setTotalAmount(total);
        // ??????????????????
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        // ??????????????????????????????
        orderEntity.setPromotionAmount(reducePromotion);
        orderEntity.setCouponAmount(reduceCoupon);
        orderEntity.setIntegrationAmount(reduceIntegration);
        // ??????????????????????????????
        orderEntity.setGrowth(GiftGrowth);
        orderEntity.setIntegration(GiftIntegration);
        orderEntity.setDeleteStatus(0);
    }

    /**
     * ???????????????????????????????????????
     * @param orderSn  ?????????
     */
    private OrderEntity buildOrder(String orderSn) {
        MemberResponseVo memberResponseVo = LoginUser.loginUser.get();
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSn);
        OrderSubmitVo orderSubmitVo = submitVoThreadLocal.get();
        orderEntity.setMemberId(memberResponseVo.getId());
        orderEntity.setMemberUsername(memberResponseVo.getUsername());
        // ????????????????????????
        R fare = wmsFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fareVo = fare.getData(new TypeReference<FareVo>() {});
        orderEntity.setFreightAmount(fareVo.getFare());   // ????????????

        orderEntity.setReceiverCity(fareVo.getAddress().getCity());
        orderEntity.setReceiverDetailAddress(fareVo.getAddress().getDetailAddress());
        orderEntity.setReceiverName(fareVo.getAddress().getName());
        orderEntity.setReceiverPhone(fareVo.getAddress().getPhone());
        orderEntity.setReceiverProvince(fareVo.getAddress().getProvince());
        orderEntity.setReceiverPostCode(fareVo.getAddress().getPostCode());
        orderEntity.setReceiverRegion(fareVo.getAddress().getRegion());

        // ??????????????????
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setAutoConfirmDay(7);

        return orderEntity;
    }

    /**
     * ???????????????????????????
     * @return ????????????????????????????????????????????????????????????????????????
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVo> cartItems = cartFeignService.getCartItems();
        if (cartItems!=null&&cartItems.size()>0) {
            List<OrderItemEntity> collect = cartItems.stream().map(item -> {
                OrderItemEntity orderItemEntity = buildOrderItem(item);
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    /**
     * ?????????????????????
     * @param item ????????????????????????
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo item) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        // ????????? spu ??????
        Long skuId = item.getSkuId();
        R spuInfo = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo data = spuInfo.getData(new TypeReference<SpuInfoVo>() {});
        orderItemEntity.setSpuId(data.getId());
        orderItemEntity.setSpuName(data.getSpuName());
        orderItemEntity.setSpuBrand(data.getBrandId().toString());
        orderItemEntity.setCategoryId(data.getCatalogId());
        // ????????? sku ??????
        orderItemEntity.setSkuId(item.getSkuId());
        orderItemEntity.setSkuName(item.getTitle());
        orderItemEntity.setSkuPic(item.getImg());
        orderItemEntity.setSkuPrice(item.getPrice());
        String skuAttr = StringUtils.collectionToDelimitedString(item.getSkuAttr(), ",");
        orderItemEntity.setSkuAttrsVals(skuAttr);
        orderItemEntity.setSkuQuantity(item.getCount());

        orderItemEntity.setIntegrationAmount(new BigDecimal(0));
        orderItemEntity.setPromotionAmount(new BigDecimal(0));
        orderItemEntity.setCouponAmount(new BigDecimal(0));
        orderItemEntity.setRealAmount(item.getPrice()
                .multiply(new BigDecimal(item.getCount()))
                .subtract(orderItemEntity.getIntegrationAmount()
                .subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getPromotionAmount())));

        // ????????????
        orderItemEntity.setGiftGrowth(item.getPrice().multiply(new BigDecimal(item.getCount())).intValue());
        orderItemEntity.setGiftIntegration(item.getPrice().multiply(new BigDecimal(item.getCount())).intValue());


        return orderItemEntity;
    }
}
