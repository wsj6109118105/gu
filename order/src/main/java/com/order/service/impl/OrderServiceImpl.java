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
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.utils.PageUtils;
import com.common.utils.Query;

import com.order.dao.OrderDao;
import com.order.entity.OrderEntity;
import com.order.service.OrderService;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {


    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberResponseVo memberResponseVo = LoginUser.loginUser.get();
        // 1. 远程查询所有的收货地址列表
        List<MemberAddress> address = memberFeignService.getAddress(memberResponseVo.getId());

        // 2. 远程查询购物车所有选中的购物项
        List<OrderItemVo> cartItems = cartFeignService.getCartItems();

        // 3. 查询用户积分
        Integer integration = memberResponseVo.getIntegration();

        //todo 4. 防重令牌
        confirmVo.setIntegration(integration);
        confirmVo.setAddresses(address);
        confirmVo.setItems(cartItems);
        return null;
    }

}
