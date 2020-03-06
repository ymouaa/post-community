package com.example.demo.util;

public interface DemoConstant {


    /**
     *
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;

    /**
     *
     * 激活失败
     */
    int ACTIVATION_FAILURE=2;


    /**
     * 默认状态的登录凭证超时时间
     */
    int DEFALUT_EXPIRED_SECONDS=3600*12;

    /**
     * 记住状态的超时时间
     */
    int REMEMBER_EXPIRED_SECONDS=3600*24*100;


    /**
     * 评论的类型: 回复帖子的评论 1
     */
    int ENTITY_TYPE_DISCUSSPOST = 1;


    /**
     * 评论的类型: 回复评论的评论 2
     */
    int ENTITY_TYPE_COMMENT = 2;
}
