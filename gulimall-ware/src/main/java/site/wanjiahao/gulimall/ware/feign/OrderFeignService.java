package site.wanjiahao.gulimall.ware.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import site.wanjiahao.common.to.OrderEntityTo;

@FeignClient("gulimall-order")
public interface OrderFeignService {


    @GetMapping("/order/order/info/orderSn/{orderSn}")
    OrderEntityTo getByOrderSn(@PathVariable("orderSn") String orderSn);
}
