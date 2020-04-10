package com.ang.springboot_es.controller.Interceptor;


import com.ang.springboot_es.entity.LoginTicket;
import com.ang.springboot_es.entity.User;
import com.ang.springboot_es.service.UserService;
import com.ang.springboot_es.util.CookieUtil;
import com.ang.springboot_es.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //通过cookie得到ticket
        StringBuffer url = request.getRequestURL();
        System.out.println(url);
        String ticket = CookieUtil.getValue(request, "ticket");
        if (ticket != null) {
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                User user = userService.findUserById(loginTicket.getUserId());
                //本次请求中持有用户
                hostHolder.setUser(user);

                // 构建用户认证的结果，并存入SecurityContext，以便于Security进行授权
                Authentication authentication=new UsernamePasswordAuthenticationToken(
                        user,user.getPassword(),userService.getAuthority(user.getId())
                );
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }
        }
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
        SecurityContextHolder.clearContext();
        hostHolder.clearUser();
    }

}
