package com.ang.springboot_es.config;


import com.ang.springboot_es.quartz.AlphaJob;
import com.ang.springboot_es.quartz.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

// 配置 -> 数据库 -> 调用
@Configuration
public class QuartzConfig {

    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(PostScoreRefreshJob.class);
        factoryBean.setName("postScoreRefreshJob");
        factoryBean.setGroup("communityJobGroup");
        // 长久保存，
        factoryBean.setDurability(true);
        // 可恢复的
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }


    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshJobTrigger(JobDetail postScoreRefreshJobDetail) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(postScoreRefreshJobDetail);
        factoryBean.setName("postScoreRefreshTrigger");
        factoryBean.setGroup("communityTriggerGroup");
        factoryBean.setRepeatInterval(1000 * 60 * 5);
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }


    // 配置JobDetail
//    @Bean
    public JobDetailFactoryBean alphaJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();

        factoryBean.setJobClass(AlphaJob.class);

        factoryBean.setName("alphaJob");

        factoryBean.setGroup("alphaGroup");

        // 长久保存，
        factoryBean.setDurability(true);

        // 可恢复的
        factoryBean.setRequestsRecovery(true);

        return factoryBean;
    }

    // 配置Trigger  SimpleTriggerFactoryBean, CronTriggerFactoryBean
//    @Bean
    public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(alphaJobDetail);
        factoryBean.setName("alphaTrigger");
        factoryBean.setGroup("alphaTriggerGroup");
        // 频率
        factoryBean.setRepeatInterval(3000);
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }


}
