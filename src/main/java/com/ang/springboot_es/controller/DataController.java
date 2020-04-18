package com.ang.springboot_es.controller;


import com.ang.springboot_es.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
public class DataController {


    @Autowired
    private DataService dataService;


    @RequestMapping(value = "/data", method = {RequestMethod.POST, RequestMethod.GET})
    public String getDataPage() {
        return "/site/admin/data";
    }


    @RequestMapping(value = "/data/uv", method = RequestMethod.POST)
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start
            , @DateTimeFormat(pattern = "yyyy-MM-dd") Date end, Model model) {
        long uv = dataService.getUV(start, end);
        model.addAttribute("uvResult",uv);
        model.addAttribute("uvStart",start);
        model.addAttribute("uvEnd",end);
        return "forward:/data";
    }

    @RequestMapping(value = "/data/dau", method = RequestMethod.POST)
    public String getDAV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start
            , @DateTimeFormat(pattern = "yyyy-MM-dd") Date end, Model model) {
        long dav = dataService.getDAU(start, end);
        model.addAttribute("dauResult",dav);
        model.addAttribute("dauStart",start);
        model.addAttribute("dauEnd",end);
        return "forward:/data";
    }


}
