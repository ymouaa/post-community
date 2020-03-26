package com.example.demo;


import com.example.demo.entity.LoginTicket;
import com.example.demo.entity.User;
import com.example.demo.util.DemoUtil;
import com.example.demo.util.RedisKeyUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.lang.Nullable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = DemoApplication.class)
public class TestRedis {

    @Autowired
    private RedisTemplate<String, Object> template;

    @Test
    public void testStrings() {
        String redisKey = "test:count";

        template.opsForValue().set(redisKey, 1);

        System.out.println(template.opsForValue().get(redisKey));
        System.out.println(template.opsForValue().increment(redisKey));
        System.out.println(template.opsForValue().decrement(redisKey));
    }

    @Test
    public void testHash() {
        String redisKey = "test:user";

        template.opsForHash().put(redisKey, "id", 1);
        template.opsForHash().put(redisKey, "username", "zhang");
    }

    @Test
    public void testLists() {

        String redisKey = "test:list";


        template.opsForList().leftPush(redisKey, 101);
        template.opsForList().leftPush(redisKey, 102);
        template.opsForList().leftPush(redisKey, 103);
        template.opsForList().leftPush(redisKey, 104);

        System.out.println(template.opsForList().size(redisKey));
        System.out.println(template.opsForList().index(redisKey, 1));
        System.out.println(template.opsForList().range(redisKey, 0, 4));

        System.out.println(template.opsForList().rightPop(redisKey));
        System.out.println(template.opsForList().rightPop(redisKey));
        System.out.println(template.opsForList().rightPop(redisKey));
        System.out.println(template.opsForList().rightPop(redisKey));

    }


    @Test
    public void testSets() {
        String redisKey = "test:teachers";

        template.opsForSet().add(redisKey, "刘备", "张飞", "关羽");
        System.out.println(template.opsForSet().size(redisKey));
        System.out.println(template.opsForSet().pop(redisKey));
        System.out.println(template.opsForSet().members(redisKey));

    }

    @Test
    public void testOp() {
        String redisKey = "test:count";
        BoundValueOperations operations = template.boundValueOps(redisKey);

        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();

        System.out.println(operations.get());


    }


    @Test
    public void testSortedSet() {
        String redisKey = "test:students";
        template.opsForZSet().add(redisKey, "aaa", 10);
        template.opsForZSet().add(redisKey, "bbb", 30);
        template.opsForZSet().add(redisKey, "ccc", 11);
        template.opsForZSet().add(redisKey, "ddd", 20);
        template.opsForZSet().add(redisKey, "eee", 34);
        Set<Object> set = template.opsForZSet().range(redisKey, 0, 5);
        System.out.println(set);
    }

    //redis的事务是有一个队列，提交的时候，统一执行，所以查询有可能会被阻塞不会立刻返回结果，不要在事务中查询
    @Test
    public void testTransaction() {
        Object object = template.execute(new SessionCallback<Object>() {

            @Nullable
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String redisKey = "test:tx";
                //开启事务
                redisOperations.multi();
                redisOperations.opsForSet().add(redisKey, "aaa");
                redisOperations.opsForSet().add(redisKey, "aaaa");
                redisOperations.opsForSet().add(redisKey, "aaaaa");
                redisOperations.opsForSet().add(redisKey, "aaaaaa");
                redisOperations.opsForSet().add(redisKey, "aaaaaaa");
                System.out.println(redisOperations.opsForSet().members(redisKey));
                System.out.println(redisOperations.opsForSet().members(redisKey));
                System.out.println(redisOperations.opsForSet().members(redisKey));
                System.out.println(redisOperations.opsForSet().members(redisKey));
                System.out.println(redisOperations.opsForSet().members(redisKey));
                //提交事务
                return redisOperations.exec();
            }
        });
        System.out.println(object);
    }


    //    @Test
    public void testFormat() {
//        User user = new User();
//        user.setPassword("123");
//        user.setUsername("aaaa");
//        user.setId(2);
//        template.opsForValue().set("test:user:"+user.getId(),user);
//        template.opsForHash().put("hash:users",user.getId()+"",user);
    }

    @Test
    public void testTicketStore() {
        LoginTicket ticket = new LoginTicket();
        ticket.setExpired(new Date());
        ticket.setStatus(0);
        ticket.setId(2);
        ticket.setUserId(321);
        ticket.setTicket(DemoUtil.generateUUID());
        String redisKey = RedisKeyUtil.getTicketKey(ticket.getTicket());
        template.opsForValue().set(redisKey, ticket, 20, TimeUnit.SECONDS);
        ticket = (LoginTicket) template.opsForValue().get(redisKey);
        System.out.println(ticket);
    }
}
