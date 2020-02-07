package com.example.demo;


import com.example.demo.dao.DiscussPostMapper;
import com.example.demo.dao.UserMapper;
import com.example.demo.entity.DiscussPost;
import com.example.demo.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;


@SpringBootTest
@ContextConfiguration(classes=DemoApplication.class)
public class TestMapper {

    @Autowired
    private UserMapper userMapper;


    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Test
    public void testSelectUser(){
        System.out.println(userMapper);

        User user = userMapper.selectById(1);
        //ser user = userMapper.selectById(2);
        System.out.println(user);
    }
    @Test
    public void testInsertUser(){
        User user = new User();
        user.setUsername("王五");
        user.setPassword("123");
        user.setEmail("12412@qe.com");
        user.setSalt("1241");
        user.setCreateTime(new Date());
        user.setType(1);
        user.setStatus(0);
        user.setHeaderUrl(null);
        user.setActivationCode("string");
        userMapper.insertUser(user);
    }
    @Test
    public void testUpdateUser(){
        userMapper.updateHeader(150,"www.baidu.com");
        userMapper.updatePassword(150,"3838438");
        userMapper.updateStatus(150,1);
    }

    @Test
    public void testSelectPosts(){
        List<DiscussPost> posts = discussPostMapper.selectDiscussPost(101, 0, 10);
        for (DiscussPost post:posts){
            System.out.println(post);
        }
       // discussPostMapper.selectDiscussPostRows(0);
        //System.out.println("=================");
        //System.out.println(discussPostMapper.selectDiscussPostRows(0));
    }
}
