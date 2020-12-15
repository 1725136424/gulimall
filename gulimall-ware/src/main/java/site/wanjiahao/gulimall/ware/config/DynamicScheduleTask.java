package site.wanjiahao.gulimall.ware.config;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import site.wanjiahao.gulimall.ware.service.MQMessageService;

@Configuration
@EnableScheduling
public class DynamicScheduleTask implements SchedulingConfigurer {

    @Mapper
    public interface CronMapper {
        @Select("select cron from cron limit 1")
        String getCron();
    }

    @Autowired
    private CronMapper cronMapper;

    @Autowired
    private MQMessageService mqMessageService;

    /*
    * 定时扫描数据库中未发出去的消息
    * */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(() -> {
            try {
                mqMessageService.intervalSendMsg();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }, (triggerContext -> {
            String cron = cronMapper.getCron();
            if (StringUtils.isBlank(cron)) {
                cron = "0/10 * * * * ?";
            }
            return new CronTrigger(cron).nextExecutionTime(triggerContext);
        }));
    }
}
