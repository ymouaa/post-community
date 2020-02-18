package com.example.demo;

import com.example.demo.dao.LoginTicketMapper;
import com.example.demo.entity.LoginTicket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes=DemoApplication.class)
public class TestTikcet {

    @Autowired
    private LoginTicketMapper loginTicketMapper;



    @Test
    public void testFindTicket(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setTicket("abc");
        loginTicketMapper.insertTicket(loginTicket);

        LoginTicket ticket = loginTicketMapper.selectByTicket("123");
        System.out.println(ticket);
    }
}
