package site.wanjiahao.gulimall.ware.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import site.wanjiahao.common.utils.R;

@FeignClient("gulimall-product")
public interface ProductFeignService {

    // 查询sku信息
    @GetMapping("/product/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);
}
