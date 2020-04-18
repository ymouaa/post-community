package com.ang.springboot_es.controller.Interceptor;


import com.ang.springboot_es.entity.User;
import com.ang.springboot_es.service.DataService;
import com.ang.springboot_es.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class DataInterceptor implements HandlerInterceptor{


    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DataService dataservice;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = request.getRemoteHost();
        dataservice.addUV(ip);


        User user = hostHolder.getUser();
        if(user!=null){
            dataservice.addDAU(user.getId());
        }
        return true;
    }
}
