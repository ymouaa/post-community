package com.ang.springboot_es.event;


import com.alibaba.fastjson.JSONObject;
import com.ang.springboot_es.dao.DiscussPostMapper;
import com.ang.springboot_es.entity.DiscussPost;
import com.ang.springboot_es.entity.Event;
import com.ang.springboot_es.entity.Message;
import com.ang.springboot_es.service.DiscussPostService;
import com.ang.springboot_es.service.ElasticsearchService;
import com.ang.springboot_es.service.MessageService;
import com.ang.springboot_es.util.DemoConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer implements DemoConstant {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    public DiscussPostService postService;

    @Autowired
    public ElasticsearchService elasticsearchService;

    @KafkaListener(topics = {TOPIC_LIKE, TOPIC_COMMENT, TOPIC_FOLLOW})
    public void handleCommentMessages(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空！");
            return;
        }
        // json->object
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误！");
            return;
        }
        // 站内通知
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());

        if (!event.getData().isEmpty()) {
            // k-v 的集合
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);

    }


    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handleHandlePublishMessages(ConsumerRecord record) {
        if(record==null||record.value()==null){
            logger.error("消息的内容为空！");
            return;
        }
        // json->object
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误！");
            return;
        }
        int id = event.getEntityId();
        DiscussPost post = postService.findDiscussPostById(id);
        elasticsearchService.save(post);
    }

}
