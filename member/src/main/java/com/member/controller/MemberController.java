package com.member.controller;

import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.common.exception.BizCodeException;
import com.member.exception.PhoneExitException;
import com.member.exception.UsernameExitException;
import com.member.feign.CouponFeignService;
import com.member.vo.MemberRegisterVo;
import com.member.vo.SocialUser;
import com.member.vo.UserLoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.member.entity.MemberEntity;
import com.member.service.MemberService;
import com.common.utils.PageUtils;
import com.common.utils.R;



/**
 * 会员
 *
 * @author lufei
 * @email 2362487738@qq.com
 * @date 2021-10-03 22:22:05
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;
    @Autowired
    CouponFeignService couponFeignService;

    @RequestMapping("/coupons")
    public R test(){
        MemberEntity member = new MemberEntity();
        member.setNickname("张三");
        System.out.println(member.getNickname());
        R membercoupon = couponFeignService.membercoupon();
        return R.ok().put("member",member).put("coupons",membercoupon.get("coupon"));
    }

    @PostMapping("/register")
    public R register(@RequestBody MemberRegisterVo registerVo) {
        try {
            memberService.regist(registerVo);
        }catch (PhoneExitException e) {
            R.error(BizCodeException.PHONE_EXIST_EXCEPTION.getCode(), BizCodeException.PHONE_EXIST_EXCEPTION.getMsg());
        }catch (UsernameExitException e) {
            R.error(BizCodeException.USER_EXIST_EXCEPTION.getCode(), BizCodeException.USER_EXIST_EXCEPTION.getMsg());
        }
        return R.ok();
    }

    @PostMapping("/oauth2/login")
    public R Login(@RequestBody SocialUser socialUser) {
        MemberEntity member = memberService.login(socialUser);
        if (member!=null) {
            return R.ok().setData(member);
        }else {
            return R.error(BizCodeException.LOGINACCT_PASSWORD_INVAILD_EXCEPTION.getCode()
                    , BizCodeException.LOGINACCT_PASSWORD_INVAILD_EXCEPTION.getMsg());
        }
    }

    @PostMapping("/login")
    public R login(@RequestBody UserLoginVo vo) {
        MemberEntity member = memberService.login(vo);
        if (member!=null) {
            // TODO 登录成功处理
            return R.ok();
        }else {
            return R.error(BizCodeException.LOGINACCT_PASSWORD_INVAILD_EXCEPTION.getCode()
                    , BizCodeException.LOGINACCT_PASSWORD_INVAILD_EXCEPTION.getMsg());
        }
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
