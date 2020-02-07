package com.example.demo.dao;


import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
/*会自动扫描*/
@Repository("alphaHibernate")    //bean的名字
@Primary// 更高的优先级
public class AlphaDaoHibernateImp implements  AlphaDao {

    @Override
    public String select() {
        return null;
    }
}
