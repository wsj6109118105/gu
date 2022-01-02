package com.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "alipay")
@Component
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "2021000118684228";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCvhgMsTif2Lnw08KPMoVYfYiPpKKJofriZYT/arhTEndTJXCmqYyw6oXdelnugB0h83XiFfjKo3Ymb5JwoiUfbyBSe+Rf0paU09YQmzq0Q6YfyFUW62Yut8+P5p7Iy79cezMrNWo7kZvyhJQnQNpa9Ni9LY/dWMTu791Bm+GcR14P2gezxr41u6ZE4SAJFXeK3unw+cGtwBQFpLfDAqzVnZJwGt78qOTZPK3lSLsQON0KRKSWmGug+DOnyD6grkcKDGCikn8esMiO1YfYTxLKg/bBxMhhtxHB8TCNYeGEs5sIhulA1xvcP1JwPcTGiH60f9tu1RqGJaMvCKcZXcGtpAgMBAAECggEBAKvOXErA0cLZ64AST/LbtSYQ9R+jQ51O9LhFhbAyl2eBKihTCwGaA0/8fpBacfXJSz3VtsPMLCA4gRrT4bqX7XuG1BBrt/QjM5ur+kfWI3cbb5MZGWD00hFaWjsj13yeYQ3n3Va2nomY+Q1u6LUiyde5Mv/zgMWgeVzajpvL52+Axppb7FuL1ltaLsfQR7iSYEdfdeJgTYCFE/NXbjI5QFbW2lcG9nZn52iknho/y3PuX5kUZKfC664hY06sUI1cKowy49DRQhQvmW48EFyDSZ/HfB56ZndZ24hw8ewjYDyQFHSv+QF2iPwAsKOSKFLL9TtST3WFa9O8tZn4nliWPNUCgYEA9HDmPJJSviA8TEzbBABjOSRNC8pajR0MQTw5iCVZkcxjUhbdILYbEP9mYegDxPARqIYvzC4EqLjgiX1CsKqLQnq/r/lhAPABaedwF+ohgEs54bLruOz7mi7AomoUz1YTiDJwqSrb5V0X+n88Lr8cQ+t0QWhVoCjw/bejSk52xMsCgYEAt9LThzDFLd/ZRClSSAKnNpm+7rlhHR9jsyb5oVwm9MnCaRW7ipYb8ZDdwUu2Ff9iGUjp8kfcYpQQ/69wCctzAgQ6u4fKnXs8Huz7iPFXiktBxEmqD6p6aH5NL/B+rlpfoqXaW1pmJx067zq9JpeCC0VMAr34g7Z3M24rIMAxvhsCgYB2FvjTuGjq2w3mWpbQVxOYbnYeRRL761WZSmwM3uZnrlMpPEx12iQ3UPt4PytbABO306PRw0WS8UXghjgquylt43kpnZOEbW5xaFLZ6s1menErHVIN08c1VHLRw+frxozHFn8g6sizBQdpiABeIQuZsG1IkAwVxNGv8QCdDE0KgwKBgB7zrdBWouzdgzzZ5FPhtnwYGTEjH7oCngv9ZQ0JcjnHeyeslB56tgvPw7cXY3qCvObbAyFbfj1PBym2RZwwmJXfVnBbJf+sT2w5pQTi9kV/9Hvzl2H555COq7qFuR7scqv1VzYIm6i8YD8F2fGUkz4IlgrkJ+MtpxaO1041bmq/AoGBAJAvZn4hwWwasei+T7rwDdqPqxuT/520GIErdhye60ZQOLBD+zzXXTE1nmgSjKmaP81t8xYG43qkDyH/yC5a3CfcXmHENAsR+w5He0DumKh6EoS+PxIiIQ14lRgM/a7hpaw2jM0zT927k+n80gckNLLzxildJ18s8gMfCBBOegNo";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyQQceVUChTJGtF/a8SXufhSxDTKporieTq9NO7yDZSpDlAX1zVPT/nf0KWAlxq1TYappWMIYtyrOABhJyn6flNP6vuSBiM5lYsepHvYrtRHqlFiJruEkiaCgEZBKL5aCfBHYj0oqgQn9MpNV/PEH4cBYAVaiI4+VX8CBUQfeEGjgN6OkpLULZ3X0JUkmSnVvCNJ1m3PD68IIlbOfEZXJUKCqmZhzprGR5VWswjxA+g87cMwvijL4gdkSy/daG62Bz5vApcmmMkuX1k1fMWP4ajZCASVw8HD+MSLRhd8We9F97gd8CW0TavzbdR+mTS5H4yEgO8F9HRAsbkhV9yu0yQIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url = "http://j8gwiylg9a.51xd.pub/Ali/noti";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url = "http://member.happymall.mall/memberOrder.html";

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    private String timeout = "5";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"timeout_express\":\""+timeout+"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        //System.out.println("支付宝的响应："+result);

        return result;

    }
}
