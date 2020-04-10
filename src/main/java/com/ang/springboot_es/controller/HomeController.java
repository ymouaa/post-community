package com.ang.springboot_es.controller;



import com.ang.springboot_es.entity.DiscussPost;
import com.ang.springboot_es.entity.Page;
import com.ang.springboot_es.entity.User;
import com.ang.springboot_es.service.DiscussPostService;
import com.ang.springboot_es.service.LikeService;
import com.ang.springboot_es.service.UserService;
import com.ang.springboot_es.util.DemoConstant;
import com.ang.springboot_es.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements DemoConstant {

    @Autowired
    private UserService userService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;


    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/error", method = RequestMethod.GET)
    public String getErrorPage() {
        return "/error/500";
    }

    @RequestMapping(path = "/denied",method = RequestMethod.GET)
    public String getDeniedPage(){
        return "/error/404";
    }

    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page) {
        //page自动放到Model中 thymeleaf中可以直接用
        //springmvc会自动实例化Model和Page，并将Page注入Model
        //所以在thymeleaf中可以直接访问Page对象中的数据
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");
        List<DiscussPost> list = discussPostService.findDicussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);

                //帖子的点赞
                long postLikeCount = likeService.findEntityLikeCount(ENTITY_TYPE_DISCUSSPOST, post.getId());

                //帖子的点赞状态
                int postLikeStatus = hostHolder.getUser() == null ? 0
                        : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_DISCUSSPOST, post.getId());

                map.put("postLikeCount", postLikeCount);
                map.put("postLikeStatus", postLikeStatus);

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        return "/index";
    }


}