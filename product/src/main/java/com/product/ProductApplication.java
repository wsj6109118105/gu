package com.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/*
    1.整合Mybatis-plus
        1 ) 导入依赖。
        2 ) 配置
            1.配置数据源
                1 ) 导入数据库驱动
                2 ) 配置数据源相关信息
            2.配置Mybatis-plus
                1 ) 使用@MapperScan扫描接口
                2 ) 配置sql映射文件位置

 */
@MapperScan("com/product/dao")
@SpringBootApplication
public class ProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }

}
