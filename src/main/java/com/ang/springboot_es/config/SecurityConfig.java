package com.ang.springboot_es.config;


import com.ang.springboot_es.util.DemoConstant;
import com.ang.springboot_es.util.DemoUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements DemoConstant {


    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resourecs/**/*");
    }

    // 授权
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        /*http.authorizeRequests()
                .antMatchers(
                        "/comment/add/**"
                        , "/discuss/add"
                        , "/like"
                        , "/follow"
                        , "/unfollow"
                        , "/user/upload"
                        , "/user/password"
                        , "/user/setting"
                        , "/notice/**"
                        , "/letter/**"
                ).hasAnyAuthority(AUTHORITY_USER, AUTHORITY_ADMIN, AUTHORITY_MODERATOR)zz
                .anyRequest().permitAll();
        */
        http.authorizeRequests()
                .antMatchers(
                        "/comment/add/**"
                        , "/discuss/add"
                        , "/like"
                        , "/follow"
                        , "/unfollow"
                        , "/user/upload"
                        , "/user/password"
                        , "/user/setting"
                        , "/notice/**"
                        , "/letter/**"
                ).hasAnyAuthority(AUTHORITY_USER, AUTHORITY_ADMIN, AUTHORITY_MODERATOR)
                .antMatchers(
                        "/discuss/top"
                        ,"/discuss/wonderful"
                ).hasAnyAuthority(AUTHORITY_MODERATOR)
                .antMatchers(
                        "/discuss/delete"
                        ,"/data/**"
                ).hasAnyAuthority(AUTHORITY_ADMIN)
                .anyRequest().permitAll();
        // 权限不够时的处理
        // 普通请求 异步请求
        http.exceptionHandling()
                .authenticationEntryPoint(
                        //没有登陆
                        new AuthenticationEntryPoint() {
                            @Override
                            public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
                                String xRequestWith = request.getHeader("x-requested-with");
                                if (xRequestWith != null && xRequestWith.equals("XMLHttpRequest")) {
                                    response.setContentType("application/plain;charset=utf-8");
                                    PrintWriter writer = response.getWriter();
                                    String msg = DemoUtil.getJSONString(403, "你还没有登录哦");
                                    writer.print(msg);
                                } else {
                                    response.sendRedirect(request.getContextPath() + "/login");
                                }
                            }
                        }
                )
                .accessDeniedHandler(
                        // 没有权限
                        new AccessDeniedHandler() {
                            @Override
                            public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
                                String xRequestWith = request.getHeader("x-requested-with");
                                if (xRequestWith != null && xRequestWith.equals("XMLHttpRequest")) {
                                    response.setContentType("application/plain;charset=utf-8");
                                    PrintWriter writer = response.getWriter();
                                    String msg = DemoUtil.getJSONString(403, "您没有访问该功能的权限！");
                                    writer.print(msg);
                                } else {
                                    response.sendRedirect(request.getContextPath() + "/denied");
                                }
                            }
                        }
                );

        // Security默认会拦截/logout请求
        // 我们需要覆盖，才能执行我们自己的
        http.logout().logoutUrl("/securitylogout");

        // 关闭csrf()
        http.csrf().disable();
    }


}
