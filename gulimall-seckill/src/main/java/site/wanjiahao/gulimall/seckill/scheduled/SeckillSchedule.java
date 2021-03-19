package site.wanjiahao.gulimall.seckill.scheduled;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import site.wanjiahao.gulimall.seckill.service.SeckillService;

/**
 * TaskSchedulingAutoConfiguration 定时任务自动配置类
 * TaskExecutionAutoConfiguration 为线程池的自动配置
 */
/**
 * 在springBoot中只有6位 不支持年份
 * 秒 分 时 日 月 周
 * 在cron中 1为周末 所以5位星期六
 * 在springBoot中 1-7 就是对应 星期几
 *
 * 在SpringBoot中要想使得方法为异步的，可以使用ComputerFuture 或者使用异步任务开启
 *  1.@EnableAsync
 *  2.@Async 异步执行的方法
 */
@EnableScheduling
@Component
public class SeckillSchedule {

    @Autowired
    private SeckillService seckillService;

    /**
     * 凌晨三点执行此定时方法
     */
    @Scheduled(cron = "0 * * * * ?")
    public void uploadTask() {

        /**
         * 数据库已经设置幂等字段，可以不用过多考虑幂等
         */
        seckillService.uploadThreeProduct();

    }

}
