package com.order.service.impl;

import com.common.vo.MemberResponseVo;
import com.order.feign.CartFeignService;
import com.order.feign.MemberFeignService;
import com.order.interceptor.LoginUser;
import com.order.vo.MemberAddress;
import com.order.vo.OrderConfirmVo;
import com.order.vo.OrderItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

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

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

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
        }, executor);

        // 3. 查询用户积分
        Integer integration = memberResponseVo.getIntegration();
        confirmVo.setIntegration(integration);


        //todo 4. 防重令牌

        CompletableFuture.allOf(addressFuture,cartItemsFuture).get();
        return confirmVo;
    }

}
