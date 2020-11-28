package site.wanjiahao.gulimall.product.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.common.valid.SaveSpuInfoGroup;
import site.wanjiahao.gulimall.product.entity.SpuInfoEntity;
import site.wanjiahao.gulimall.product.service.SpuInfoService;
import site.wanjiahao.gulimall.product.vo.SpuInfoUpdateStatusVo;
import site.wanjiahao.gulimall.product.vo.SpuInfoVo;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

// import org.apache.shiro.authz.annotation.RequiresPermissions;



/**
 * spu信息
 *
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-15 21:07:35
 */
@RestController
@RequestMapping("product/spuinfo")
public class SpuInfoController {
    @Autowired
    private SpuInfoService spuInfoService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("product:spuinfo:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = spuInfoService.queryPage(params);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("product:spuinfo:info")
    public R info(@PathVariable("id") Long id){
		SpuInfoEntity spuInfo = spuInfoService.getById(id);

        return R.ok().put("spuInfo", spuInfo);
    }

    /**
     * 查询某spu下的重量信息
     */
    @PostMapping("/spus/weight")
    public Map<Long, BigDecimal> getWeightBySpuIds(@RequestBody List<Long> spuIds) {
        return spuInfoService.getWeightBySpuIds(spuIds);
    }

    /**
     * 查询spu信息，封装为map key:id value:entity
     */
    @PostMapping("/spus")
    public Map<Long, SpuInfoEntity> listSpuInfoMapByIds(@RequestBody List<Long> spuIds) {
        return spuInfoService.listSpuInfoMapByIds(spuIds);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:spuinfo:save")
    public R save(@RequestBody SpuInfoEntity spuInfo){
		spuInfoService.save(spuInfo);
        return R.ok();
    }

    /**
     * 保存sku以及其他相关数据
     */
    @PostMapping("/saveSpuInfo")
    public R saveSpuInfo(@Validated(SaveSpuInfoGroup.class) @RequestBody SpuInfoVo spuInfoVo) {
        spuInfoService.saveSpuInfo(spuInfoVo);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:spuinfo:update")
    public R update(@RequestBody SpuInfoEntity spuInfo){
		spuInfoService.updateById(spuInfo);

        return R.ok();
    }

    /**
     * 修改spu上架状态
     */
    @PostMapping("/updatePublishStatus")
    public R updatePublishStatus(@RequestBody SpuInfoUpdateStatusVo spuInfoUpdateStatusVo) {
        spuInfoService.updatePublishStatus(spuInfoUpdateStatusVo);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:spuinfo:delete")
    public R delete(@RequestBody Long[] ids){
		spuInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
