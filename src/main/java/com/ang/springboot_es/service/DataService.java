package com.ang.springboot_es.service;

import com.ang.springboot_es.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataService {


    @Autowired
    private RedisTemplate redisTemplate;


    private SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

    // ip访问
    public void addUV(String ip) {
        String uvKey = RedisKeyUtil.getUVKey(format.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(uvKey, ip);
    }

    // 日期区间统计ip
    public long getUV(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        List<String> keyList = new ArrayList<>();
        while (!calendar.getTime().after(end)) {
            String key = RedisKeyUtil.getUVKey(format.format(calendar.getTime()));
            keyList.add(key);
            calendar.add(Calendar.DATE, 1);
        }

        String redisKey = RedisKeyUtil.getUVKey(format.format(start), format.format(end));

        redisTemplate.opsForHyperLogLog().union(redisKey, keyList.toArray());

        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }


    public void addDAU(int userId) {
        String redisKey = RedisKeyUtil.getDAUKey(format.format(new Date()));
        redisTemplate.opsForValue().setBit(redisKey, userId, true);
    }


    public long getDAU(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        List<byte[]> keyList = new ArrayList<>();
        while (!calendar.getTime().after(end)) {
            String key = RedisKeyUtil.getDAUKey(format.format(calendar.getTime()));
            keyList.add(key.getBytes());
            calendar.add(Calendar.DATE, 1);
        }

        String redisKey = RedisKeyUtil.getDAUKey(format.format(start), format.format(end));

        Object obj = redisTemplate.execute(new RedisCallback() {
            @Nullable
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {

                connection.bitOp(RedisStringCommands.BitOperation.OR, redisKey.getBytes()
                        , keyList.toArray(new byte[0][]));
                return connection.bitCount(redisKey.getBytes());
            }
        });

        return (long) obj;
    }


}
