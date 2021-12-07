package com.cart.service;

import com.cart.vo.CartItem;

import java.util.concurrent.ExecutionException;

/**
 * user:lufei
 * DATE:2021/12/6
 **/
public interface cartService {
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;
}
