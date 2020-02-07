package com.example.demo;

import com.example.demo.dao.AlphaDao;
import com.example.demo.service.AlphaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import java.text.SimpleDateFormat;
import java.util.Date;


@SpringBootTest
@ContextConfiguration(classes=DemoApplication.class)
class DemoApplicationTests implements ApplicationContextAware{

	@Test
	void contextLoads() {
	}

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext=applicationContext;
	}
	@Test
	public void testAppcationContext(){
		System.out.println(this.applicationContext);


		AlphaDao dao = applicationContext.getBean(AlphaDao.class);
		/*
		* 当我想换成mabits时，依赖的是接口
		* */

		String select = dao.select();
		System.out.println(select);

		dao=applicationContext.getBean("alphaHibernate",AlphaDao.class);
		System.out.println(dao.select());
	}
	@Test
	public void testBean(){
		AlphaService alphaService = applicationContext.getBean(AlphaService.class);
		System.out.println(alphaService);

	}

	@Test
	public void testBeanConfig(){
		SimpleDateFormat dateFormat = applicationContext.getBean(SimpleDateFormat.class);
		System.out.println(dateFormat.format(new Date()));
	}
}
