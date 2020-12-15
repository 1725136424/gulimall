package site.wanjiahao.gulimall.ware.config;

import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

/**
 * 基于原生的静态定时任务, 当前定时任务为单线程，下一个任务的执行时间，受上一个任务的时间影响
 */
/*@Configuration
@EnableScheduling // 开启定时任务*/
public class StaticScheduleTask {

    /*
    * 间隔5s后执行
    * Cron表达式参数分别表示：
      秒（0~59） 例如0/5表示每5秒
      分（0~59）
      时（0~23）
      日（0~31）的某天，需计算
      月（0~11）
      周几（ 可填1-7 或 SUN/MON/TUE/WED/THU/FRI/SAT）
      表达式为:  0/5 * * * * ?
    *
    * */
    @Scheduled(cron = "0/5 * * * * ?")
    private void intervalTask() {
        System.out.println("执行当前定时任务:" + LocalDateTime.now());
    }
}
