package com.ang.springboot_es.controller;

import com.alibaba.fastjson.JSONObject;

import com.ang.springboot_es.entity.Message;
import com.ang.springboot_es.entity.Page;
import com.ang.springboot_es.entity.User;
import com.ang.springboot_es.service.MessageService;
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
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController implements DemoConstant {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    /**
     * 私信列表
     */
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page) {
        User user = hostHolder.getUser();
        page.setLimit(10);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));
        //会话列表
        List<Message> conversationList = messageService.findConversation(user.getId(), page.getOffset(), page.getLimit());

        List<Map<String, Object>> conversationVoList = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> conversationVo = new HashMap<>();

                //会话的未读消息数
                conversationVo.put("unreadCount", messageService.findUnreadLetterCount(message.getConversationId(), user.getId()));
                //会话
                conversationVo.put("conversation", message);

                //私信数量（感觉这个可以不用，不过页面有这个显示，还是加上吧）
                conversationVo.put("letters", messageService.findLetterCount(message.getConversationId()));

                //头像
                //显示对方的头像
                //这个会话可能是由当前登录的user最初发起的，from_id = user.id
                //也有可能是对方发起的
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                conversationVo.put("target", userService.findUserById(targetId));
                conversationVoList.add(conversationVo);
            }
        }


        int noticeUnreadCount = messageService.findUnreadNoticeCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        //总的未读消息数
        int letterUnreadCount = messageService.findUnreadLetterCount(null, user.getId());
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        model.addAttribute("conversations", conversationVoList);

        return "/site/letter";
    }


    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(Model model, Page page, @PathVariable("conversationId") String conversationId) {
        //分页信息
        page.setLimit(20);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        //私信列表
        List<Message> letterList = messageService.findLetter(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letters = new ArrayList<>();
        if (letterList != null) {
//            for (Message message : letterList) {
//                Map<String, Object> map = new HashMap<>();
//                map.put("letter", message);
//                map.put("fromUser", userService.findUserById(message.getFromId()));
//                letters.add(map);
//            }
            //新的日期显示在页面最前
            for (int i = letterList.size() - 1; i >= 0; i--) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", letterList.get(i));
                map.put("fromUser", userService.findUserById(letterList.get(i).getFromId()));
                letters.add(map);
            }

        }
        model.addAttribute("letters", letters);

        //私信的来源
        model.addAttribute("target", getLetterTarget(conversationId));

        //将未读改为已读
        List<Integer> ids = getReadList(letterList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }
        return "/site/letter-detail";
    }

    private List<Integer> getReadList(List<Message> messages) {
        List<Integer> ids = new ArrayList<>();
        if (messages != null) {
            for (Message message : messages) {
                if (message.getToId() == hostHolder.getUser().getId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }


    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        User user = hostHolder.getUser();
        int id1 = Integer.parseInt(ids[0]);
        int id2 = Integer.parseInt(ids[1]);
        int targetId = user.getId() == id1 ? id2 : id1;
        return userService.findUserById(targetId);
    }


    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String addMessage(String toName, String content) {
        User toUser = userService.findUserByName(toName);
        if (toUser == null) {
            return DemoUtil.getJSONString(1, "目标用户不存在");
        }
        Message message = new Message();
        message.setToId(toUser.getId());
        message.setFromId(hostHolder.getUser().getId());
        message.setContent(content);
        message.setCreateTime(new Date());
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        messageService.addMessage(message);
        return DemoUtil.getJSONString(0);
    }




    @RequestMapping(path = "/notice/list", method = RequestMethod.GET)
    public String getNoticeList(Model model) {
        User user = hostHolder.getUser();

        Message latestComment = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);

        if (latestComment != null) {
            Map<String, Object> noticeVo = new HashMap<>();
            noticeVo.put("message", latestComment);
            // 字符
            String content = HtmlUtils.htmlUnescape(latestComment.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            noticeVo.put("user", userService.findUserById((Integer) data.get("userId")));
            noticeVo.put("entityType", data.get("entityType"));
            noticeVo.put("entityId", data.get("entityId"));
            noticeVo.put("postId", data.get("postId"));

            noticeVo.put("count", messageService.findNoticeCount(user.getId(), TOPIC_COMMENT));

            noticeVo.put("unread", messageService.findUnreadNoticeCount(user.getId(), TOPIC_COMMENT));
            model.addAttribute("commentNotice", noticeVo);
        }


        Message latestLike = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
        if (latestLike != null) {
            Map<String, Object> noticeVo = new HashMap<>();
            noticeVo.put("message", latestLike);
            // 字符
            String content = HtmlUtils.htmlUnescape(latestLike.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            noticeVo.put("user", userService.findUserById((Integer) data.get("userId")));
            noticeVo.put("entityType", data.get("entityType"));
            noticeVo.put("entityId", data.get("entityId"));
            noticeVo.put("postId", data.get("postId"));

            noticeVo.put("count", messageService.findNoticeCount(user.getId(), TOPIC_LIKE));

            noticeVo.put("unread", messageService.findUnreadNoticeCount(user.getId(), TOPIC_LIKE));
            model.addAttribute("likeNotice", noticeVo);
        }


        Message latestFollow = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        if (latestFollow != null) {
            Map<String, Object> noticeVo = new HashMap<>();
            noticeVo.put("message", latestFollow);
            // 字符
            String content = HtmlUtils.htmlUnescape(latestFollow.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            noticeVo.put("user", userService.findUserById((Integer) data.get("userId")));
            noticeVo.put("entityType", data.get("entityType"));
            noticeVo.put("entityId", data.get("entityId"));
            noticeVo.put("count", messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW));

            noticeVo.put("unread", messageService.findUnreadNoticeCount(user.getId(), TOPIC_FOLLOW));
            model.addAttribute("followNotice", noticeVo);
        }

        // 查询未读消息
        // 未读的私信数量
        int letterUnreadCount = messageService.findUnreadLetterCount(null, user.getId());
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        // 未读的通知数量
        int noticeUnreadCount = messageService.findUnreadNoticeCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);


        return "/site/notice";
    }


    @RequestMapping(path = "/notice/detail/{topic}", method = RequestMethod.GET)
    public String getNotices(Model model, @PathVariable("topic") String topic, Page page) {
        User user = hostHolder.getUser();
        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> noticeVo = new ArrayList<>();
        if (noticeList != null) {
            for (Message notice : noticeList) {
                Map<String, Object> map = new HashMap<>();
                //通知
                map.put("notice", notice);
                //内容
                String content = HtmlUtils.htmlUnescape(notice.getContent());

                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.findUserById((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));

                //通知的作者
                map.put("fromUser", userService.findUserById(notice.getFromId()));

                noticeVo.add(map);
            }
        }
        model.addAttribute("notices", noticeVo);

        // 设置已读
        List<Integer> ids = getReadList(noticeList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "/site/notice-detail";
    }


    // 删除消息
    @RequestMapping(path = "/message/delete", method = RequestMethod.POST)
    @ResponseBody
    public String deleteMessage(int msgId) {
        messageService.updateMessageStatus(msgId, 2);
        return DemoUtil.getJSONString(0);
    }
}
