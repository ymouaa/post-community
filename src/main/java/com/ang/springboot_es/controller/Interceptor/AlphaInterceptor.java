package com.ang.springboot_es.controller.Interceptor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AlphaInterceptor implements HandlerInterceptor {

    public static final Logger logger = LoggerFactory.getLogger(AlphaInterceptor.class);

    //在controller前执行
    //返回值false 不往下执行
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        logger.debug(this.getClass().getName() + "  preHandle  " + handler.toString());
        return true;
    }


    //在controller之后执行
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        logger.debug(this.getClass().getName() + "  postHandle " + handler.toString());
    }
    //模板引擎的之后执行
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        logger.debug(this.getClass().getName() + "  afterCompletion " + handler.toString());
    }
}
