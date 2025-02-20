package com.lyc.config;

import com.lyc.interceptor.LoginInterceptor;
import com.lyc.interceptor.RefreshTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @ClassName SpringMvcConfig
 * @Description TODO SpringMvc配置类
 * @Author LuKey_C
 * @Date 2025/2/20 13:52
 * @Version 1.0
 */
@Configuration
public class SpringMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Autowired
    private RefreshTokenInterceptor refreshTokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .excludePathPatterns(
                        "/shop/**",
                        "/voucher/**",
                        "/shop-type/**",
                        "/user/code",
                        "/user/login",
                        "/blog/hot"
                );
        registry.addInterceptor(refreshTokenInterceptor);
    }
}
