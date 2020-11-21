package site.wanjiahao.gulimall.cart.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import site.wanjiahao.common.utils.R;

@FeignClient("gulimall-product")
public interface ProductFeignService {

    @RequestMapping("/product/skuinfo/info/{skuId}")
    R skuInfo(@PathVariable("skuId") Long skuId);

    @RequestMapping("/product/skusaleattrvalue/saleAttr/sku/{skuId}")
    R infoAttr(@PathVariable("skuId") Long skuId);

}
