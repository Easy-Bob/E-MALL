package com.bob.mall.seckill.schedule;

import com.bob.mall.seckill.service.SecKillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 定时上架 秒杀商品信息
 */
@Slf4j
@Component
public class SeckillSkuSchedule {

    @Autowired
    private SecKillService secKillService;

    @Autowired
    private RedissonClient redissonClient;

    // 默认同步
    @Async
    @Scheduled(cron = "0 * * * * *")
    public void uploadSecKillSku3Days(){
        // 分布式锁
        RLock lock = redissonClient.getLock("seckill:upload:lock");
        lock.lock(10, TimeUnit.SECONDS);
        try{
            secKillService.uploadSecKillSku3Days();
        }catch (Exception e){
        }finally {
            lock.unlock();
        }
    }
}
