package com.authServer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * user:lufei
 * DATE:2021/11/30
 **/
@ConfigurationProperties(prefix = "myauth.mall")
@Data
public class OAuth2ConfigurationProperties {
    private String client_id;
    private String client_secret;
}
