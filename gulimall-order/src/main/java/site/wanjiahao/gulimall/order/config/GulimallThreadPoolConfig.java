package site.wanjiahao.gulimall.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@ConfigurationProperties(prefix = "gulimall.thread")
@Configuration
@Data
public class GulimallThreadPoolConfig {

    private int corePoolSize;

    private int maximumPoolSize;

    private int keepAliveTime;

    private int capacity;

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        return new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(capacity),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.DiscardPolicy());
    }
}
