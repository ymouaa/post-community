package com.ang.springboot_es.controller;


import com.ang.springboot_es.entity.User;
import com.ang.springboot_es.service.FollowService;
import com.ang.springboot_es.service.LikeService;
import com.ang.springboot_es.service.UserService;
import com.ang.springboot_es.util.DemoConstant;
import com.ang.springboot_es.util.DemoUtil;
import com.ang.springboot_es.util.HostHolder;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Controller
@RequestMapping(path = "/user")
public class UserController implements DemoConstant {


    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${demo.path.upload}")
    private String uploadPath;

    @Value("${demo.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;


    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;


    @Value("${qiniu.ak}")
    private String accessKey;

    @Value("${qiniu.sk}")
    private String secretKey;


    @Value("${qiniu.bucket.header.name}")
    private String headerBucketname;

    @Value("${qiniu.bucket.header.url}")
    private String headerUrl;

    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage(Model model) {
        // 文件名
        String filename=DemoUtil.generateUUID();


        // 响应结果
        StringMap putPolicy = new StringMap();
        putPolicy.put("returnBody", DemoUtil.getJSONString(0));

        // 上传凭证
        Auth auth = Auth.create(accessKey, secretKey);
        String uploadToken = auth.uploadToken(headerBucketname, filename, 600, putPolicy);

        model.addAttribute("uploadToken",uploadToken);
        model.addAttribute("filename",filename);

        return "/site/setting";
    }



    // 跟新路径
    @RequestMapping(value = "/header/url",method = RequestMethod.POST)
    @ResponseBody
    public String updateHeaderUrl(String filename,Model model){
        if(StringUtils.isBlank(filename)){
            model.addAttribute("error","您还没有选择图片");
            return "/site/setting";
        }

        String url=headerUrl+"/"+filename;

        userService.updateHeader(hostHolder.getUser().getId(),url);

        return DemoUtil.getJSONString(0);
    }






    //存储文件
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String upload(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片");
            return "/site/setting";
        }

        String filename = headerImage.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件格式不正确");
            return "/site/setting";
        }
        filename = DemoUtil.generateUUID() + suffix;
        File file = new File(uploadPath + "/" + filename);
        try {
            headerImage.transferTo(file);
        } catch (IOException e) {
            logger.error("上传文件失败" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器异常", e);
        }
        //更新头像
        User user = hostHolder.getUser();//获取当前登录的用户
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(), headerUrl);
        return "redirect:/index";
    }


    /**
     * 获取头像
     */
    @RequestMapping(path = "/header/{filename}", method = RequestMethod.GET)
    public void getHeader(@PathVariable(name = "filename") String filename, HttpServletResponse response) {
        filename = uploadPath + "/" + filename;
        //System.out.println(filename);
        String suffex = filename.substring(filename.lastIndexOf("."));
        response.setContentType("image/" + suffex);
        try (
                ServletOutputStream out = response.getOutputStream();
                FileInputStream fis = new FileInputStream(filename)
        ) {
            byte[] buf = new byte[1024];
            int len = 0;
            while ((len = fis.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            logger.error("读取头像失败" + e.getMessage());
            throw new RuntimeException();
        }
    }


    /**
     * 修改密码
     */
    @RequestMapping(path = "/password", method = RequestMethod.POST)
    public String updatePassword(String newpassword, String oldpassword, String repassword, Model model, @CookieValue("ticket") String ticket) {
        if (StringUtils.isBlank(newpassword)) {
            model.addAttribute("newpwdMsg", "新密码不能为空");
            return "/site/setting";
        }
        if (StringUtils.isBlank(oldpassword)) {
            model.addAttribute("oldpwdMsg", "旧密码不能为空");
            return "/site/setting";
        }
        if (StringUtils.isBlank(repassword)) {
            model.addAttribute("repwdMsg", "确认密码不能为空");
            return "/site/setting";
        }
        if (StringUtils.equals(newpassword, repassword)) {
            model.addAttribute("newpwdMsg", "新密码和旧密码不能相同");
            return "/site/setting";
        }
        User user = hostHolder.getUser();
        if (!user.getPassword().equals(DemoUtil.md5(oldpassword + user.getSalt()))) {
            model.addAttribute("oldpwdMsg", "旧密码不正确");
        }
        userService.updatePassword(user.getId(), DemoUtil.md5(newpassword + user.getSalt()));
        //这里选择把ticket设为1
        userService.logout(ticket);
        return "redirect:/index";
    }


    /**
     * 用户详情页
     */
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String profile(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        //防止查不存在的用户
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        //用户
        model.addAttribute("user", user);
        //点赞数量
        int likeCount = likeService.findLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        //关注数量

        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);

        model.addAttribute("followeeCount", followeeCount);

        //粉丝数量


        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);

        //对当前用户是否关注

        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }





}
