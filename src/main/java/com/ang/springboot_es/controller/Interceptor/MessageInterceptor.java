package com.ang.springboot_es.controller.Interceptor;

import com.ang.springboot_es.service.MessageService;
import com.ang.springboot_es.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class MessageInterceptor implements HandlerInterceptor {



    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        if(hostHolder.getUser()!=null&&modelAndView!=null){
            Map<String, Object> model = modelAndView.getModel();
            if(!model.containsKey("letterUnreadCount")&&!model.containsKey("noticeUnreadCount")){
                int unreadNoticeCount = messageService.findUnreadNoticeCount(hostHolder.getUser().getId(), null);
                int unreadLetterCount = messageService.findUnreadLetterCount(null, hostHolder.getUser().getId());
                modelAndView.addObject("totalCount",unreadLetterCount+unreadNoticeCount);
            }else{
                Integer unreadLetterCount = (Integer) model.get("letterUnreadCount");
                Integer unreadNoticeCount = (Integer) model.get("noticeUnreadCount");
                modelAndView.addObject("totalCount",unreadLetterCount+unreadNoticeCount);
            }
        }
    }
}
