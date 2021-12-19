package com.cart.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.cart.feign.ProductFeignService;
import com.cart.interceptor.CartInterceptor;
import com.cart.service.cartService;
import com.cart.vo.Cart;
import com.cart.vo.CartItem;
import com.cart.vo.UserInfoTo;
import com.cart.vo.skuInfoVo;
import com.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * user:lufei
 * DATE:2021/12/6
 **/
@Slf4j
@Service
public class cartServiceImpl implements cartService {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    private final String CART_PREFIX = "mall:cart:";

    /**
     * 将商品添加到购物车
     * @param skuId 商品id
     * @param num 商品数量
     * @return 当前添加的商品
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String res = (String) cartOps.get(skuId.toString());
        if (StringUtils.isEmpty(res)) {
            // 添加新商品到购物车
            CartItem cartItem = new CartItem();
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                // 远程查询商品的信息
                R skuInfo = productFeignService.getSkuInfo(skuId);
                skuInfoVo data = skuInfo.getData("skuInfo", new TypeReference<skuInfoVo>() {
                });
                // 商品添加到购物车
                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setImg(data.getSkuDefaultImg());
                cartItem.setTitle(data.getSkuTitle());
                cartItem.setSkuId(skuId);
                cartItem.setPrice(data.getPrice());
            },executor);
            // 远程查询 sku 组合信息
            CompletableFuture<Void> getSkuSaleAttrValues = CompletableFuture.runAsync(() -> {
                List<String> skuSaleAttrValue = productFeignService.getSkuSaleAttrValue(skuId);
                cartItem.setSkuAttr(skuSaleAttrValue);
            }, executor);

            CompletableFuture.allOf(getSkuSaleAttrValues,getSkuInfoTask).get();
            String s = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(),s);
            return cartItem;
        }
        // 购物车已有商品，修改数量
        CartItem cartItem = JSON.parseObject(res, CartItem.class);
        cartItem.setCount(num+cartItem.getCount());
        String s = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(),s);
        return cartItem;
    }

    /**
     * 获取购物车中的某个购物项
     * @param skuId 商品id
     * @return 购物车中的商品
     */
    @Override
    public CartItem GetCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String o = (String) cartOps.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(o, CartItem.class);
        return cartItem;
    }

    /**
     * 获取整个购物车
     * @return 返回用户购物车
     */
    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        Cart cart = new Cart();
        if (userInfoTo.getUserId()!=null) {
            // 登录了
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            // 如果有临时购物车 ，则进行合并
            List<CartItem> tempCartItem = getCartItem(CART_PREFIX + userInfoTo.getUserKey());
            if (tempCartItem!=null) {
                for (CartItem item : tempCartItem) {
                    addToCart(item.getSkuId(),item.getCount());
                }
                // 清除临时购物车
                clearCart(CART_PREFIX + userInfoTo.getUserKey());
            }
            List<CartItem> cartItem = getCartItem(cartKey);
            cart.setItems(cartItem);
        }else {
            // 没登录
            String cartKey = CART_PREFIX + userInfoTo.getUserKey();
            List<CartItem> cartItem = getCartItem(cartKey);
            cart.setItems(cartItem);
        }

        return cart;
    }

    /**
     * 获取到要操作的购物车
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey = "";
        if (userInfoTo.getUserId()!=null) {   // 登录
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        }else {         // 未登录
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
        return operations;
    }

    /**
     * 获取指定 key 的购物车信息
     * @param cartKey 购物车的键
     * @return 返回购物车信息
     */
    private List<CartItem> getCartItem(String cartKey) {
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(cartKey);
        List<Object> values = hashOps.values();
        if (values!=null) {
            List<CartItem> items = values.stream().map((obj) -> {
                String s = (String) obj;
                CartItem cartItem = JSON.parseObject(s, CartItem.class);
                return cartItem;
            }).collect(Collectors.toList());
            return items;
        }
        return null;
    }

    /**
     * 清空购物车
     * @param cartKey 购物车的键
     */
    @Override
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    /**
     * 勾选购物项
     * @param skuId 商品id
     * @param check 商品是否被勾选
     */
    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = GetCartItem(skuId);
        cartItem.setCheck(check == 1);
        String s = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(),s);
    }

    /**
     * 修改商品数量
     * @param skuId 商品id
     * @param count 修改的数量
     */
    @Override
    public void countItem(Long skuId, Integer count) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = GetCartItem(skuId);
        cartItem.setCount(count);
        String s = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(),s);
    }

    /**
     * 删除商品
     * @param skuId 商品的id
     */
    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    @Override
    public List<CartItem> getCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if(userInfoTo.getUserId()==null) {
            return null;
        }else {
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            List<CartItem> cartItem = getCartItem(cartKey);
            assert cartItem != null;
            return cartItem.stream().filter(CartItem::getCheck).map(item->{
                BigDecimal price = productFeignService.getPrice(item.getSkuId());
                // 更新为最新价格
                item.setPrice(price);
                return item;
            }).collect(Collectors.toList());
        }
    }
}
