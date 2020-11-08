package site.wanjiahao.gulimall.product.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import site.wanjiahao.common.utils.R;

@FeignClient("gulimall-ware")
public interface WareFeignService {

    @GetMapping("/ware/waresku/sku/{skuId}/hasStock")
    R hasStock(@PathVariable("skuId") Long skuId);

    /**
     * 查询所有商品的库存信息
     */
    @GetMapping("/ware/waresku/sku/hasAllStock")
    R listAllStock();
}
