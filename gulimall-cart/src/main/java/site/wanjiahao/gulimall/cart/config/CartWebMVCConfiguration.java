package site.wanjiahao.gulimall.cart.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import site.wanjiahao.gulimall.cart.interceptor.ThreadLocalInterceptor;

@Configuration
public class CartWebMVCConfiguration implements WebMvcConfigurer {

    // 添加本地线程拦截器映射
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ThreadLocalInterceptor()).addPathPatterns("/**");
    }
}
