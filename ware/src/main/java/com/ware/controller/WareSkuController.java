package com.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.common.exception.BizCodeException;
import com.ware.Exception.NoStockException;
import com.ware.vo.LockStockResult;
import com.ware.vo.SkuHasStockVo;
import com.ware.vo.WareSkuLockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.ware.entity.WareSkuEntity;
import com.ware.service.WareSkuService;
import com.common.utils.PageUtils;
import com.common.utils.R;



/**
 * 商品库存
 *
 * @author lufei
 * @email 2362487738@qq.com
 * @date 2021-10-03 22:51:34
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    /**
     * 根据订单锁定库存
     * @param vo 订单信息
     * @return 返回锁定的结果
     */
    @PostMapping("/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockVo vo) {
        try {
            wareSkuService.orderLockStock(vo);
            return R.ok();
        }catch (NoStockException e) {
            return R.error(BizCodeException.NO_STOCK_EXCEPTION.getCode(), BizCodeException.NO_STOCK_EXCEPTION.getMsg());
        }

    }

    // 查询 sku 是否有库存
    @PostMapping("/hasstock")
    public R getHasStock(@RequestBody List<Long> skuIds){
        List<SkuHasStockVo> hasStockVos = wareSkuService.getHasStock(skuIds);
        // sku_id,stock
        return R.ok().setData(hasStockVos);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:waresku:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:waresku:info")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:waresku:delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
