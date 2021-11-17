package com.product;


import com.product.dao.AttrGroupDao;
import com.product.dao.SkuSaleAttrValueDao;
import com.product.service.CategoryService;
import com.product.vo.SkuItemSaleAttrVo;
import com.product.vo.SkuItemVo;
import com.product.vo.SpuItemAttrGroupVo;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import java.util.List;


@SpringBootTest
class ProductApplicationTests {

    @Autowired
    CategoryService categoryService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    SkuSaleAttrValueDao saleAttrValueDao;

    @Test
    void test() {
        /*List<SpuItemAttrGroupVo> attrGroupWithAttrsBySpuId = attrGroupDao.getAttrGroupWithAttrsBySpuId(5L, 225L);
        System.out.println(attrGroupWithAttrsBySpuId);*/
        List<SkuItemSaleAttrVo> saleAttrsBySpuId = saleAttrValueDao.getSaleAttrsBySpuId(5L);
        System.out.println(saleAttrsBySpuId);
    }

    @Test
    void RedissonTest() {
        RLock lock = redissonClient.getLock("lock");
        lock.lock();
        try {
            System.out.println(redissonClient);
        }finally {
            lock.unlock();
        }
    }


    @Test
    void testRedis() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        //保存
        ops.set("hello","world");
        //查询
        System.out.println(ops.get("hello"));
    }

    @Test
    void contextLoads() {
    }

    @Test
    void testFindPath(){
        Long[] catelogPath = categoryService.findCatelogPath(227L);
        for (Long aLong : catelogPath) {
            System.out.println(aLong);
        }
    }


}
