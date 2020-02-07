package com.example.demo.service;


import com.example.demo.dao.AlphaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service  //业务组件
//@Scope("prototye")  //默认单例bean 加上后不是
public class AlphaService {

    public AlphaService(){
        System.out.println("实例化");

    }

    @Autowired
    private AlphaDao alphaDao;

    @PostConstruct
    public void init(){
        System.out.println(" 初始化 service");
    }


    @PreDestroy
    public void destroy(){
        System.out.println("销毁service");
    }


}
