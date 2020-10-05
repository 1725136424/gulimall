package site.wanjiahao.gulimall.member;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.gulimall.member.feign.CouponFeignService;

@SpringBootTest
class GulimallMemberApplicationTests {

    @Autowired
    private CouponFeignService couponFeignService;

    @Test
    void contextLoads() {
        R test = couponFeignService.test();
        System.out.println(test);
    }

}
