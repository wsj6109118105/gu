package com.product;


import com.product.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;


@SpringBootTest
class ProductApplicationTests {

    @Autowired
    CategoryService categoryService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

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
