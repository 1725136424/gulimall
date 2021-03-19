package site.wanjiahao.gulimall.seckill.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import site.wanjiahao.gulimall.seckill.interceptor.LoginInterceptor;

@Configuration
public class WebMVCConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 只要携带了auth就需要登录认证
        registry.addInterceptor(new LoginInterceptor()).addPathPatterns("/seckill/auth/**");
    }
}
