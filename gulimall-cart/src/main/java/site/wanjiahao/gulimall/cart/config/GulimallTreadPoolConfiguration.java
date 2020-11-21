package site.wanjiahao.gulimall.cart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@ConfigurationProperties(prefix = "gulimall.thread")
@Configuration
@Data
public class GulimallTreadPoolConfiguration {

    private Integer corePoolSize;

    private Integer maximumPoolSize;

    private Integer keepAliveTime;

    private Integer capacity;

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
