package site.wanjiahao.gulimall.coupon;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

//@SpringBootTest
class GulimallCouponApplicationTests {

    @Test
    void contextLoads() {
        // 当前日期
        LocalDate startTime = LocalDate.now();
        // 当前日期三天之内
        LocalDate endTime = startTime.plusDays(3);
        // min
        LocalTime min = LocalTime.MIN;
        // max
        LocalTime max = LocalTime.MAX;
        LocalDateTime start = LocalDateTime.of(startTime, min);
        LocalDateTime end = LocalDateTime.of(endTime, max);
        System.out.println(start);
        System.out.println(end);
    }

}
