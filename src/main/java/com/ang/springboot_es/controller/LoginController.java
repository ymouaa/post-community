package com.ang.springboot_es.controller;


import com.ang.springboot_es.entity.User;
import com.ang.springboot_es.service.UserService;
import com.ang.springboot_es.util.DemoConstant;
import com.ang.springboot_es.util.DemoUtil;
import com.ang.springboot_es.util.RedisKeyUtil;
import com.google.code.kaptcha.Producer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;



@Controller
public class LoginController implements DemoConstant{

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    //注册
    @RequestMapping(path="/register",method = RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }

    @RequestMapping(path = "/register",method= RequestMethod.POST)
    public String register(Model model, User user){
        Map<String, Object> map = userService.register(user);
        if(map==null||map.isEmpty()){
            model.addAttribute("msg","注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else{
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/register";
        }
    }

    @RequestMapping(path = "/activation/{userId}/{code}",
            method = RequestMethod.GET)
    public String activation(Model model
            ,@PathVariable("userId") int userId
            ,@PathVariable("code") String code){

        int result = userService.activation(userId, code);
        if(result==ACTIVATION_SUCCESS){
            model.addAttribute("msg","激活成功，您的账号已经可以正常使用了");
            model.addAttribute("target","/login");
        }else if(result==ACTIVATION_REPEAT){
            model.addAttribute("msg","该账号已经激活");
            model.addAttribute("target","/index");
        }else{
            model.addAttribute("msg","激活失败，激活码不正确");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }

    //登录
    @RequestMapping(path = "/login",method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }

    @RequestMapping(path = "/login",method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean remember
                        /*,HttpSession session*/
            , Model model
            , HttpServletResponse response
            , @CookieValue("KaptchaOwner") String kaptchaOwner) {
        //String kaptcha= (String) session.getAttribute("kaptcha");
        String kaptcha = null;
        if (kaptchaOwner != null) {
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }
        if(StringUtils.isBlank(kaptcha)||StringUtils.isBlank(code)||!code.equalsIgnoreCase(kaptcha)){
            model.addAttribute("codeMsg","验证码不正确！");
            return "/site/login";
        }
        //检查账号密码
        //勾上记住，过期时间 12小时或100天 即cookie的时间
        int expiredSecond=remember?REMEMBER_EXPIRED_SECONDS:DEFALUT_EXPIRED_SECONDS;
        //System.out.println(expiredSecond);
        //System.out.println(expiredSecond/24/3600);
        Map<String, Object> map = userService.login(username, password, expiredSecond);
        //如果登录成功，将登录凭证通过cookie传到浏览器
        if(map.containsKey("ticket")){
            Cookie cookie=new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath(contextPath);//有效路径
            cookie.setMaxAge(expiredSecond);
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
            return "redirect:/index";
        }else{
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }

    }

    /**
     * 退出登录
     */
    @RequestMapping(path = "/logout",method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        return "redirect:/login";
    }

    /**
     * 响应验证码请求 返回图片和cookie
     */
    @RequestMapping(path="/kaptcha",method = RequestMethod.GET)
    public void getKaptcha(/*HttpSession session, */HttpServletResponse response) {
        //生成验证码
        String text=kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);


        String owner = DemoUtil.generateUUID();
        Cookie cookie = new Cookie("KaptchaOwner", owner);
        //Sets the maximum age in seconds for this Cookie
        // 60秒过期
        cookie.setMaxAge(60);

        //Specifies a path for the cookie to which the client should return the cookie.
        /*
        Specifies a path for the cookie to which the client should return the cookie.
        The cookie is visible to all the pages in the directory you specify,
        and all the pages in that directory's subdirectories.
        A cookie's path must include the servlet that set the cookie,
        for example, /catalog, which makes the cookie visible to all directories on the server under /catalog.
        */
        cookie.setPath(contextPath);
        response.addCookie(cookie);

        String redisKey = RedisKeyUtil.getKaptchaKey(owner);

        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);

        //验证码存入session
        //session.setAttribute("kaptcha",text);

        //图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream out=response.getOutputStream();
            ImageIO.write(image,"png",out);
        } catch (IOException e) {
            logger.error("响应验证码失败" + e.getMessage());
        }

    }

}

