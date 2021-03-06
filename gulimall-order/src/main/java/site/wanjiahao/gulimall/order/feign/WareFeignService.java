package site.wanjiahao.gulimall.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import site.wanjiahao.common.to.LockStockTo;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.gulimall.order.entity.MQMessageEntity;

import java.util.List;
import java.util.Map;

@FeignClient("gulimall-ware")
public interface WareFeignService {

    @PostMapping("/ware/waresku/skus/hasStock")
    Map<Long, Boolean> hasStocks(@RequestBody List<Long> skuIds);

    @PostMapping("/ware/waresku/lockStock")
    R lockStock(@RequestBody LockStockTo lockStockTo);

    @GetMapping("/ware/waresku/unlockStock/{orderSn}")
    R unlockStock(@PathVariable("orderSn") String orderSn);

    @PostMapping("/mqMessage/save")
    R saveMessage(@RequestBody MQMessageEntity mqMessageEntity);

    @PostMapping("/mqMessage/update")
    R updateById(@RequestBody MQMessageEntity mqMessageEntity);

    @GetMapping("/mqMessage/select/{id}")
    R selectById(@PathVariable("id") String messageId);
}
