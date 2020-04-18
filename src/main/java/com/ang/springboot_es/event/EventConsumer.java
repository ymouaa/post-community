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
import com.ang.springboot_es.util.DemoUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import javax.swing.text.ZoneView;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

@Component
public class EventConsumer implements DemoConstant {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    public DiscussPostService postService;

    @Autowired
    public ElasticsearchService elasticsearchService;


    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    // 生成长图的cmd
    @Value("${wk.image.command}")
    private String wkImageCmd;

    // 本地存储图片的路径
    @Value("${wk.image.storage}")
    private String wkStoragePath;


    @Value("${qiniu.ak}")
    private String accessKey;

    @Value("${qiniu.sk}")
    private String secretKey;

    @Value("${qiniu.bucket.share.name}")
    private String shareBucketname;


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
        int id = event.getEntityId();
        DiscussPost post = postService.findDiscussPostById(id);
        elasticsearchService.save(post);
    }


    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handleHandleDeleteMessages(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空！");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误！");
            return;
        }
        int id = event.getEntityId();
        elasticsearchService.deleteDiscussPost(id);
    }

    @KafkaListener(topics = {TOPIC_SHARE})
    public void handleShareMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空！");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误！");
            return;
        }
        Map<String, Object> data = event.getData();
        String htmlUrl = data.get("htmlUrl").toString();
        String filename = data.get("filename").toString();
        String suffix = data.get("suffix").toString();

        Auth auth = Auth.create(accessKey, secretKey);


        String cmd = wkImageCmd + " --quality 75 " + htmlUrl + " " + wkStoragePath +"/"+ filename + suffix;
        try {
            Runtime.getRuntime().exec(cmd);
            logger.info("生成长图");
        } catch (IOException e) {

        }
        // 上传图片到七牛云
        // 启动定时器，监视图片，一旦生成，就上传

        //什么时候停止呢？
        //执行体内，某个条件达成 用future来控制

        UploadTask task = new UploadTask(filename, suffix);
        Future future
                = taskScheduler.scheduleAtFixedRate(task, 500);
        task.setFuture(future);


    }

    class UploadTask implements Runnable {

        private String filename;

        private String suffix;

        //启动任务的返回值
        private Future future;

        // 开始时间
        private Long startTime;

        // 上传次数
        private int uploadTimes;

        UploadTask(String filename, String suffix) {
            this.filename = filename;
            this.suffix = suffix;
            startTime = System.currentTimeMillis();
        }


        public void setFuture(Future future) {
            this.future = future;
        }

        @Override
        public void run() {
            // 生成图片失败
            if (System.currentTimeMillis() - startTime > 30000) {
                logger.error("执行时间过长，终止任务" + filename);
                future.cancel(true);
                return;
            }
            // 上传失败
            if (uploadTimes >= 3) {
                logger.error("尝试上传次数过多，终止任务" + filename);
                future.cancel(true);
                return;
            }

            String path = wkStoragePath + "/" + filename + suffix;
            File file = new File(path);
            if (file.exists()) {
                logger.info(String.format("开始第%d次上传图片[%s]", ++uploadTimes, filename));
                Auth auth = Auth.create(accessKey, secretKey);
                StringMap policy = new StringMap();

                policy.put("returnBody", DemoUtil.getJSONString(0));

                String uploadToken = auth.uploadToken(shareBucketname, filename, 3600, policy);
                // 上传机房
                UploadManager manager = new UploadManager(new Configuration(Region.huanan()));
                try {

                    // 响应结果
                    Response response = manager.put(path, filename, uploadToken, null,
                            "image/" + suffix, false);
                    // 解析成Json对象
                    JSONObject json = JSONObject.parseObject(response.bodyString());

                    if (json == null
                            || json.get("code") == null
                            || !json.get("code").toString().equals("0")) {
                        logger.error(String.format("第%d次上传图片失败[%s]", uploadTimes, filename));

                    } else {

                        logger.info(String.format("第%d次上传图片成功[%s]",uploadTimes, filename));
                        future.cancel(true);

                    }
                } catch (QiniuException e) {
                    logger.error(String.format("第%d次上传图片失败[%s]", uploadTimes, filename));
                }
            } else {
                logger.info("等待图片生成[" + filename + "]");
            }

        }
    }

}
