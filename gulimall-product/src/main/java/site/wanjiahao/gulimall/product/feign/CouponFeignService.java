package site.wanjiahao.gulimall.product.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import site.wanjiahao.common.to.MemberPriceTo;
import site.wanjiahao.common.to.SkuFullReductionTo;
import site.wanjiahao.common.to.SkuLadderTo;
import site.wanjiahao.common.to.SpuBoundsTo;
import site.wanjiahao.common.utils.R;

import java.util.List;

@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    @RequestMapping("/coupon/spubounds/save")
     R save(@RequestBody SpuBoundsTo spuBounds);

    @RequestMapping("/coupon/skuladder/save")
     R save(@RequestBody SkuLadderTo skuLadderTo);


    @RequestMapping("/coupon/skufullreduction/save")
    R save(@RequestBody SkuFullReductionTo skuFullReductionTo);

    @RequestMapping("/coupon/memberprice/saveBatch")
    R save(@RequestBody List<MemberPriceTo> memberPriceTos);


}
