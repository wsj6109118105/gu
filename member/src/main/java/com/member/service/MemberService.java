package com.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.utils.PageUtils;
import com.member.entity.MemberEntity;
import com.member.exception.PhoneExitException;
import com.member.exception.UsernameExitException;
import com.member.vo.MemberRegisterVo;

import java.util.Map;

/**
 * 会员
 *
 * @author lufei
 * @email 2362487738@qq.com
 * @date 2021-10-03 22:22:05
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(MemberRegisterVo registerVo);

    boolean CheckEmailUnique(String email);

    void CheckPhoneUnique(String phone) throws PhoneExitException;

    void CheckUserNameUnique(String userName) throws UsernameExitException;
}

