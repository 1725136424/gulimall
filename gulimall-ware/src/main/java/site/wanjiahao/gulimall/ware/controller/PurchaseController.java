package site.wanjiahao.gulimall.ware.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.gulimall.ware.entity.PurchaseEntity;
import site.wanjiahao.gulimall.ware.service.PurchaseService;
import site.wanjiahao.gulimall.ware.vo.DonePurchaseVo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

// import org.apache.shiro.authz.annotation.RequiresPermissions;



/**
 * 采购信息
 *
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 16:04:38
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("ware:purchase:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 查询没有在采购的采购单
     */
    @GetMapping("/unreceive")
    public R listUnReceive() {
        List<PurchaseEntity> purchaseEntities =  purchaseService.listUnReceive();
        return R.ok().put("data", purchaseEntities);
    }

    /**
     * 领取采购单
     */
    @PostMapping("/receive")
    public R receive(@RequestBody List<Long> purchaseIds) {
        purchaseService.receive(purchaseIds);
        return R.ok();
    }

    /**
     * 完成采购单
     */
    @PostMapping("/done")
    private R done(@RequestBody DonePurchaseVo donePurchaseVo) {
        purchaseService.done(donePurchaseVo);
        return R.ok();
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("ware:purchase:info")
    public R info(@PathVariable("id") Long id){
		PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("ware:purchase:save")
    public R save(@RequestBody PurchaseEntity purchase){
		purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("ware:purchase:update")
    public R update(@RequestBody PurchaseEntity purchase){
		purchaseService.updateById(purchase);
        return R.ok();
    }

    /**
     * 分配用户
     */
    @PostMapping("/assignUser")
    public R assignUser(@RequestBody PurchaseEntity purchase){
        purchaseService.assignUser(purchase);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("ware:purchase:delete")
    public R delete(@RequestBody Long[] ids){
		purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
