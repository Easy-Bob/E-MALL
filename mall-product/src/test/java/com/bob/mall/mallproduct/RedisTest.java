package com.bob.mall.mallproduct;

import com.bob.mall.product.MallProductApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.UUID;

@SpringBootTest(classes = MallProductApplication.class)
public class RedisTest {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Test
    public void testStringRedisTemplate(){
        // 获取操作String类型的Options对象
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        // 插入数据
        ops.set("name","bob"+ UUID.randomUUID());
        // 获取存储的信息
        System.out.println("刚刚保存的值："+ops.get("name"));
    }
}
