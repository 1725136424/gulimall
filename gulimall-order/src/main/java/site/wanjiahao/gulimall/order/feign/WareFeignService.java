package site.wanjiahao.gulimall.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import site.wanjiahao.common.utils.R;

import java.util.List;
import java.util.Map;

@FeignClient("gulimall-ware")
public interface WareFeignService {

    @PostMapping("/ware/waresku/skus/hasStock")
    Map<Long, Boolean> hasStocks(@RequestBody List<Long> skuIds);

    @PostMapping("/ware/waresku/lockStock")
    R lockStock(@RequestBody Map<Long, Integer> lockMap);
}
