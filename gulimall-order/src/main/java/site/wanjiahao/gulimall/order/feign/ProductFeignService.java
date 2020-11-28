package site.wanjiahao.gulimall.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import site.wanjiahao.gulimall.order.to.SpuInfoEntityTo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@FeignClient("gulimall-product")
public interface ProductFeignService {

    @PostMapping("/product/spuinfo/spus/weight")
    Map<Long, BigDecimal> getWeightBySpuIds(@RequestBody List<Long> spuIds);

    @PostMapping("/product/spuinfo/spus")
    Map<Long, SpuInfoEntityTo> listSpuInfoMapByIds(@RequestBody List<Long> spuIds);
}
