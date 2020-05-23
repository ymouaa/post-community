package com.ang.springboot_es.service;

import com.ang.springboot_es.dao.MessageMapper;
import com.ang.springboot_es.entity.Message;
import com.ang.springboot_es.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.Arrays;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;


    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<Message> findConversation(int userId, int offset, int limit) {
        return messageMapper.selectConversation(userId, offset, limit);
    }


    public int findConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }


    public int updateMessageStatus(int id, int status) {
        return messageMapper.updateMessage(Arrays.asList(id), status);
    }


    /**
     * 查询某个会话的所有消息
     */
    public List<Message> findLetter(String conversationId, int offset, int limit) {
        return messageMapper.selectMessageByConversationId(conversationId, offset, limit);
    }

    /**
     * 查询某个会话的消息数
     */
    public int findLetterCount(String conversationId) {
        return messageMapper.selectMessageCount(conversationId);
    }

    /**
     * 查询未读消息
     */
    public int findUnreadLetterCount(String conversationId, int userId) {
        return messageMapper.selectUnreadMessageCount(conversationId, userId);
    }

    /**
     * 添加私信
     */
    public int addMessage(Message message) {
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    /**
     * 读私信,就是将状态改为1啦
     */
    public int readMessage(List<Integer> ids) {
        return messageMapper.updateMessage(ids, 1);
    }

    /**
     * 查最新的通知
     */
    public Message findLatestNotice(int userId, String topic) {
        return messageMapper.selectLatestNotice(userId, topic);
    }

    /**
     * 查询通知总数
     */
    public int findNoticeCount(int userId, String topic) {
        return messageMapper.selectNoticeCount(userId, topic);
    }

    /**
     * 查询未读通知数
     */
    public int findUnreadNoticeCount(int userId, String topic) {
        return messageMapper.selectUnreadCount(userId, topic);
    }

    /**
     * 查询通知列表
     */
    public List<Message> findNotices(int userId, String topic, int offset, int limit) {
        return messageMapper.selectNotices(userId, topic, offset, limit);
    }
}
