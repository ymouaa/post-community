package com.example.demo.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

@Configuration  //配置类
public class AlphaConfig {


    /*
    想把SimpleDataFormat装到容器
     */
    @Bean
    public SimpleDateFormat simpleDateFormat (){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

}
