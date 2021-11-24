package com.third.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * user:lufei
 * DATE:2021/11/24
 **/
@ConfigurationProperties(prefix = "send.mall")
@Data
public class SendMessageConfig {
    private String accountSId;
    private String accountToken;
    private String appId;
}
