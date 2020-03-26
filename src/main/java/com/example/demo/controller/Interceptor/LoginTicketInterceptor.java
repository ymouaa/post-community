package com.example.demo.controller.Interceptor;


import com.example.demo.entity.LoginTicket;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import com.example.demo.util.CookieUtil;
import com.example.demo.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Enumeration;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //通过cookie得到ticket
        String ticket = CookieUtil.getValue(request, "ticket");
        if (ticket != null) {
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                User user = userService.findUserById(loginTicket.getUserId());
                //本次请求中持有用户
                hostHolder.setUser(user);
            }
        }
//        HandlerMethod handlerMethod = (HandlerMethod) handler;
//        if(!handlerMethod.getMethod().getName().equals("login")){
//            request.getRequestDispatcher("/login").forward(request,response);
//            return false;
//        }
        return true;
    }

    //模板前
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        hostHolder.clearUser();
    }
//            Enumeration<String> headerNames = request.getHeaderNames();
//        while (headerNames.hasMoreElements()){
//            String name = headerNames.nextElement();
//            Enumeration<String> headers = request.getHeaders(name);
//            if(headers!=null){
//                System.out.print(name+" : ");
//                while (headers.hasMoreElements()){
//                    String headerValue = headers.nextElement();
//                    System.out.print(headerValue+",");
//                }
//                System.out.println();
//            }
//        }
//
//
//        Enumeration<String> parameterNames = request.getParameterNames();
//        while (parameterNames.hasMoreElements()) {
//            String name = parameterNames.nextElement();
//            String[] values = request.getParameterValues(name);
//            System.out.print(name + " : ");
//            if (values != null) {
//
//                for (String string : values) {
//                    System.out.print(string + ",");
//                }
//                System.out.println();
//            }
//        }
}
