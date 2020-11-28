package site.wanjiahao.gulimall.order.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class GulimallFeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            // 利用RequestContextHolder(请求上下文保持器)来获取当前线程的请求
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                HttpServletRequest request = requestAttributes.getRequest();
                String cookie = request.getHeader("Cookie");
                // 设置请求头中的Cookie
                template.header("Cookie", cookie);
            }
        };
    }
}
