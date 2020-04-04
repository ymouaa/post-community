package com.ang.springboot_es.util;


import com.ang.springboot_es.entity.User;
import org.springframework.stereotype.Component;

/*
代替session对象
 */
@Component
public class HostHolder {
    private ThreadLocal<User> users =new ThreadLocal<>();

    public void setUser(User user) {
        users.set(user);
    }

    public User getUser() {
        return  users.get();
    }

    public void clearUser() {
        users.remove();
    }


}
