package site.wanjiahao.gulimall.product.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import site.wanjiahao.common.to.ESProductMappingTo;
import site.wanjiahao.common.utils.R;

import java.util.List;

@FeignClient("gulimall-search")
public interface ESFeignService {

    @PostMapping("/search/product/saveBatch")
    R saveBatch(@RequestBody List<ESProductMappingTo> esProductMappingTos);

    /**
     * 删除所有spu对应的sku
     * @param skuIds
     * @return
     */
    @PostMapping("/search/product/deleteBatch")
    R deleteBatch(@RequestBody List<Long> skuIds);

    @PostMapping("/search/product/save")
    R save(@RequestBody ESProductMappingTo esProductMappingTo);

    @GetMapping("/search/product/delete")
    R delete(@RequestParam("spuId") Long spuId);
}
