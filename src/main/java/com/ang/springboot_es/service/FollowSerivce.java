package com.ang.springboot_es.service;

import com.ang.springboot_es.entity.User;
import com.ang.springboot_es.util.DemoConstant;
import com.ang.springboot_es.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowSerivce implements DemoConstant {


    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;


    public void follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Nullable
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);


                operations.multi();

                operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());

                operations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());

                return operations.exec();
            }
        });
    }

    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Nullable
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                operations.multi();

                operations.opsForZSet().remove(followeeKey, entityId);
                operations.opsForZSet().remove(followerKey, userId);


                return operations.exec();
            }
        });
    }


    //查询关注的实体的数量
    public long findFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    //查询实体的粉丝数
    public long findFollowerCount(int entitytype, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entitytype, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    //查询当前用户是否关注该实体
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    // 查询用户的关注的人
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        //分页
        //redis里面已经按日期排序了
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit);
        if (targetIds == null) {
            return null;
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer id : targetIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(id);
            map.put("user", user);

            Double score = redisTemplate.opsForZSet().score(followeeKey, id);
            map.put("followTime", new Date(score.longValue()));
//            boolean b = hasFollowed(userId, ENTITY_TYPE_USER, id);
//            map.put("hasFollowed",b);  在视图层判断 使用hostHolder 业务层竟然没有注入这个，算一点减少耦合吧:(
            list.add(map);
        }

        return list;
    }

    // 查询用户的粉丝
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit) {
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit);

        if (targetIds == null) {
            return null;
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer id : targetIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(id);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(followerKey, id);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }

        return list;
    }

}
