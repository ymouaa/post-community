package com.ang.springboot_es.controller;



import com.ang.springboot_es.entity.Event;
import com.ang.springboot_es.entity.User;
import com.ang.springboot_es.event.EventProducer;
import com.ang.springboot_es.service.CommentService;
import com.ang.springboot_es.service.DiscussPostService;
import com.ang.springboot_es.service.LikeService;
import com.ang.springboot_es.util.DemoConstant;
import com.ang.springboot_es.util.DemoUtil;
import com.ang.springboot_es.util.HostHolder;
import com.ang.springboot_es.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements DemoConstant {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId) {
        User user = hostHolder.getUser();

        // 点赞
        likeService.like(user.getId(), entityType, entityId, entityUserId);


        long likeCount = likeService.findEntityLikeCount(entityType, entityId);

        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        if (likeStatus == 1) {
            // 触发点赞事件 自己赞自己不发通知
            if(user.getId()!=entityUserId){
                Event event = new Event().setTopic(TOPIC_LIKE)
                        .setUserId(user.getId())
                        .setEntityType(entityType)
                        .setEntityId(entityId)
                        .setEntityUserId(entityUserId)
                        // 不管是点赞的是帖子还是评论 都可以跳到帖子的详情页面
                        .setData("postId", postId);

                eventProducer.fireEvent(event);
            }
        }


        if(entityType==ENTITY_TYPE_DISCUSSPOST){
            String redisKey= RedisKeyUtil.getPostKey();
            redisTemplate.opsForSet().add(redisKey,postId);
        }

        return DemoUtil.getJSONString(0, null, map);

    }

}
