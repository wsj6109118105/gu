package com.cart.vo;

import lombok.Data;
import lombok.ToString;

/**
 * user:lufei
 * DATE:2021/12/6
 **/
@Data
@ToString
public class UserInfoTo {

    private Long userId;
    private String userKey;

    private boolean tempUser = false;
}
