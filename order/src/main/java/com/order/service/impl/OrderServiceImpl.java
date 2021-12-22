package com.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.common.utils.R;
import com.common.vo.MemberResponseVo;
import com.order.constant.OrderConstant;
import com.order.feign.CartFeignService;
import com.order.feign.MemberFeignService;
import com.order.feign.WmsFeignService;
import com.order.interceptor.LoginUser;
import com.order.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {


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
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo submitVo) {
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        MemberResponseVo memberResponseVo = LoginUser.loginUser.get();
        // 令牌的对比和删除要保证原子性
        String script = "if redis.call('get',KEYS[1]==ARGV[1]) then return redis.call('del',KEYS[1]) else return 0 end";
        String orderToken = submitVo.getOrderToken();
        // 原子操作
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId()), orderToken);
        if (result==1) {
            // 令牌验证成功
        }
//        String redisToken = redisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId());
//        if (orderToken!=null && orderToken.equals(redisToken)) {
//            // 令牌验证通过
//            redisTemplate.delete(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId());
//        }else {
//
//        }
        return responseVo;
    }

}
