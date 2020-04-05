package com.ang.springboot_es.controller;

import com.ang.springboot_es.entity.Comment;
import com.ang.springboot_es.entity.DiscussPost;
import com.ang.springboot_es.entity.Event;
import com.ang.springboot_es.event.EventProducer;
import com.ang.springboot_es.service.CommentService;
import com.ang.springboot_es.service.DiscussPostService;
import com.ang.springboot_es.util.DemoConstant;
import com.ang.springboot_es.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements DemoConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;


    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;


    /**
     * 回复帖子
     */
    @RequestMapping(value = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);//0代表有效的
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        // 触发评论事件
        Event event = new Event().setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", discussPostId);

        if (comment.getTargetId() != 0) {
            event.setEntityUserId(comment.getTargetId());
        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        } else if (comment.getEntityType() == ENTITY_TYPE_DISCUSSPOST) {
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(event);

        // 只有评论了贴子才触发发帖事件
        if(comment.getEntityType()==ENTITY_TYPE_DISCUSSPOST){
            event = new Event();
            event.setTopic(TOPIC_PUBLISH);
            event.setUserId(comment.getUserId());
            event.setEntityId(discussPostId);
            event.setEntityType(ENTITY_TYPE_DISCUSSPOST);
            eventProducer.fireEvent(event);
        }


        return "redirect:/discuss/detail/" + discussPostId;
    }
}

