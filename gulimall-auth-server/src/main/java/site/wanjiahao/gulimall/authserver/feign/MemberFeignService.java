package site.wanjiahao.gulimall.authserver.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.gulimall.authserver.vo.Auth2WeiboResponseVo;
import site.wanjiahao.gulimall.authserver.vo.RegisterUserVo;

@FeignClient("gulimall-member")
public interface MemberFeignService {


    @PostMapping("/member/member/register")
    R register(@RequestBody RegisterUserVo registerUserVo);

    @PostMapping("/member/member/login")
    R login(@RequestBody RegisterUserVo registerUserVo);

    @PostMapping("/member/member/auth2Login")
    R auth2Login(@RequestBody Auth2WeiboResponseVo body);
}
