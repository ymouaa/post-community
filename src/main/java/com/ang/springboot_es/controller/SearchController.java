package com.ang.springboot_es.controller;

import com.ang.springboot_es.entity.DiscussPost;
import com.ang.springboot_es.entity.Page;
import com.ang.springboot_es.entity.User;
import com.ang.springboot_es.service.ElasticsearchService;
import com.ang.springboot_es.service.LikeService;
import com.ang.springboot_es.service.UserService;
import com.ang.springboot_es.util.DemoConstant;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
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
public class SearchController implements DemoConstant {


    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserService userService;


    // search?keyword=dsfs&&
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model) {
        //搜索帖子
        org.springframework.data.domain.Page<DiscussPost>
                result = elasticsearchService.search(keyword, page.getCurrent() - 1, page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (result != null) {
            for (DiscussPost post : result) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                map.put("user", userService.findUserById(post.getUserId()));
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_DISCUSSPOST, post.getId()));
                discussPosts.add(map);
            }
        }
        //分页信息
        page.setPath("/search?keyword=" + keyword);
        page.setRows(result == null ? 0 : (int) result.getTotalElements());

        model.addAttribute("keyword", keyword);
        model.addAttribute("posts", discussPosts);

        return "/site/search";
    }

}
