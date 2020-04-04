package com.ang.springboot_es.controller;


import com.ang.springboot_es.entity.Event;
import com.ang.springboot_es.entity.Page;
import com.ang.springboot_es.entity.User;
import com.ang.springboot_es.event.EventProducer;
import com.ang.springboot_es.service.FollowSerivce;
import com.ang.springboot_es.service.UserService;
import com.ang.springboot_es.util.DemoConstant;
import com.ang.springboot_es.util.DemoUtil;
import com.ang.springboot_es.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@SuppressWarnings("ALL")
@Controller
public class FollowerController implements DemoConstant {

    @Autowired
    private FollowSerivce followSerivce;

    @Autowired
    private HostHolder hostHolder;


    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    /**
     * 关注
     */
    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followSerivce.follow(user.getId(), entityType, entityId);

        // 触发关注事件
        Event event = new Event().setTopic(TOPIC_FOLLOW)
                .setUserId(user.getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);

        return DemoUtil.getJSONString(0, "关注成功");
    }

    /**
     * 取关
     */
    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followSerivce.unfollow(user.getId(), entityType, entityId);

        return DemoUtil.getJSONString(0, "已取消关注");
    }


    /**
     * 关注的用户列表
     */
    @RequestMapping(path = "/followees/{userId}", method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }

        model.addAttribute("user", user);


        page.setPath("/followees/" + userId);
        page.setLimit(10);
        page.setRows((int) followSerivce.findFolloweeCount(userId, ENTITY_TYPE_USER));

        List<Map<String, Object>> userList = followSerivce.findFollowees(userId, page.getOffset(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map : userList) {

                User u = (User) map.get("user");
                boolean b = hasFollowed(u.getId());
                map.put("hasFollowed", b);
            }
        }
        model.addAttribute("users", userList);
        return "/site/followee";
    }

    /**
     * 判断当前登录用户是否关注指定用户
     */
    private boolean hasFollowed(int userId) {
        if (hostHolder.getUser() == null) {
            return false;
        }
        return followSerivce.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
    }


    /**
     * 粉丝列表
     */
    @RequestMapping(path = "/followers/{userId}", method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }

        model.addAttribute("user", user);


        page.setPath("/followers/" + userId);
        page.setLimit(10);
        page.setRows((int) followSerivce.findFollowerCount(ENTITY_TYPE_USER, userId));

        List<Map<String, Object>> userList = followSerivce.findFollowers(userId, page.getOffset(), page.getLimit());

        if (userList != null) {
            for (Map<String, Object> map : userList) {

                User u = (User) map.get("user");
                boolean b = hasFollowed(u.getId());
                map.put("hasFollowed", b);
            }
        }
        model.addAttribute("users", userList);
        return "/site/follower";
    }

}
