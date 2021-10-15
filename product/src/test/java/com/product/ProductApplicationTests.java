package com.product;


import com.product.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;



@SpringBootTest
class ProductApplicationTests {

    @Autowired
    CategoryService categoryService;

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
