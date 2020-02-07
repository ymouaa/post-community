package com.example.demo;

import com.example.demo.controller.HomeController;
import com.example.demo.entity.Page;
import com.example.demo.service.DiscussPostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes=DemoApplication.class)
public class TestPage {


    @Autowired
    private DiscussPostService discussPostService;

    @Test
    public  void  testPageRows(){
        int rows = discussPostService.findDiscussPostRows(0);

        System.out.println(rows/10);
        Page page = new Page();
        page.setRows(rows);

        System.out.println(page.getTotal());

    }
}
