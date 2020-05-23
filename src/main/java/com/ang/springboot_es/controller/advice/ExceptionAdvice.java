package com.ang.springboot_es.controller.advice;

import com.ang.springboot_es.util.DemoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
/**
 * 统一异常处理
 *
 * < @ControllerAdvice
 * - 用于修饰类，表示该类是Controller的全局配置类。在此类中，可以对Controller进行如下三种全局配置： 异常处理方案、绑定数据方案、绑定参数方案。
 * < @ExceptionHandler
 * - 用于修饰方法，该方法会在Controller出现异常后被调用，用于处理捕获到的异常。
 * < @ModelAttribute
 * - 用于修饰方法，该方法会在Controller方法执行前被调用，用于为Model对象绑定参数。
 * < @DataBinder
 * - 用于修饰方法，该方法会在Controller方法执行前被调用，用于绑定参数的转换器。
 **/

//只扫描加了Controller注解的类
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler(value = {Exception.class})
    public void error(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("服务器发生异常: " + e.getMessage());
        for (StackTraceElement element : e.getStackTrace()) {
            logger.error(element.toString());
        }

        String xRequestedWith = request.getHeader("x-requested-with");
        if ("XMLHttpRequest".equals(xRequestedWith)) {
            //异步请求
            //返回Json
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = null;
            writer = response.getWriter();

            writer.write(DemoUtil.getJSONString(1, "服务器异常!"));
        } else {
            //普通请求
            //跳转错误页面
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }


}
