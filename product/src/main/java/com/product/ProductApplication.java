package com.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;


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
    2.使用逻辑删除
        1 ) 配置全局的逻辑删除规则
        2 )
    3.JSR303校验
        1 ) 给Bean添加校验注解，并定义自己的错误提示
        2 ) 开启校验功能，在需要校验的请求数据上添加@Valid
        3 ) 校验的Bean后紧跟BindingResult，可以获取到校验结果
        4 ) 分组校验
            1.为校验注解添加分组:groups(接口信息)
            2.在Controller上使用@Validated标注要校验的分组
            3.默认没有指定分组的校验在分组校验不生效，不校验分组才会生效
        5 ) 自定义校验
            1.编写一个自定义的校验注解 ListValue
            2.编写一个自定义的校验器 ListValueValidDation需要实现ConstraintValidator<A extend Annotation,T> 第一个参数为对应的注解，第二个为处理的类型
            3.关联自定义的校验器和自定义的校验注解  @Constraint(validatedBy = {ListValueValidDation.class})//可以在自定义注解指定校验器
    4.统一的异常处理
        @ControllerAdvice

    6.整合redis
        1 ) 引入依赖
        2 ) 配置信息

    7.整合 Redisson 作为分布式锁的功能框架
        1 ) 引入依赖
        2 ) 配置参照官方文档

    8.整合 springCache 简化缓存开发
        1 ) 引入依赖
        2 ) 配置
        3 ) 修改配置，将数据保存为Json格式
            CacheAutoConfiguration -> RedisCacheConfiguration -> RedisCacheManager
            -> 初始化配置
 */
@EnableFeignClients(basePackages = {"com.product.feign"})
@EnableDiscoveryClient
@MapperScan("com/product/dao")
@SpringBootApplication
public class ProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }

}
