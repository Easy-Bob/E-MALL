package com.bob.mall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.bob.common.constant.OrderConstant;
import com.bob.common.constant.SeckillConstant;
import com.bob.common.utils.R;
import com.bob.common.vo.MemberVO;
import com.bob.mall.seckill.dto.SeckillOrderDto;
import com.bob.mall.seckill.dto.SeckillSkuRedisDto;
import com.bob.mall.seckill.feign.CouponFeignService;
import com.bob.mall.seckill.feign.ProductFeignService;
import com.bob.mall.seckill.interceptor.AuthInterceptor;
import com.bob.mall.seckill.service.SecKillService;
import com.bob.mall.seckill.vo.SeckillSessionEntity;
import com.bob.mall.seckill.vo.SeckillSkuRelationEntity;
import com.bob.mall.seckill.vo.SkuInfoVo;
import org.apache.commons.lang.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SecKillServiceImpl implements SecKillService {

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public void uploadSecKillSku3Days() {
        // 通过openfeign远程调用
        R r = couponFeignService.getLate3DaysSession();
        if(r.getCode() == 0){
            String json = (String)r.get("data");
            List<SeckillSessionEntity> seckillSessionEntities = JSON.parseArray(json, SeckillSessionEntity.class);

            // 上架商品 // Redis数据存储
            // 1. 缓存 sku商品信息
            saveSessionSkuInfos(seckillSessionEntities);
            // 2. 缓存 每日秒杀的信息
            saveSessionInfos(seckillSessionEntities);

        }
    }

    /**
     * 查询当前时间内的秒杀活动及对应的商品sku信息
     * @return
     */
    @Override
    public List<SeckillSkuRedisDto> getCurrentSecKillSkus() {
        // 1. 确定当前时间 对应的秒杀活动
        long time = new Date().getTime();
        // 2. 从redis中查询所有秒杀活动
        Set<String> keys = redisTemplate.keys(SeckillConstant.SESSION_CHACE_PREFIX + "*");
        for (String key : keys) {
            String replace = key.replace(SeckillConstant.SESSION_CHACE_PREFIX, "");

            String[] startEnd = replace.split("_");
            Long start = Long.parseLong(startEnd[0]);
            Long end = Long.parseLong(startEnd[1]);

            if(time >= start && time <= end){
                List<String> range = redisTemplate.opsForList().range(key, 0, -1);
                BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SeckillConstant.SKU_CHACE_PREFIX);
                List<String> list = ops.multiGet(range);
                if(list != null && !list.isEmpty()){
                    List<SeckillSkuRedisDto> collect = list.stream().map(item -> {
                        SeckillSkuRedisDto dto = JSON.parseObject(item, SeckillSkuRedisDto.class);
                        return dto;
                    }).collect(Collectors.toList());
                    return collect;
                }
            }
        }
        return null;
    }

    /* 根据SKUID查询秒杀活动对应的信息
     * @param skuId
     * @return
     */
    @Override
    public SeckillSkuRedisDto getSeckillSessionBySkuId(Long skuId) {
        // 1.找到所有需要参与秒杀的商品的sku信息
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SeckillConstant.SKU_CHACE_PREFIX);
        Set<String> keys = ops.keys();
        if(keys != null && keys.size() > 0){
            String regx = "\\d_"+ skuId;
            for (String key : keys) {
                boolean matches = Pattern.matches(regx, key);
                if(matches){
                    // 说明找到了对应的SKU的信息
                    String json = ops.get(key);
                    SeckillSkuRedisDto dto = JSON.parseObject(json, SeckillSkuRedisDto.class);
                    return dto;
                }
            }
        }
        return null;
    }

    @Override
    public String kill(String killId, String code, Integer num) {
        //根据killId获取当前秒杀的商品信息 Redis
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SeckillConstant.SKU_CHACE_PREFIX);
        String json = ops.get(killId);
        if(StringUtils.isNotBlank(json)){
            SeckillSkuRedisDto dto = JSON.parseObject(json, SeckillSkuRedisDto.class);
            // 校验合法性

            // 校验时效性
            Long startTime = dto.getStartTime();
            Long endTime = dto.getEndTime();
            long now = new Date().getTime();
            // 商品秒杀时间合理
            if(now >= startTime && now <= endTime){
                // 校验 随机码合法性
                String randCode = dto.getRandCode();
                Long skuId = dto.getSkuId();
                String redisKillId = dto.getPromotionSessionId() + "_" + skuId;
                if(randCode.equals(code) && killId.equals(redisKillId)){
                    // 校验 抢购商品数量是否合法
                    if(num <= dto.getSeckillLimit().intValue()){
                        MemberVO memberVO = AuthInterceptor.threadLocal.get();
                        Long id = memberVO.getId();
                        String redisKey = id + "_" + redisKillId;
                        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), endTime - now, TimeUnit.MILLISECONDS);
                        if(aBoolean){
                            // 数据插入成功，第一次秒杀
                            RSemaphore semaphore = redissonClient.getSemaphore(SeckillConstant.SKU_STOCK_SEMAPHORE + randCode);
                            try {
                                boolean b = semaphore.tryAcquire(num, 100, TimeUnit.MILLISECONDS);
                                if(b){
                                    String orderSN = UUID.randomUUID().toString().replace("-", "");
                                    // 快速下订单 -> RocketMQ
                                    SeckillOrderDto orderDto = new SeckillOrderDto();
                                    orderDto.setOrderSN(orderSN);
                                    orderDto.setSkuId(skuId);
                                    orderDto.setSeckillPrice(dto.getSeckillPrice());
                                    orderDto.setMemberId(id);
                                    orderDto.setNum(num);
                                    orderDto.setPromotionSessionId(dto.getPromotionSessionId());

                                    // 通过RocketMQ,发送异步消息
                                    rocketMQTemplate.sendOneWay(
                                            OrderConstant.ROCKETMQ_SECKILL_ORDER_TOPIC
                                            ,MessageBuilder.withPayload(JSON.toJSONString(orderDto)).build());

                                    return orderSN;
                                }
                            } catch (InterruptedException e) {
                                return null;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private void saveSessionInfos(List<SeckillSessionEntity> seckillSessionEntities) {
        // 循环session
        for (SeckillSessionEntity seckillSessionEntity : seckillSessionEntities) {
            long start = seckillSessionEntity.getStartTime().getTime();
            long end = seckillSessionEntity.getEndTime().getTime();

            // 生成key
            String key = SeckillConstant.SESSION_CHACE_PREFIX + start + "_" + end;
            Boolean flag = redisTemplate.hasKey(key);

            if(!flag){
                // 需要存储到Redis中的秒杀商品skuId
                List<String> collect = seckillSessionEntity.getRelationEntities().stream().map(item -> {
                    return item.getPromotionId() + "_" + item.getSkuId().toString();
                }).collect(Collectors.toList());

                redisTemplate.opsForList().leftPushAll(key, collect);
            }



        }
    }

    private void saveSessionSkuInfos(List<SeckillSessionEntity> seckillSessionEntities) {
        seckillSessionEntities.stream().forEach(session -> {
            // 对于session中的每个skuId, 封装具体信息
            BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(SeckillConstant.SKU_CHACE_PREFIX);
            session.getRelationEntities().stream().forEach(item ->{
                Boolean flag = redisTemplate.hasKey(item.getSkuId().toString());
                if(!flag){
                    SeckillSkuRedisDto dto = new SeckillSkuRedisDto();

                    // 1. 获取sku基本信息
                    R info = productFeignService.info(item.getSkuId());
                    if(info.getCode() == 0){
                        Object skuInfoData = info.get("skuInfo");
                        SkuInfoVo skuInfoVo = JSON.parseObject(JSON.toJSONString(skuInfoData), SkuInfoVo.class);
                        dto.setSkuInfoVo(skuInfoVo);
                    }

                    // 2. 获取sku秒杀信息
//                dto.setSkuId(item.getSkuId());
//                dto.setSeckillPrice(item.getSeckillPrice());
//                dto.setSeckillCount(item.getSeckillCount());
//                dto.setSeckillLimit(item.getSeckillLimit());
//                dto.setSeckillSort(item.getSeckillSort());
                    BeanUtils.copyProperties(item, dto);

                    // 3.设置商品的秒杀时间
                    dto.setStartTime(session.getStartTime().getTime());
                    dto.setEndTime(session.getEndTime().getTime());

                    // 4. 随机码
                    String token = UUID.randomUUID().toString().replace("-", "");
                    dto.setRandCode(token);
                    dto.setPromotionSessionId(item.getPromotionSessionId());

                    // 分布式信号量的处理 限流
                    RSemaphore semaphore = redissonClient.getSemaphore(SeckillConstant.SKU_STOCK_SEMAPHORE + token);
                    // 把秒杀活动的商品数量作为分布式信号量的信号量
                    semaphore.trySetPermits(item.getSeckillCount().intValue());
                    hashOps.put(item.getPromotionId() + "_" + item.getSkuId(), JSON.toJSONString(dto));
                }

            });
        });
    }
}
