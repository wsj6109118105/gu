package com.member.exception;

/**
 * user:lufei
 * DATE:2021/11/25
 **/
public class UsernameExitException extends RuntimeException{
    public UsernameExitException() {
        super("用户名存在");
    }
}
