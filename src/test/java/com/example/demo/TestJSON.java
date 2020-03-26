package com.example.demo;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.util.DemoUtil;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class TestJSON {


    @Test
    public void testJSON() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "张三");
        map.put("age", 15);
        String s = DemoUtil.getJSONString(100, "这是消息", map);
        System.out.println(s);

    }

    @Test
    public void testObject() {
        O o = new O();
        o.setAge(11);
        o.setContent("我是1111");
        o.setName("lala");
        String toJSONString = JSONObject.toJSONString(o);
        System.out.println(toJSONString);
        HashMap<String, Object> map = JSONObject.parseObject("", HashMap.class);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
        ;
    }

}

class O {
    private String name;
    private int age;
    private String content;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    @Override
    public String toString() {
        return "O{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", content='" + content + '\'' +
                '}';
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String conten) {
        this.content = conten;
    }
}
