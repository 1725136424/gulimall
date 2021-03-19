package site.wanjiahao.gulimall.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.gulimall.order.vo.ReceiveAddressVo;

import java.util.List;

@FeignClient("gulimall-member")
public interface MemberFeignService {

    @GetMapping("/member/memberreceiveaddress/addresses/{memberId}")
    List<ReceiveAddressVo> findAllAddress(@PathVariable("memberId") Long memberId);


    @GetMapping("/member/memberreceiveaddress/getPostage/{addressId}")
    R getPostage(@PathVariable("addressId") Long addressId);

    @GetMapping("/member/memberreceiveaddress/defaultAddress/{memberId}")
    ReceiveAddressVo findDefaultAddress(@PathVariable("memberId") Long memberId);

    @RequestMapping("/member/member/info/{id}")
    R info(@PathVariable("id") Long id);

}
