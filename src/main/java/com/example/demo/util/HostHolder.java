package com.example.demo.util;


import com.example.demo.entity.User;
import org.springframework.stereotype.Component;

/*
代替session对象
 */
@Component
public class HostHolder {
    private ThreadLocal<User> users =new ThreadLocal<>();

    public void set(User user){
        users.set(user);
    }
    public User get(){
        return  users.get();
    }
    public void clear(){
        users.remove();
    }


}
