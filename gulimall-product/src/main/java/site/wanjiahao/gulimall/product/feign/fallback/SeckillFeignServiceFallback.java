package site.wanjiahao.gulimall.product.feign.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.gulimall.product.feign.SeckillFeignService;

@Slf4j
@Component
public class SeckillFeignServiceFallback implements SeckillFeignService {
    @Override
    public R seckillInfo(Long skuId) {
        log.info("熔断方法调用");
        return R.error(522, "太多错误了");
    }
}
