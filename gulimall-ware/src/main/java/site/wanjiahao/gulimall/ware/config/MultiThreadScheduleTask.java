package site.wanjiahao.gulimall.ware.config;

import org.springframework.scheduling.annotation.Scheduled;

/*@Component
@EnableScheduling // 开启定时任务
@EnableAsync // 开启多线程执行异步任务*/
public class MultiThreadScheduleTask {

    @Scheduled(fixedDelay = 1000)
    public void first() {
        System.out.println("第一个任务执行");
    }

    @Scheduled(fixedDelay = 2000)
    public void second() {
        System.out.println("第二个任务执行");
    }
}
