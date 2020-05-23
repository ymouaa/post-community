package com.ang.springboot_es.service;


import com.ang.springboot_es.dao.UserMapper;
import com.ang.springboot_es.entity.LoginTicket;
import com.ang.springboot_es.entity.User;
import com.ang.springboot_es.util.DemoConstant;
import com.ang.springboot_es.util.DemoUtil;
import com.ang.springboot_es.util.MailClient;
import com.ang.springboot_es.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;


@Service
public class UserService implements DemoConstant {

    @Autowired
    private UserMapper userMapper;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private RedisTemplate redisTemplate;

    //获取域名
    @Value("${demo.path.domain}")
    private String domain;


    //访问路径
    @Value("${server.servlet.context-path}")
    private String contextPath;


    public User findUserById(int id) {
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
    }

    //返回注册的结果
    //比如用户名重复
    //邮箱已注册
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }

        //验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在");
            return map;
        }

        //验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册");
            return map;
        }

        //加密密码
        //1加盐
        user.setSalt(DemoUtil.generateUUID().substring(0, 5));
        user.setPassword(DemoUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);//普通用户

        user.setStatus(0); //未激活
        user.setActivationCode(DemoUtil.generateUUID());//生成激活码
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));

        user.setCreateTime(new Date());
        userMapper.insertUser(user);
        //发邮件

        //模板
        //在templates/mail/activation.html
        Context context = new Context();      //org.thymeleaf.context;
        context.setVariable("email", user.getEmail());
        String url = domain + contextPath + "/activationcode/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        // http://localhost:8080/demo/activation/101/code
        String content = templateEngine.process("/mail/activation", context);
        // mailClient.sendMail(user.getEmail(),"激活账号",content);
        new Thread(() -> mailClient.sendMail(user.getEmail(), "激活账号", content)).start();

        return map;
    }


    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        HashMap<String, Object> map = new HashMap<>();

        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "用户名不能为空！");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        //验证用户名
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "用户名不存在！");
            return map;
        }
        //验证用户是否激活
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "账号未激活!");
            return map;
        }
        //验证密码是否正确
        if (!user.getPassword().equals(DemoUtil.md5(password + user.getSalt()))) {
            map.put("passwordMsg", "密码不正确！");
            return map;
        }

        //添加登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setStatus(0);
        loginTicket.setExpired
                (new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicket.setTicket(DemoUtil.generateUUID());
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        // 退出登录时，将redis中loginTicket的status设为1 不设置redis中的过期时间
        redisTemplate.opsForValue().set(redisKey, loginTicket);
        map.put("ticket", loginTicket.getTicket());
        return map;
    }


    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        //重复激活
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        }
        if (user.getActivationCode().equals(code)) {
            //激活用户
            userMapper.updateStatus(userId, 1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        }
        return ACTIVATION_FAILURE;
    }

    public LoginTicket findLoginTicket(String ticket) {
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        //return loginTicketMapper.selectByTicket(ticket);
    }

    // 退出登录
    public void logout(String ticket) {
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);
        //loginTicketMapper.updateTicketStatus(ticket,1);
    }


    //修改头像headerUrl
    public int updateHeader(int userId, String headerUrl) {

        //return userMapper.updateHeader(userId, headerUrl);
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;
    }


    //修改密码
    public int updatePassword(int userId, String password) {
        return userMapper.updatePassword(userId, password);
    }


    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

    /*
    查询缓存 初始化缓存 删除缓存
      1.缓存用户
      2.没有再到数据库中查
      3.修改数据时，删除缓存
    */
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        //记得aobing说过为了避免缓存雪崩 缓存的时间不设为相同
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    private User getCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }
    // 根据用户的字段来获取权限
    public Collection<? extends GrantedAuthority> getAuthority(int userId) {
        User user = this.findUserById(userId);

        List<GrantedAuthority>list =new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }

}
