package com.cart.service;

import com.cart.vo.Cart;
import com.cart.vo.CartItem;

import java.util.concurrent.ExecutionException;

/**
 * user:lufei
 * DATE:2021/12/6
 **/
public interface cartService {
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    CartItem GetCartItem(Long skuId);

    Cart getCart() throws ExecutionException, InterruptedException;

    void clearCart(String cartKey);

    void checkItem(Long skuId, Integer check);

    void countItem(Long skuId, Integer count);

    void deleteItem(Long skuId);
}
