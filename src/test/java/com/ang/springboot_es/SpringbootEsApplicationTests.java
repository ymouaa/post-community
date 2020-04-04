//package com.ang.springboot_es;
//
//import com.ang.springboot_es.dao.DiscussPostMapper;
//import com.ang.springboot_es.entity.DiscussPost;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.util.List;
//
//
//@RunWith(SpringRunner.class)
//@SpringBootTest
//@ContextConfiguration(classes = SpringBootApplication.class)
//public class SpringbootEsApplicationTests {
//
//    @Test
//    public void contextLoads() {
//    }
//
//
//    @Autowired
//    private DiscussPostMapper postMapper;
//
//    @Test
//    public void testMapper(){
//        List<DiscussPost> list = postMapper.selectDiscussPost(159, 0, Integer.MAX_VALUE);
//        for (DiscussPost post : list) {
//            System.out.println(post);
//        }
//    }
//}
