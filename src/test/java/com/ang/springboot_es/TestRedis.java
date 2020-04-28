package com.ang.springboot_es;

import com.ang.springboot_es.entity.DiscussPost;
import com.ang.springboot_es.service.DiscussPostService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = SpringbootEsApplication.class)
public class TestRedis {

    @Autowired
    private RedisTemplate redisTemplate;


    @Autowired
    private DiscussPostService discussPostService;


    @Test
    public void testCache(){

        List<DiscussPost> posts = discussPostService.findDiscussPosts(0, 0, 1, 1);
        for (DiscussPost post : posts) {
            System.out.println(post);
        }
    }

}
