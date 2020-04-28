package com.ang.springboot_es.config;

import com.ang.springboot_es.controller.Interceptor.AlphaInterceptor;
import com.ang.springboot_es.controller.Interceptor.DataInterceptor;
import com.ang.springboot_es.controller.Interceptor.LoginTicketInterceptor;
import com.ang.springboot_es.controller.Interceptor.MessageInterceptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMcvConfig implements WebMvcConfigurer {

    @Autowired
    private AlphaInterceptor alphaInterceptor;

    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

    @Autowired
    private DataInterceptor dataInterceptor;

    @Autowired
    private MessageInterceptor messageInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //不拦截静态资源
        registry.addInterceptor(alphaInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpeg", "/**/*.jpg")
                .addPathPatterns("/register", "/login");
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns(
                        "/**/*.css"
                        , "/**/*.js"
                        , "/**/*.png"
                        , "/**/*.jpeg"
                        , "/**/*.jpg");
        registry.addInterceptor(dataInterceptor)
                .excludePathPatterns(
                        "/**/*.css"
                        , "/**/*.js"
                        , "/**/*.png"
                        , "/**/*.jpeg"
                        , "/**/*.jpg");

        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns(
                        "/**/*.css"
                        , "/**/*.js"
                        , "/**/*.png"
                        , "/**/*.jpeg"
                        , "/**/*.jpg");
    }



}
