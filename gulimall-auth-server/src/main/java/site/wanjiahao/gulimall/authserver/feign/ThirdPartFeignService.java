package site.wanjiahao.gulimall.authserver.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.gulimall.authserver.to.VerifyCodeTo;

@FeignClient("gulimall-third-party")
public interface ThirdPartFeignService {

    @PostMapping(value = "/third-party/sms/sendCode")
    R sendCode(@RequestBody VerifyCodeTo verifyCodeTo);
}
