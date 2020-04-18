package com.ang.springboot_es;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//@RunWith(SpringRunner.class)
//@SpringBootTest
//@ContextConfiguration(classes = SpringbootEsApplication.class)
public class TestThreadPool {


    private static final Logger LOGGER = LoggerFactory.getLogger(TestThreadPool.class);

    private ExecutorService executorService = Executors.newFixedThreadPool(5);

    @Test
    public void testExecutor() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                LOGGER.debug("hello executor");
            }
        };


        for (int i = 0; i < 10; i++) {
            executorService.submit(task);
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Autowired
    private Scheduler scheduler;

    @Test
    public void testDelete() {
        try {
            scheduler.deleteJob(new JobKey("alphaJob","alphaGroup"));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

    }
}
