package com.example.demo.controller;

import com.example.demo.service.AlphaService;
import com.example.demo.util.DemoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@Controller
@RequestMapping("/alpha")
public class AlphaController {
    @Autowired
    private AlphaService alphaService;

    @RequestMapping("/helle")
    @ResponseBody
    public String sayhello(){
        return "hello";
    }


    @RequestMapping("/http")
    public void http(HttpServletRequest request,HttpServletResponse response){
     //    获取请求数据
        System.out.println("请求方式"+request.getMethod());
        System.out.println("请求路径"+request.getServletPath());
        System.out.println("请求url"+request.getRequestURL());
        System.out.println("ContextPath"+request.getContextPath());

        Enumeration<String> enumeration = request.getHeaderNames();
        while(enumeration.hasMoreElements()){
            String element = enumeration.nextElement();
            String value=request.getHeader(element);
            System.out.println(element+" : "+value);
        }
     //返回响应数据
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out=null;
        try {
            out = response.getWriter();
            out.write("<h1>welcome<h1>");
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(out!=null)
                out.close();
        }
    }

    //GET请求

    //查询所有学生
    // /students?current=1&limit=20;
    @RequestMapping(
            path="/students",method= RequestMethod.GET
    )
    @ResponseBody
    public String getStudents(@RequestParam(name="current",required = false,defaultValue = "1") int current,
                              @RequestParam(name="current",required = false,defaultValue = "10") int limit){
        System.out.println(current+"  "+limit);
        return "some students";
    }


    // /student/123
    @RequestMapping(path="/student/{id}",method=RequestMethod.GET)
    @ResponseBody
    public String getStudent(@PathVariable("id") int id){
        System.out.println(id);
        return "a student";
    }



    //html
    @RequestMapping(path="/teacher",method = RequestMethod.GET)
    public ModelAndView getTeahcer(){
        ModelAndView mav=new ModelAndView();
        mav.addObject("name","张三");
        mav.addObject("age","15");
        mav.setViewName("/demo/view");
        return mav;
    }

    @RequestMapping(path="/school",method=RequestMethod.GET)
    public String getschool(Model model){
        model.addAttribute("name","某某大学");
        model.addAttribute("age","???年");
        return "/demo/view";
    }


    //json
    //  字符串
    @RequestMapping("/emps")
    @ResponseBody
    public List<Map<String,Object>> look(){
        List<Map<String,Object>> emps=new ArrayList<>();
        Map <String,Object> emp=new HashMap<>();
        emp.put("name","张三");
        emp.put("age","23");
        emp.put("money","123");
        emps.add(emp);
        emp=new HashMap<>();
        emp.put("name","李四");
        emp.put("age","23");
        emp.put("money","123");
        emps.add(emp);
        emp=new HashMap<>();
        emp.put("name","王五");
        emp.put("age","23");
        emp.put("money","123");
        emps.add(emp);
        return emps;
    }


    // ajax示例
    @RequestMapping(path = "/ajax", method = RequestMethod.POST)
    @ResponseBody
    public String testAjax(String name, int age) {
        System.out.println(name);
        System.out.println(age);
        return DemoUtil.getJSONString(0, "操作成功!");
    }


    @RequestMapping(path = "/test", method = RequestMethod.GET)
    public String testTemplate(Model model) {
        List<Map<String, Object>> list = new ArrayList<>();

        Map<String, Object> m1 = new HashMap<>();
        m1.put("status", 0);


        Map<String, Object> m2 = new HashMap<>();
        m2.put("status", 1);

        Map<String, Object> m3 = new HashMap<>();
        m3.put("status", 2);

        list.add(m1);
        list.add(m2);
        list.add(m3);


        model.addAttribute("data", 111);
        model.addAttribute("innerdata", 222);
        model.addAttribute("statuslist", list);
        return "/demo/testTemplate";
    }

}















