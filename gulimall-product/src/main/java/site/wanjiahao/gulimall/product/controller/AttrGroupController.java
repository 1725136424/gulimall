package site.wanjiahao.gulimall.product.controller;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.gulimall.product.entity.AttrGroupEntity;
import site.wanjiahao.gulimall.product.service.AttrGroupService;
import site.wanjiahao.gulimall.product.service.CategoryService;
import site.wanjiahao.gulimall.product.vo.AttrGroupVo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

// import org.apache.shiro.authz.annotation.RequiresPermissions;


/**
 * 属性分组
 *
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 16:18:27
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 列表
     */
    @RequestMapping("/list/{catId}")
    // @RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params,
                  @PathVariable(value = "catId") Long catId){
        List<AttrGroupVo> attrGroupVos = new ArrayList<>();
        PageUtils page = attrGroupService.queryPage(params, catId);
        List<?> list = page.getList();
        list.forEach((item) -> {
            AttrGroupVo attrGroupVo = new AttrGroupVo();
            BeanUtils.copyProperties(item, attrGroupVo);
            Long catelogId = attrGroupVo.getCatelogId();
            attrGroupVo.setCategory(categoryService.listById(catelogId));
            attrGroupVos.add(attrGroupVo);
        });
        page.setList(attrGroupVos);
        return R.ok().put("page", page);
    }

    /**
     * 获取本分类没有关联的数据
     */
    @RequestMapping("/category/{catId}/attr/otherList")
    public R otherList(@RequestParam Map<String, Object> params,
                       @PathVariable("catId") Long catId) {
        PageUtils page = attrGroupService.listOtherAttrWithPage(catId, params);
        return R.ok().put("page", page);
    }

    /**
     * 查询当前属性组下的属性
     */
    @RequestMapping("/{groupId}/attr/relation")
    // @RequiresPermissions("product:attrgroup:list")
    public R listAttr(@RequestParam Map<String, Object> params,
                  @PathVariable("groupId") Long groupId){
        PageUtils page = attrGroupService.queryPageWithAttr(params, groupId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    // @RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
        AttrGroupVo attrGroupVo = new AttrGroupVo();
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();
        // 获取分类的完整路径名称
        List<Long> cIds = categoryService.listCategoryPath(catelogId);
        attrGroupVo.setCatelogIds(cIds);
        BeanUtils.copyProperties(attrGroup, attrGroupVo);
        return R.ok().put("attrGroup", attrGroupVo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 保存关联关系
     */
    @RequestMapping("/{attrGroupId}/attr/saveRel")
    public R saveRel(@RequestBody Long[] attrId,
                     @PathVariable("attrGroupId") Long attrGroupId) {
        attrGroupService.saveRel(attrGroupId, attrId);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeRelation(Arrays.asList(attrGroupIds));
        return R.ok();
    }

    /**
     * 删除当前关联关系
     */
    @RequestMapping("/{groupId}/attr/{attrId}/delete")
    public R deleteRel(@PathVariable("groupId") Long groupId,
                       @PathVariable("attrId") Long attrId) {
        attrGroupService.deleteRelWithAttr(groupId, attrId);
        return R.ok();
    }

    /**
     * 批量删除当前关系
     */
    @RequestMapping("/{groupId}/attr/delete")
    public R deleteRel(@PathVariable("groupId") Long groupId,
                       @RequestBody Long[] attrGroupIds) {
        attrGroupService.deleteRelWithAttr(groupId, attrGroupIds);
        return R.ok();
    }


}
