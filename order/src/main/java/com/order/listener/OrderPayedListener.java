package com.order.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayConfig;
import com.alipay.api.internal.util.AlipaySignature;
import com.order.config.AlipayTemplate;
import com.order.service.OrderService;
import com.order.vo.PayAsyncVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * user:lufei
 * DATE:2022/1/1
 **/
@RestController
public class OrderPayedListener {

    @Autowired
    OrderService orderService;

    @PostMapping("/Ali/noti")
    public String handleAlipayed(PayAsyncVo vo, HttpServletRequest request) throws AlipayApiException, UnsupportedEncodingException {
        // 验签
        System.out.println("支付宝通知到位");
        Map<String,String> params = new HashMap<String,String>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
            params.put(name, valueStr);
        }
        boolean verify_result = AlipaySignature.rsaCheckV1(params, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApYoPsRwjtLUXFQ4LaR6Eh+YA9RHFXirQI+AdcvPUWOki31U18w02zh2I7Vr6EAOLwAhTj593olyv5bU0wdFDb4yHZSxMPaJGyblqSNUPSnNWRLuZHXIjZO7Q83IhE0sJd/fHUyH3BFSIKlf1utX/NP/eA4jwXyJ7MOk/VVnFP+A7kzWUKNYVOPy1OiD1aXRDTMYnh/iHDdm5gPbxL4RC8D2ZuRL+HvT4ZkLPWT75Ye+tatqwVyvuPbeX/7DjRG6rAKsnX5Oh7tvXGRGtiotaZNJG7ciabO1/Qq46hfDqQvINC2f9EQC67TmbwM0Y0/E/jA3fpMaJtGTD3t67Ogg29QIDAQAB", "UTF-8", "RSA2");
        if (verify_result) {
            System.out.println("签名验证成功");
            String result = orderService.handlePayResult(vo);
            return result;
        }else {
            System.out.println("签名验证失败");
            return "error";
        }
    }
}
