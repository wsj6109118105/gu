package com.ware.dao;

import com.ware.entity.WareOrderTaskDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 库存工作单
 *
 * @author lufei
 * @email 2362487738@qq.com
 * @date 2021-10-03 22:51:34
 */
@Mapper
public interface WareOrderTaskDetailDao extends BaseMapper<WareOrderTaskDetailEntity> {

    void updateLockStatus(@Param("taskDetailId") Long taskDetailId);
}
