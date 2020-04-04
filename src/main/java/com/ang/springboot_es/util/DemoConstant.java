package com.ang.springboot_es.util;

public interface DemoConstant {


    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    int ACTIVATION_FAILURE = 2;


    /**
     * 默认状态的登录凭证超时时间
     */
    int DEFALUT_EXPIRED_SECONDS = 3600 * 12;

    /**
     * 记住状态的超时时间
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;


    /**
     * 实体类型：评论的类型: 回复帖子的评论 1
     */
    int ENTITY_TYPE_DISCUSSPOST = 1;


    /**
     * 实体类型：评论的类型: 回复评论的评论 2
     */
    int ENTITY_TYPE_COMMENT = 2;

    /**
     * 实体类型：用户 3
     */

    int ENTITY_TYPE_USER = 3;


    /**
     * 主题：点赞
     */
    String TOPIC_LIKE = "like";

    /**
     * 主题：评论
     */
    String TOPIC_COMMENT = "comment";

    /**
     * 主题：关注
     */

    String TOPIC_FOLLOW = "follow";


    /**
     * 系统用户ID
     */
    int SYSTEM_USER_ID = 1;
}