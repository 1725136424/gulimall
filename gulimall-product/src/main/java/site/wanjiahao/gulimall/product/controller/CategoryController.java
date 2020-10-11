package site.wanjiahao.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

// import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import site.wanjiahao.gulimall.product.entity.CategoryEntity;
import site.wanjiahao.gulimall.product.service.CategoryService;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.R;


/**
 * 商品三级分类
 *
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 16:18:27
 */
@RestController
@RequestMapping("product/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 查询所有分类，以及其子分类数据
     */
    @RequestMapping("/list")
    // @RequiresPermissions("product:category:list")
    public R list(@RequestParam Map<String, Object> params) {
        List<CategoryEntity> entities = categoryService.listWithTree();
        return R.ok().put("data", entities);
    }

    /**
     * 根据父分类获取所有分类集合
     */
    @RequestMapping("/list/{pcid}")
    public R list(@PathVariable("pcid") Long pcid) {
        List<CategoryEntity> entities = categoryService.listCategoryByPcid(pcid);
        return R.ok().put("data", entities);
    }

    /**
     * 根据品牌id获取所有的分类
     */
    @GetMapping("/brand/{brandId}")
    public R listByBrandId(@PathVariable("brandId") Long brandId,
                           @RequestParam Map<String, Object> params) {
        PageUtils pageUtils = categoryService.listWithPageByBranId(params, brandId);
        return R.ok().put("page", pageUtils);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
    // @RequiresPermissions("product:category:info")
    public R info(@PathVariable("catId") Long catId) {
        CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("category", category);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:category:save")
    public R save(@RequestBody CategoryEntity category) {
        categoryService.save(category);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:category:update")
    public R update(@RequestBody CategoryEntity category) {
        categoryService.updateById(category);

        return R.ok();
    }

    /**
     *  批量修改
     */
    @RequestMapping("/batchUpdate")
    public R update(@RequestBody CategoryEntity[] categoryEntities) {
        categoryService.updateBatch(Arrays.asList(categoryEntities));
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:category:delete")
    public R delete(@RequestBody Long[] catIds) {
        categoryService.removeByIds(Arrays.asList(catIds));
        return R.ok();
    }

}
