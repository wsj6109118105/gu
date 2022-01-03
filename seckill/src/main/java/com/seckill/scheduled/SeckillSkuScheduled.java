package com.seckill.scheduled;

import com.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/** 秒杀商品定时上架
 * user:lufei
 * DATE:2022/1/3
 **/
@Service
@Slf4j
public class SeckillSkuScheduled {

    @Autowired
    SeckillService seckillService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void uploadSecKillSkuLatest3Days() {
        seckillService.uploadSecKillSkuLatest3Days();
    }
}
