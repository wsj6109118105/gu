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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
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
            List<SeckillSessionsWithSkusVo> data = Session.getData(new TypeReference<List<SeckillSessionsWithSkusVo>>() {});
            // 缓存到 redis
            //     1 保存活动时间   key:start_endTime   val:skuIds
            saveSessionInfos(data);
            //     2 商品的详细信息
            saveSessionSkuInfos(data);
        }
    }

    /**
     * 保存活动信息
     * @param sessions
     */
    private void saveSessionInfos(List<SeckillSessionsWithSkusVo> sessions) {
        sessions.forEach(session->{
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();
            String key = SESSIONS_CACHE_PREFIX+startTime+"_"+endTime;
            // todo skuIds
            List<String> skuIds = session.getRelationEntities().stream().map(skuId -> {
                return skuId.getId().toString();
            }).collect(Collectors.toList());
            stringRedisTemplate.opsForList().leftPushAll(key,skuIds);
        });
    }

    /**
     * 保存商品信息
     */
    private void saveSessionSkuInfos(List<SeckillSessionsWithSkusVo> sessions){
        sessions.forEach(session->{
            // 准备 hash操作
            BoundHashOperations<String, Object, Object> ops = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
            session.getRelationEntities().forEach(seckillSkuVo -> {
                SecKillSkuRedisTo redisTo = new SecKillSkuRedisTo();
                // sku 基本信息
                R info = productFeignService.skuInfo(seckillSkuVo.getSkuId());
                if (info.getCode()==0) {
                    SkuInfoVo skuInfo = info.getData("skuInfo", new TypeReference<SkuInfoVo>() {});
                    redisTo.setSkuInfoVo(skuInfo);
                }
                // sku 秒杀信息
                BeanUtils.copyProperties(seckillSkuVo,redisTo);
                // 设置时间
                redisTo.setStartTime(session.getStartTime().getTime());
                redisTo.setEndTime(session.getEndTime().getTime());
                // 设置随机码
                String token = UUID.randomUUID().toString().replace("-", "");
                redisTo.setRandomCode(token);

                // 商品可以秒杀的数量  限流
                RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                semaphore.trySetPermits(seckillSkuVo.getSeckillCount());

                String s = JSON.toJSONString(redisTo);
                ops.put(seckillSkuVo.getSkuId(),s);
            });
        });
    }
}
