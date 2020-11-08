package site.wanjiahao.gulimall.search.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.wanjiahao.common.to.ESProductMappingTo;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.gulimall.search.service.ESService;

import java.util.List;

@RestController
@RequestMapping("/search")
public class ESController {

    @Autowired
    private ESService esService;

    @PostMapping("/product/saveBatch")
    public R saveBatch(@RequestBody List<ESProductMappingTo> esProductMappingTos) {
        boolean b = esService.saveProduct(esProductMappingTos);
        return R.ok().put("data", b);
    }

    @PostMapping("/product/save")
    public R save(@RequestBody ESProductMappingTo esProductMappingTo) {
        boolean b = esService.save(esProductMappingTo);
        return R.ok().put("data", b);
    }

    @PostMapping("/product/deleteBatch")
    R deleteBatch(@RequestBody List<Long> skuIds) {
        boolean b = esService.deleteBatchProduct(skuIds);
        return R.ok().put("data", b);
    }

    @GetMapping("/product/delete")
    R delete(Long spuId) {
        boolean b = esService.delete(spuId);
        return R.ok().put("data", b);
    }
}
