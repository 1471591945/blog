package com.lzp.interceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author LZP
 * @Date 2021/5/3 10:51
 * @Version 1.0
 *
 * 配置类
 * 加上@Configuration，SpringBoot才会认为该类是一个配置类，才会去加载它
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置视图解析器
     * @param registry
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/admin/main").setViewName("/admin/index");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin")
                .excludePathPatterns("/admin/login");
    }
}
