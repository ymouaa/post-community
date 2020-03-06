package com.example.demo.util;


import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.Map;
import java.util.UUID;

public class DemoUtil {

    /****
    //生成随机字符串
    *********/
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }



    /****
     * MD5加密
     *
     *
     * MD5密码加密
     * 特点:
     * 原密码：hello
     * 每次加密的结果都是一样
     * 但不能解密
     * hello--->abc123456
     * 但黑客 可以有简单密码的库 把那些密码加密成这样的值
     *提高安全性
     *user表里不是有salt字段吗
     * 加上salt
     * hello+3eaarfdf____---->abc123456dkfh
     *没有规律了
     * 黑客盗取就难了
     * 而且越长越难
     * 加上中文就更难了
     */
    public static String md5(String key){
        if(StringUtils.isBlank(key))   //  ""，  ” “ ， null  都会认为是blank
        {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
     }

    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if (map != null) {
            for (String key : map.keySet()) {
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();
    }

    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }



}
