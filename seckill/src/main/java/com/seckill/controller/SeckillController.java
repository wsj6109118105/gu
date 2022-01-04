package com.seckill.controller;

import com.common.utils.R;
import com.seckill.service.SeckillService;
import com.seckill.to.SecKillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * user:lufei
 * DATE:2022/1/4
 **/
@RestController
public class SeckillController {

    @Autowired
    SeckillService seckillService;

    /**
     * 返回当前时间可以参与的秒杀商品信息
     * @return
     */
    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus() {
        List<SecKillSkuRedisTo> skuRedisTo = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(skuRedisTo);
    }
}
