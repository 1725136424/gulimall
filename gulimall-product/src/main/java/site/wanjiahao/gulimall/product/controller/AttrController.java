package site.wanjiahao.gulimall.product.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.gulimall.product.entity.AttrEntity;
import site.wanjiahao.gulimall.product.service.AttrService;

import java.util.Arrays;
import java.util.Map;

// import org.apache.shiro.authz.annotation.RequiresPermissions;



/**
 * 商品属性
 *
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 16:18:27
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    /**
     * 列表
     * type 属性的类型 基础属性 销售属性
     */
    @RequestMapping("{type}/list/{catId}")
    // @RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params,
                  @PathVariable("type") String type,
                  @PathVariable("catId") Long catId){
        PageUtils page = attrService.queryPage(params, type, catId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    // @RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId){
		AttrEntity attr = attrService.getById(attrId);

        return R.ok().put("attr", attr);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttrEntity attr){
		attrService.save(attr);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrEntity attr){
		attrService.updateById(attr);

        return R.ok();
    }

    /**
     * 修改状态
     */
    @PostMapping("/changeStatus")
    public R changeStatus(@RequestBody AttrEntity attr){
        attrService.updateById(attr);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeRelation(Arrays.asList(attrIds));
        return R.ok();
    }

}
