package com.example.demo.service;


import com.example.demo.dao.UserMapper;
import com.example.demo.entity.User;
import com.example.demo.util.DemoUtil;
import com.example.demo.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    //获取域名
    @Value("${demo.path.domain}")
    private String domain;


    //项目路径
    @Value("${server.servlet.context-path}")
    private String contextPath;


    public User findUserById(int id){
        return userMapper.selectById(id);
    }

    //返回注册的结果
    //比如用户名重复
    //邮箱已注册
    //。。。。
    public Map<String,Object> register(User user){
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if(user==null){
            throw new IllegalArgumentException("参数不能为空");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空");
            return map;
        }

        //验证账号
        User u=userMapper.selectByName(user.getUsername());
        if(u!=null){
            map.put("usernameMsg","该账号已存在");
            return map;
        }

        //验证邮箱
        u=userMapper.selectByEmail(user.getEmail());
        if(u!=null){
            map.put("emailMsg","该邮箱已被注册");
            return map;
        }

        //加密密码
        //1加盐
        user.setSalt(DemoUtil.generateUUID().substring(0,5));
        user.setPassword(DemoUtil.md5(user.getPassword()+user.getSalt()));
        user.setType(0);//普通用户

        user.setStatus(0); //未激活
        user.setActivationCode(DemoUtil.generateUUID());//生成激活码
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));

        user.setCreateTime(new Date());
        //入库
        userMapper.insertUser(user);
        //发邮件啦

        //模板是谁？
        //需要什么参数
        //在templates/mail/activation.html


        Context context=new Context();      //org.thymeleaf.context;

        context.setVariable("email",user.getEmail() );

        String url=domain+contextPath+"/activationcode/"+user.getId()+"/"+user.getActivationCode();

        context.setVariable("url",url);

//http://localhost:8080/demo/activation/101/code
        String content=templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(),"激活账号",content);
        return map;
    }




}
