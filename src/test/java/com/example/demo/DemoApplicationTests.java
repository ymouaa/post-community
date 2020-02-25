package com.example.demo;

import com.example.demo.dao.AlphaDao;
import com.example.demo.dao.UserMapper;
import com.example.demo.entity.User;
import com.example.demo.service.AlphaService;
import com.example.demo.service.UserService;
import com.example.demo.util.DemoConstant;
import com.example.demo.util.DemoUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
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
	/*
	* 日期处理
	* */
	@Test
	public void testDate(){
		Date date = null;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String s="";
		for(int i=1;i<100;i++){
			 date = new Date(System.currentTimeMillis() +3600*24*i*1000);
			 s=format.format(date);

			System.out.println(s+"    "+i);
			if(i%30==0||i%31==0||i%29==0)
				System.out.print("  "+" ------- ");
		}
		System.out.println("===========================");
		Date date1 = new Date(System.currentTimeMillis() +3600*24*1000);
		/**
		 *	2020-02-21 12:35:19
		 	2020-02-21 22:40:53
		 */

		String format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date1);
		System.out.println(format);
		System.out.println(format1);

	}
	@Autowired
	private UserService userService;
	@Autowired
	private UserMapper userMapper;
	@Test
	public void update(){
		User user = userService.findUserById(149);
		System.out.println(user.getPassword());
		String md5 = DemoUtil.md5(user.getPassword()+user.getSalt());
		userMapper.updatePassword(149,md5);
	}
	//8b3f1ad1ee0505ea5a46e9dad8976c61
	//8b3f1ad1ee0505ea5a46e9dad8976c61
	/*
	public void testIssame(){

		User user = userMapper.selectByName("niuke");
		System.out.println(user.getPassword());
		System.out.println(DemoUtil.md5("12345"+user.getSalt()));
	}*/




}
