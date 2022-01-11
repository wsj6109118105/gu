package com.seckill.controller;

import com.common.utils.R;
import com.seckill.service.SeckillService;
import com.seckill.to.SecKillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
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

    /**
     * 返回商品的秒杀优惠信息
     * @param skuId
     * @return
     */
    @GetMapping("/sku/seckill/{skuId}")
    public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId){

        SecKillSkuRedisTo to = seckillService.getSkuSeckillInfo(skuId);
        return R.ok().setData(to);
    }

    /**
     * 秒杀服务接口
     * @param killId 场次及商品id
     * @param key 随机码
     * @param num 秒杀数量
     * @return
     */
    @GetMapping("/kill")
    public R seckill(@RequestParam("killId") String killId,
                     @RequestParam("key") String key,
                     @RequestParam("num") Integer num) {
        String orderSn = seckillService.kill(killId,key,num);
        return R.ok().setData(orderSn);
    }
}
