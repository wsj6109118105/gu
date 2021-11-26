package com.member.service.impl;

import com.member.dao.MemberLevelDao;
import com.member.entity.MemberLevelEntity;
import com.member.exception.PhoneExitException;
import com.member.exception.UsernameExitException;
import com.member.vo.MemberRegisterVo;
import com.member.vo.UserLoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.utils.PageUtils;
import com.common.utils.Query;

import com.member.dao.MemberDao;
import com.member.entity.MemberEntity;
import com.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegisterVo registerVo) {
        MemberDao memberDao = this.baseMapper;
        MemberEntity member = new MemberEntity();
        // 设置默认等级
        MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();
        member.setLevelId(levelEntity.getId());
        // 检查用户名和手机号是否唯一,为了让controller感知异常，使用异常机制
        CheckPhoneUnique(registerVo.getPhone());
        CheckUserNameUnique(registerVo.getUserName());
        member.setMobile(registerVo.getPhone());
        member.setUsername(registerVo.getUserName());

        // 密码要进行加密
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(registerVo.getPassWord());
        System.out.println(encode);
        member.setPassword(encode);
        System.out.println(member.getPassword());
        // TODO 其他的默认信息

        memberDao.insert(member);
    }

    @Override
    public boolean CheckEmailUnique(String email) {
        return false;
    }

    @Override
    public void CheckPhoneUnique(String phone) throws PhoneExitException {
        MemberDao memberDao = this.baseMapper;
        Long mobile = memberDao.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (mobile > 0) {
            throw new PhoneExitException();
        }
    }

    @Override
    public void CheckUserNameUnique(String userName) throws UsernameExitException {
        MemberDao memberDao = this.baseMapper;
        Long username = memberDao.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (username > 0) {
            throw new UsernameExitException();
        }
    }

    @Override
    public MemberEntity login(UserLoginVo vo) {
        String loginacct = vo.getLoginacct();
        String passWord = vo.getPassWord();
        MemberDao memberDao = this.baseMapper;
        MemberEntity member = memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginacct).or().eq("mobile", loginacct));
        if (member!=null) {
            String password = member.getPassword();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            System.out.println(passWord);
            System.out.println(password);
            boolean matches = passwordEncoder.matches(passWord, password);
            if (matches) {
                return member;
            }else {
                return null;
            }
        }

        return null;
    }

}
