package com.seckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * user:lufei
 * DATE:2022/1/2
 **/
/*@Component
@Slf4j
public class HelloSchedule {

    *//**
     * 默认是阻塞的
     * 可以让业务以异步的方式自己提交到线程池
     * @throws InterruptedException
     *//*
    @Async  // 让方法异步执行
    @Scheduled(cron = "* * * ? * 7")
    public void hello() throws InterruptedException {
        log.info("hello....");
        Thread.sleep(3000);
    }
}*/
