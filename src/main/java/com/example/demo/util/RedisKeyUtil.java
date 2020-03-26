package com.example.demo.util;


public class RedisKeyUtil {

    private static final String SPLIT = ":";

    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    private static final String PREFIX_LIKE_COUNT = "like:count";

    private static final String PREFIX_FOLLOWEE = "followee";

    private static final String PREFIX_FOLLOWER = "follower";

    // 验证码前缀
    private static final String PREFIX_KAPTCHA = "Kaptcha";

    // 登录凭证前缀
    private static final String PREIX_TICKET = "ticket";

    // 用户
    private static final String PREFIX_USER = "user";

    //某个实体的赞
    //like:entity:entityType:entityId --> set(userId)   (如果我想看到谁赞的我,所以不用一个整数)
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    //某个用户的赞
    //like:count:userId --> string
    public static String getLikeCountKey(int userId) {
        return PREFIX_LIKE_COUNT + SPLIT + userId;
    }


    //某个用户关注的实体（分类）
    // followee:userId:entityType --> zset(entityId,now)
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }


    // 某个实体拥有的粉丝
    // follower:entityType:entityId --> zset(userId,now)
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }


    // 验证码输入某个用户 但该用户没有登录 用服务器生成的字符串来标识
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    // 登录凭证
    public static String getTicketKey(String ticket) {
        return PREIX_TICKET + SPLIT + ticket;
    }


    // 用户
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }


}
