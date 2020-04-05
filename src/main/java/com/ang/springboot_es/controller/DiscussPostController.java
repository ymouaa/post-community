package com.ang.springboot_es.controller;


import com.ang.springboot_es.entity.*;
import com.ang.springboot_es.event.EventProducer;
import com.ang.springboot_es.service.CommentService;
import com.ang.springboot_es.service.DiscussPostService;
import com.ang.springboot_es.service.LikeService;
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

import java.util.*;

@Controller
@RequestMapping(path = "/discuss")
public class DiscussPostController implements DemoConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer producer;

    /**
     * 发布帖子
     *
     * @param title
     * @param content
     * @return
     */
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return DemoUtil.getJSONString(403, "您还没有登录");
        }
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setContent(content);
        discussPost.setTitle(title);
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);


        //发帖事件
        Event event = new Event();
        event.setTopic(TOPIC_PUBLISH);
        event.setUserId(user.getId());
        event.setEntityId(discussPost.getId());
        event.setEntityType(ENTITY_TYPE_DISCUSSPOST);

        producer.fireEvent(event);


        // 报错的情况,将来统一处理.
        return DemoUtil.getJSONString(0, "发布成功");
    }

    /**
     * 帖子详情
     */
    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", discussPost);
        //帖子的作者 这里选择不用关联查询
        //之前在显示一页帖子时，也没有使用关联查询
        User user = userService.findUserById(discussPost.getUserId());
        model.addAttribute("user", user);

        //点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_DISCUSSPOST, discussPostId);

        //点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0
                : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_DISCUSSPOST, discussPostId);
        model.addAttribute("likeCount", likeCount);
        model.addAttribute("likeStatus", likeStatus);

        //评论的分页信息
        page.setRows(discussPost.getCommentCount());
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        //评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_DISCUSSPOST, discussPostId, page.getOffset(), page.getLimit());
        //评论VO列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                //评论VO
                Map<String, Object> commentVo = new HashMap<>();
                //作者
                commentVo.put("user", userService.findUserById(comment.getUserId()));
                //评论内容
                commentVo.put("comment", comment);

                //评论的点赞数
                long commentLikeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                //评论的点赞状态
                int commentLikeStatus = hostHolder.getUser() == null ? 0
                        : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());

                commentVo.put("commentLikeCount", commentLikeCount);

                commentVo.put("commentLikeStatus", commentLikeStatus);

                //回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);

                //回复VO列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();

                if (replyList != null) {
                    for (Comment reply : replyList) {
                        //回复VO
                        Map<String, Object> replyVo = new HashMap<>();
                        //作者
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        //回复
                        replyVo.put("reply", reply);
                        //回复的目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);

                        //回复的点赞数
                        long replyLikeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        //回复的点赞状态
                        int replytLikeStatus = hostHolder.getUser() == null ? 0
                                : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("replyLikeCount", replyLikeCount);
                        replyVo.put("replyLikeStatus", replytLikeStatus);

                        replyVoList.add(replyVo);
                    }
                }

                commentVo.put("replys", replyVoList);
                //回复数量
                int replyCount = commentService.findCommentsRows(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments", commentVoList);
        return "/site/discuss-detail";
    }

}
