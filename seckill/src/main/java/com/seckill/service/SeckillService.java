package com.seckill.service;

import com.seckill.to.SecKillSkuRedisTo;

import java.util.List;

/**
 * user:lufei
 * DATE:2022/1/3
 **/
public interface SeckillService {
    void uploadSecKillSkuLatest3Days();

    List<SecKillSkuRedisTo> getCurrentSeckillSkus();

    SecKillSkuRedisTo getSkuSeckillInfo(Long skuId);

    String kill(String killId, String key, Integer num);
}
