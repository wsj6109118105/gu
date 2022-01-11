package com.seckill.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.common.utils.R;
import com.seckill.feign.CouponFeignService;
import com.seckill.feign.ProductFeignService;
import com.seckill.service.SeckillService;
import com.seckill.to.SecKillSkuRedisTo;
import com.seckill.vo.SeckillSessionsWithSkusVo;
import com.seckill.vo.SeckillSkuVo;
import com.seckill.vo.SkuInfoVo;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * user:lufei
 * DATE:2022/1/3
 **/
@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RedissonClient redissonClient;

    private final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";   // 活动键的前缀

    private final String SKUKILL_CACHE_PREFIX = "seckill:skus";    //  商品的键的前缀

    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:";   // 商品信号量+ 商品随机码

    /**
     * 上架最近3天的秒杀商品
     */
    @Override
    public void uploadSecKillSkuLatest3Days() {
        // 扫描需要参与秒杀的商品
        R Session = couponFeignService.getLatest3DaysSession();
        if (Session.getCode() == 0) {
            // 上架商品
            List<SeckillSessionsWithSkusVo> data = Session.getData(new TypeReference<List<SeckillSessionsWithSkusVo>>() {
            });
            if (data != null) {
                // todo 保存是设置过期时间
                // 缓存到 redis
                //     1 保存活动时间   key:start_endTime   val:skuIds
                saveSessionInfos(data);
                //     2 商品的详细信息
                saveSessionSkuInfos(data);
            }

        }
    }

    /**
     * 查询当前场次秒杀商品的信息
     * @return
     */
    @Override
    public List<SecKillSkuRedisTo> getCurrentSeckillSkus() {
        // 确定当前时间属于哪个秒杀场次
        long time = new Date().getTime();
        Set<String> keys = stringRedisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
        for (String key : keys) {
            String replace = key.replace(SESSIONS_CACHE_PREFIX, "");
            String[] s = replace.split("_");
            Long startTime = Long.parseLong(s[0]);
            Long endTime = Long.parseLong(s[1]);
            // 获取当前场次的商品信息
            if (time>=startTime&&time<endTime) {
                List<String> range = stringRedisTemplate.opsForList().range(key, 0, -1);
                BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                List<String> list = hashOps.multiGet(range);
                if (list!=null) {
                    List<SecKillSkuRedisTo> redisTos = list.stream().map(item -> {
                        //String s1 = item.toString();
                        SecKillSkuRedisTo skuRedisTo = JSON.parseObject(item, SecKillSkuRedisTo.class);
                        return skuRedisTo;
                    }).collect(Collectors.toList());
                    return redisTos;
                }
            }
        }
        return null;
    }

    /**
     * 获取某个商品的秒杀预告信息
     * @param skuId 商品id
     * @return
     */
    @Override
    public SecKillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        Set<String> keys = hashOps.keys();
        if (keys!=null&&keys.size()>0) {
            String regx = "\\d_"+skuId;
            for (String key : keys) {
                if (Pattern.matches(regx,key)) {
                    String s = hashOps.get(key);
                    SecKillSkuRedisTo to = JSON.parseObject(s, SecKillSkuRedisTo.class);
                    long now = new Date().getTime();
                    Long start = to.getStartTime();
                    Long end = to.getEndTime();
                    if (!(now>=start&&now<=end)) {
                        to.setRandomCode(null);
                    }
                    return to;
                }
            }
        }

        return null;
    }

    /**
     * 秒杀服务实现
     * @param killId 场次及商品id
     * @param key 随机码
     * @param num 秒杀数量
     * @return
     */
    @Override
    public String kill(String killId, String key, Integer num) {
        return null;
    }

    /**
     * 保存活动信息
     *
     * @param sessions
     */
    private void saveSessionInfos(List<SeckillSessionsWithSkusVo> sessions) {
        sessions.forEach(session -> {
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();
            String key = SESSIONS_CACHE_PREFIX + startTime + "_" + endTime;
            Boolean aBoolean = stringRedisTemplate.hasKey(key);
            if (!aBoolean) {
                List<String> skuIds = session.getRelationEntities().stream().map(skuId -> skuId.getPromotionSessionId()+"_"+skuId.getSkuId().toString()).collect(Collectors.toList());
                stringRedisTemplate.opsForList().leftPushAll(key, skuIds);
            }
        });
    }

    /**
     * 保存商品信息,并根据秒杀商品数量设置信号量
     */
    private void saveSessionSkuInfos(List<SeckillSessionsWithSkusVo> sessions) {
        sessions.forEach(session -> {
            // 准备 hash操作

            BoundHashOperations<String, Object, Object> ops = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);

            session.getRelationEntities().forEach(seckillSkuVo -> {
                // 设置随机码
                String token = UUID.randomUUID().toString().replace("-", "");
                if (!ops.hasKey(seckillSkuVo.getPromotionSessionId().toString()+"_"+seckillSkuVo.getSkuId().toString())) {
                    SecKillSkuRedisTo redisTo = new SecKillSkuRedisTo();
                    // sku 基本信息
                    R info = productFeignService.skuInfo(seckillSkuVo.getSkuId());
                    if (info.getCode() == 0) {
                        SkuInfoVo skuInfo = info.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        redisTo.setSkuInfoVo(skuInfo);
                    }
                    // sku 秒杀信息
                    BeanUtils.copyProperties(seckillSkuVo, redisTo);
                    // 设置时间
                    redisTo.setStartTime(session.getStartTime().getTime());
                    redisTo.setEndTime(session.getEndTime().getTime());

                    redisTo.setRandomCode(token);

                    // 商品可以秒杀的数量  限流
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                    semaphore.trySetPermits(seckillSkuVo.getSeckillCount());
                    String s = JSON.toJSONString(redisTo);
                    ops.put(seckillSkuVo.getPromotionSessionId().toString()+"_"+seckillSkuVo.getSkuId().toString(), s);
                }
            });
        });
    }
}
