package com.coupon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
   1.命名空间：配置隔离
   默认：public(保留空间)。所有新增的所有配置都在public。
    1 ) 开发，测试，生成，环境隔离。在 bootstrap.properties 中配置命名空间即可。
    2 ) 每一个微服务互相隔离配置，每一个微服务都创建一个自己的命名空间。
   2.配置集：所有的配置的集合
   3.配置集ID：类似文件命
        Data Id:
   4.配置分组:可以在创建配置时自定义分组名
        默认所有的配置集都属于 DEFAULT_GROUP
        双11：1111    618：618   双十二：1212
        在 bootstrap.properties 中配置分组
   5.同时加载多个配置可以在 bootstrap.properties 中配置，在 nacos 配置中心进行设置。
        想要获取配置文件的值，可以使用 @Value 等注解，优先使用配置中心的配置。
 */

@MapperScan("com/coupon/dao")
@SpringBootApplication
public class CouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(CouponApplication.class, args);
    }

}
