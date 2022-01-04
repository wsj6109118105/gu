package com.seckill.scheduled;

import com.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/** 秒杀商品定时上架
 * user:lufei
 * DATE:2022/1/3
 **/
@Service
@Slf4j
public class SeckillSkuScheduled {

    @Autowired
    SeckillService seckillService;

    @Autowired
    RedissonClient redissonClient;

    private final String UPLOAD_LOCK = "seckill:upload:lock";

    // todo 幂等性处理
    @Scheduled(cron = "0 * * * * ?")
    public void uploadSecKillSkuLatest3Days() {
        log.info("商品扫描信息.....");
        // 分布式锁
        RLock lock = redissonClient.getLock(UPLOAD_LOCK);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            seckillService.uploadSecKillSkuLatest3Days();
        } finally {
            lock.unlock();
        }
    }
}
