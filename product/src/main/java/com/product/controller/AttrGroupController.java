package com.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.product.entity.AttrEntity;
import com.product.service.AttrService;
import com.product.service.CategoryService;
import com.product.vo.AttrGroupRelationVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.product.entity.AttrGroupEntity;
import com.product.service.AttrGroupService;
import com.common.utils.PageUtils;
import com.common.utils.R;


/**
 * 属性分组
 *
 * @author lufei
 * @email 2362487738@qq.com
 * @date 2021-10-03 21:30:31
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    /**
     * 列表
     */
    @RequestMapping("/list/{catId}")
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params, @PathVariable("catId") long catId){
        //PageUtils page = attrGroupService.queryPage(params);

        PageUtils page = attrGroupService.queryPage(params,catId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

        Long catelogId = attrGroup.getCatelogId();

        Long[] Path = categoryService.findCatelogPath(catelogId);

        attrGroup.setCatelogPath(Path);

        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

    /**
     * 查询分组关联属性
     * @param groupId
     * @return
     */
    @GetMapping("/{groupId}/attr/relation")
    public R attrRelation(@PathVariable("groupId") Long groupId){
        List<AttrEntity> entityList = attrService.getRelationAttr(groupId);

        return R.ok().put("data",entityList);
    }

    /**
     * 查询分组未关联属性
     * @param groupId
     * @param params
     * @return
     */
    @GetMapping("/{groupId}/noattr/relation")
    public R attrNoRelation(@PathVariable("groupId") Long groupId,
                            @RequestParam Map<String, Object> params){

        PageUtils page = attrService.getNoRelation(groupId,params);

        return R.ok().put("page",page);
    }

    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVo[] attr){

        attrGroupService.deleteRelation(attr);

        return R.ok();
    }
}
