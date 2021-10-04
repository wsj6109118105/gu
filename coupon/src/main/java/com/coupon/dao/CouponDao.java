package com.coupon.dao;

import com.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author lufei
 * @email 2362487738@qq.com
 * @date 2021-10-03 21:57:21
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
