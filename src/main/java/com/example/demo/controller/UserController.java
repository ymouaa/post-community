package com.example.demo.controller;

import com.example.demo.dao.LoginTicketMapper;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import com.example.demo.util.DemoUtil;
import com.example.demo.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


@Controller
@RequestMapping(path = "/user")
public class UserController {


    private static final Logger logger= LoggerFactory.getLogger(UserController.class);

    @Value("${demo.path.upload}")
    private String uploadPath;

    @Value("${demo.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }


    //存储文件
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String upload(MultipartFile headerImage,Model model){
        if(headerImage==null){
            model.addAttribute("error","您还没有选择图片");
            return "/site/setting";
        }

        String filename = headerImage.getOriginalFilename();
        String suffix=filename.substring(filename.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件格式不正确");
            return "/site/setting";
        }
        filename=DemoUtil.generateUUID()+suffix;
        File file=new File(uploadPath+"/"+filename);
        try {
            headerImage.transferTo(file);
        } catch (IOException e) {
            logger.error("上传文件失败"+e.getMessage());
            throw new RuntimeException("上传文件失败，服务器异常",e);
        }
        //更新头像
        User user = hostHolder.get();//获取当前登录的用户
        String headerUrl=domain+contextPath+"/user/header/"+filename;
        userService.updateHeader(user.getId(),headerUrl);
        return "redirect:/index";
    }
    @RequestMapping(path = "/header/{filename}",method = RequestMethod.GET)
    public void getHeader(@PathVariable(name = "filename")String filename,HttpServletResponse response){
        filename=uploadPath+"/"+filename;
        System.out.println(filename);
        String suffex=filename.substring(filename.lastIndexOf("."));
        response.setContentType("image/"+suffex);
        try (
                ServletOutputStream out = response.getOutputStream();
                FileInputStream fis=new FileInputStream(filename);
        ) {
            byte []buf=new byte[1024];
            int len=0;
            while((len=fis.read(buf))!=-1){
                out.write(buf,0,len);
            }
        } catch (IOException e) {
            logger.error("读取头像失败"+e.getMessage());
            throw new RuntimeException();
        }
    }

    @RequestMapping(path="/password",method = RequestMethod.POST)
    public String updatePassword(String newpassword,String oldpassword,String repassword,Model model,@CookieValue("ticket") String ticket){
        if(StringUtils.isBlank(newpassword)){
            model.addAttribute("newpwdMsg","新密码不能为空");
            return "/site/setting";
        }
        if(StringUtils.isBlank(oldpassword)){
            model.addAttribute("oldpwdMsg","旧密码不能为空");
            return "/site/setting";
        }
        if(StringUtils.isBlank(repassword)){
            model.addAttribute("repwdMsg","确认密码不能为空");
            return "/site/setting";
        }
        if(!StringUtils.equals(newpassword,repassword)) {
            model.addAttribute("newpwdMsg","新密码和旧密码不能相同");
            return "/site/setting";
        }
        User user = hostHolder.get();
        if(!user.getPassword().equals(DemoUtil.md5(oldpassword+user.getSalt()))) {
            model.addAttribute("oldpwdMsg","旧密码不正确");
            return "/site/setting";
        }
        userService.updatePassword(user.getId(),DemoUtil.md5(newpassword+user.getSalt()));
//这里选择把ticket设为1
        userService.logout(ticket);
        return "redirect:/index";
    }
}
