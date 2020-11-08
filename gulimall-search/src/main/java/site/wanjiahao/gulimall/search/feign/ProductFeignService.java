package site.wanjiahao.gulimall.search.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import site.wanjiahao.common.utils.R;

@FeignClient("gulimall-product")
public interface ProductFeignService {

    @RequestMapping("/product/attr/info/{attrId}")
    R infoAttr(@PathVariable("attrId") Long attrId);


    @RequestMapping("/product/brand/info/{brandId}")
    // @RequiresPermissions("product:brand:info")
    R infoBrand(@PathVariable("brandId") Long brandId);

    @RequestMapping("/product/category/info/{catId}")
    // @RequiresPermissions("product:category:info")
    R infoCategory(@PathVariable("catId") Long catId);

}
