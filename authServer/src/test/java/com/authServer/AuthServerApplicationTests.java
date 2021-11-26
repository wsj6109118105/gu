package com.authServer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;

//@SpringBootTest
class AuthServerApplicationTests {

    @Test
    void contextLoads() {
        SCryptPasswordEncoder sCryptPasswordEncoder = new SCryptPasswordEncoder();
        String encode = sCryptPasswordEncoder.encode("123456");
        System.out.println(encode);
        System.out.println(sCryptPasswordEncoder.matches("123456", "$e0801$0LLDlqM5dxvHwP8nquLszyacHger+TT7mrIE11Qp9lTMtZRg06Yr4yItreYNYK6uPUm6uHeBMeQpjNekrYjAIg==$9rEBCNreJj7QJEF7i2RbMMYZ0fmAC1o8Zs/4XcWlyEQ="));
    }

}
