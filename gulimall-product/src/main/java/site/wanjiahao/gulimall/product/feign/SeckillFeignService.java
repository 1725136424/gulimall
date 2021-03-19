package site.wanjiahao.gulimall.product.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.gulimall.product.feign.fallback.SeckillFeignServiceFallback;

@FeignClient(value = "gulimall-seckill", fallback = SeckillFeignServiceFallback.class)
public interface SeckillFeignService {

    @GetMapping("//seckill/sku/seckill/info/{skuId}")
    R seckillInfo(@PathVariable("skuId") Long skuId);
}
