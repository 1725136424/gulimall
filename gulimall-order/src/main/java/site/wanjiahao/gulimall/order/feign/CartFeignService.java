package site.wanjiahao.gulimall.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.gulimall.order.vo.OrderItemVo;

import java.util.List;

@FeignClient("gulimall-cart")
public interface CartFeignService {

    @GetMapping("/findCheckCartItem")
    List<OrderItemVo> findCheckCartItem();

    @GetMapping("/getTotalPrice")
    R getTotalPrice();

}
