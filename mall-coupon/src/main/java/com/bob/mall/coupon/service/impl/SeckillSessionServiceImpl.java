package com.bob.mall.coupon.service.impl;

import com.bob.common.utils.PageUtils;
import com.bob.mall.coupon.entity.SeckillSkuRelationEntity;
import com.bob.mall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bob.common.utils.*;

import com.bob.mall.coupon.dao.SeckillSessionDao;
import com.bob.mall.coupon.entity.SeckillSessionEntity;
import com.bob.mall.coupon.service.SeckillSessionService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {


    @Autowired
    private SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLate3Days() {
        List<SeckillSessionEntity> list = this.list(new QueryWrapper<SeckillSessionEntity>().between("start_time", startTime(), endTime()));
        List<SeckillSessionEntity> newList = list.stream().map(session -> {
            // 根据sessionId查询出商品信息
            List<SeckillSkuRelationEntity> relationEntities = seckillSkuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", session.getId()));
            session.setRelationEntities(relationEntities);
            return session;
        }).collect(Collectors.toList());
        return newList;
    }

    private String startTime(){
        LocalDate now = LocalDate.now();
        LocalDate startDay = now.plusDays(0);
        LocalTime min = LocalTime.MIN;
        LocalDateTime start = LocalDateTime.of(startDay, min);
        return start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String endTime(){
        LocalDate now = LocalDate.now();
        LocalDate endDay = now.plusDays(2);
        LocalTime max = LocalTime.MAX;
        LocalDateTime end = LocalDateTime.of(endDay, max);
        return end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}