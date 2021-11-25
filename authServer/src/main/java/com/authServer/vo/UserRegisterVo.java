package com.authServer.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * user:lufei
 * DATE:2021/11/25
 **/
@Data
public class UserRegisterVo {

    @NotEmpty(message = "用户名必须填写")
    @Length(min = 6,max = 18,message = "用户名必须在6-18位")
    private String userName;

    @NotEmpty(message = "密码必须填写")
    @Length(min = 6,max = 18,message = "用户名必须在6-18位")
    private String passWord;

    @NotEmpty(message = "手机号必须填写")
    @Pattern(regexp = "^[1][0-9]{10}$",message = "手机号格式不正确")
    private String phone;

    @NotEmpty(message = "验证码必须填写")
    private String code;
}
