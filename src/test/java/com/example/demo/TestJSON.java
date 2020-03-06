package com.example.demo;

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
}
