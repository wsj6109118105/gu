package com.member.exception;

/**
 * user:lufei
 * DATE:2021/11/25
 **/
public class PhoneExitException extends RuntimeException{
    public PhoneExitException() {
        super("手机号存在");
    }
}
