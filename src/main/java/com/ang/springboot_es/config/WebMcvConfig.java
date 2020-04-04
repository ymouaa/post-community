package com.ang.springboot_es.config;

import com.ang.springboot_es.controller.Interceptor.AlphaInterceptor;
import com.ang.springboot_es.controller.Interceptor.LoginTicketInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMcvConfig implements WebMvcConfigurer {

    @Autowired
    private AlphaInterceptor alphaInterceptor;

    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //不拦截静态资源
        registry.addInterceptor(alphaInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpeg","/**/*.jpg")
                .addPathPatterns("/register","/login");
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns(
                        "/**/*.css"
                        , "/**/*.js"
                        , "/**/*.png"
                        , "/**/*.jpeg"
                        , "/**/*.jpg");
    }

}
