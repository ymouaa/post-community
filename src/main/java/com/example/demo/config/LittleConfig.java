package com.example.demo.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

public class LittleConfig extends WebMvcConfigurerAdapter{
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        /*将所有  /static/* 下的访问都映射到classpath:/static/ 目录下
         */
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
    }


}
