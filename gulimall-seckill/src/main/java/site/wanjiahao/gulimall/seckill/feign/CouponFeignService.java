package site.wanjiahao.gulimall.seckill.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import site.wanjiahao.common.utils.R;

import java.util.List;

@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    @GetMapping("/coupon/seckillsession/getThreadSession")
    R getThreeSession();

    @PostMapping("/coupon/seckillsession/publish")
    R publish(@RequestBody List<Long> sessionIds);
}
