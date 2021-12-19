package com.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.utils.PageUtils;
import com.member.entity.MemberReceiveAddressEntity;

import java.util.List;
import java.util.Map;

/**
 * 会员收货地址
 *
 * @author lufei
 * @email 2362487738@qq.com
 * @date 2021-10-03 22:22:06
 */
public interface MemberReceiveAddressService extends IService<MemberReceiveAddressEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 查询对应会员的收货地址列表
     * @param memberId 会员id
     * @return 返回会员收货地址列表信息
     */
    List<MemberReceiveAddressEntity> getAddress(Long memberId);
}

