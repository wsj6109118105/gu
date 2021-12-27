package com.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.exception.NoStockException;
import com.common.utils.R;
import com.common.vo.MemberResponseVo;
import com.order.constant.OrderConstant;
import com.order.dao.OrderItemDao;
import com.order.entity.OrderItemEntity;
import com.order.enume.OrderStatusEnum;
import com.order.feign.CartFeignService;
import com.order.feign.MemberFeignService;
import com.order.feign.ProductFeignService;
import com.order.feign.WmsFeignService;
import com.order.interceptor.LoginUser;
import com.order.service.OrderItemService;
import com.order.to.OrderCreateTo;
import com.order.vo.*;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 订单页面信息生成
     * @return 返回封装好的订单页面的信息
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberResponseVo memberResponseVo = LoginUser.loginUser.get();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        // 1. 远程查询所有的收货地址列表
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddress> address = memberFeignService.getAddress(memberResponseVo.getId());
            confirmVo.setAddresses(address);
        }, executor);

        // 2. 远程查询购物车所有选中的购物项
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

        // 3. 查询用户积分
        Integer integration = memberResponseVo.getIntegration();
        confirmVo.setIntegration(integration);


        //todo 4. 防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        confirmVo.setOrderToken(token);
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX+memberResponseVo.getId(),token,30, TimeUnit.MINUTES);
        CompletableFuture.allOf(addressFuture,cartItemsFuture).get();
        return confirmVo;
    }

    /**
     * 下单功能  创建订单，验证令牌，验证价格
     * @param submitVo 订单的数据
     * @return 返回支付页面需要的信息
     */
    @GlobalTransactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo submitVo) {
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        MemberResponseVo memberResponseVo = LoginUser.loginUser.get();
        submitVoThreadLocal.set(submitVo);
        responseVo.setCode(0);
        // 令牌的对比和删除要保证原子性
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        String orderToken = submitVo.getOrderToken();
        // 原子操作
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), List.of(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId()), orderToken);
        if (result==1) {
            // 令牌验证成功
            OrderCreateTo order = createOrder();
            BigDecimal payPrice = order.getPayPrice();
            BigDecimal subPayPrice = submitVo.getPayPrice();
            if (Math.abs(payPrice.subtract(subPayPrice).doubleValue())<0.01) {
                // 金额对比成功,保存订单
                saveOrder(order);
                //todo 库存锁定, 只要有异常回滚订单数据
                // 1) 订单号，所有订单项(skuId,num,skuName)
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
                // todo 远程锁库存
                R r = wmsFeignService.orderLockStock(wareSkuLockVo);
                if (r.getCode()==0) {
                    responseVo.setOrderEntity(order.getOrder());
                    int i = 10/0;
                    return responseVo;
                }else {
                    responseVo.setCode(3);
                    return responseVo;
                }
            }else {
                // 对比失败
                responseVo.setCode(2);
                return responseVo;
            }
        }else {
            responseVo.setCode(1);
        }
        return responseVo;
    }

    /**
     * 保存订单
     * @param order 订单信息
     */
    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);
        orderItemService.saveBatch(orderItems);
    }

    /**
     * 创建订单
     * @return 返回一个订单信息
     */
    public OrderCreateTo createOrder() {
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        // 生成订单号
        String orderSn = IdWorker.getTimeId();
        // 设置订单号,设置收货人信息
        OrderEntity orderEntity = buildOrder(orderSn);

        // 创建所有订单项
        List<OrderItemEntity> orderItemEntity = buildOrderItems(orderSn);
        // 计算价格相关
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
     * 计算价格相关,优惠信息,成长值,积分
     * @param orderEntity
     * @param orderItemEntity
     */
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntity) {
        // 计算商品总价
        BigDecimal total = new BigDecimal(0);

        // 计算总的优惠信息
        BigDecimal reduceCoupon = new BigDecimal(0);
        BigDecimal reducePromotion = new BigDecimal(0);
        BigDecimal reduceIntegration = new BigDecimal(0);

        // 计算总的积分和成长值
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
        // 设置总额
        orderEntity.setTotalAmount(total);
        // 设置应付总额
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        // 设置总的各种优惠信息
        orderEntity.setPromotionAmount(reducePromotion);
        orderEntity.setCouponAmount(reduceCoupon);
        orderEntity.setIntegrationAmount(reduceIntegration);
        // 设置总的积分和成长值
        orderEntity.setGrowth(GiftGrowth);
        orderEntity.setIntegration(GiftIntegration);
        orderEntity.setDeleteStatus(0);
    }

    /**
     * 按照订单号设置收货人的信息
     * @param orderSn  订单号
     */
    private OrderEntity buildOrder(String orderSn) {
        MemberResponseVo memberResponseVo = LoginUser.loginUser.get();
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSn);
        OrderSubmitVo orderSubmitVo = submitVoThreadLocal.get();
        orderEntity.setMemberId(memberResponseVo.getId());
        orderEntity.setMemberUsername(memberResponseVo.getUsername());
        // 远程获取地址信息
        R fare = wmsFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fareVo = fare.getData(new TypeReference<FareVo>() {});
        orderEntity.setFreightAmount(fareVo.getFare());   // 设置运费

        orderEntity.setReceiverCity(fareVo.getAddress().getCity());
        orderEntity.setReceiverDetailAddress(fareVo.getAddress().getDetailAddress());
        orderEntity.setReceiverName(fareVo.getAddress().getName());
        orderEntity.setReceiverPhone(fareVo.getAddress().getPhone());
        orderEntity.setReceiverProvince(fareVo.getAddress().getProvince());
        orderEntity.setReceiverPostCode(fareVo.getAddress().getPostCode());
        orderEntity.setReceiverRegion(fareVo.getAddress().getRegion());

        // 设置订单状态
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setAutoConfirmDay(7);

        return orderEntity;
    }

    /**
     * 构建所有订单项数据
     * @return 返回将购物车中的订单项构建成与数据库一致的订单项
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
     * 构建一个订单项
     * @param item 每一个订单的数据
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo item) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        // 商品的 spu 信息
        Long skuId = item.getSkuId();
        R spuInfo = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo data = spuInfo.getData(new TypeReference<SpuInfoVo>() {});
        orderItemEntity.setSpuId(data.getId());
        orderItemEntity.setSpuName(data.getSpuName());
        orderItemEntity.setSpuBrand(data.getBrandId().toString());
        orderItemEntity.setCategoryId(data.getCatalogId());
        // 商品的 sku 信息
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

        // 成长积分
        orderItemEntity.setGiftGrowth(item.getPrice().multiply(new BigDecimal(item.getCount())).intValue());
        orderItemEntity.setGiftIntegration(item.getPrice().multiply(new BigDecimal(item.getCount())).intValue());


        return orderItemEntity;
    }
}
