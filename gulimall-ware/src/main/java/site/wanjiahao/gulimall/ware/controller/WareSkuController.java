package site.wanjiahao.gulimall.ware.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.wanjiahao.common.code.BizCodeEnum;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.gulimall.ware.entity.WareSkuEntity;
import site.wanjiahao.gulimall.ware.service.WareSkuService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

// import org.apache.shiro.authz.annotation.RequiresPermissions;



/**
 * 商品库存
 *
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 16:04:38
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("ware:waresku:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 查询当前sku是否存在库存 stock - lockStock
     */
    @GetMapping("/sku/{skuId}/hasStock")
    public R hasStock(@PathVariable("skuId") Long skuId) {
        boolean res = wareSkuService.listHasStockBySkuId(skuId);
        return R.ok().put("data", res);
    }

    /**
     * 锁定库存
     */
    @PostMapping("/lockStock")
    public R lockStock(@RequestBody Map<Long, Integer> lockMap) {
        try {
            wareSkuService.lockStock(lockMap);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error(BizCodeEnum.STOCK_UN_ENOUGH_EXCEPTION.getBizCode(),
                    BizCodeEnum.STOCK_UN_ENOUGH_EXCEPTION.getMessage());
        }

        return R.ok();
    }

    /**
     * 查询所有sku是否有库存 stock - lockStock
     */
    @PostMapping("/skus/hasStock")
    public Map<Long, Boolean> hasStocks(@RequestBody List<Long> skuIds) {
        return wareSkuService.listStockMap(skuIds);
    }

    /**
     * 查询商品的所有库存信息
     */
    @GetMapping("/sku/hasAllStock")
    public R hasAllStock() {
        Map<Long, Boolean> resMap =  wareSkuService.listHasAllStock();
        return R.ok().put("data", resMap);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("ware:waresku:info")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("ware:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("ware:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("ware:waresku:delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
