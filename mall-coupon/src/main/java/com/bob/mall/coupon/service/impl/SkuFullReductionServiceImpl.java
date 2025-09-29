package com.bob.mall.coupon.service.impl;

import com.bob.common.dto.SkuReductionDTO;
import com.bob.common.utils.PageUtils;
import com.bob.mall.coupon.entity.MemberPriceEntity;
import com.bob.mall.coupon.entity.SkuLadderEntity;
import com.bob.mall.coupon.service.MemberPriceService;
import com.bob.mall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bob.common.utils.*;

import com.bob.mall.coupon.dao.SkuFullReductionDao;
import com.bob.mall.coupon.entity.SkuFullReductionEntity;
import com.bob.mall.coupon.service.SkuFullReductionService;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    private SkuLadderService skuLadderService;
    @Autowired
    private MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionDTO dto) {
        // 5.3 保存满减信息，折扣，会员价
        // mall_sms: sms_sku_ladder sms_full_reduction sms_member_price
        // 1.折扣
        SkuLadderEntity ladderEntity = new SkuLadderEntity();
        ladderEntity.setSkuId(dto.getSkuId());
        ladderEntity.setFullCount(dto.getFullCount());
        ladderEntity.setDiscount(dto.getDiscount());
        ladderEntity.setAddOther(dto.getCountStatus());
        if(ladderEntity.getFullCount() > 0){
            skuLadderService.save(ladderEntity);
        }
        // 2.满减
        SkuFullReductionEntity fullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(dto,fullReductionEntity);
        if(fullReductionEntity.getFullPrice().compareTo(new BigDecimal(0)) == 1){
            this.save(fullReductionEntity);
        }
        // 3.会员价
        List<MemberPriceEntity> memberPriceEntities = dto.getMemberPrice().stream().map(item -> {
            MemberPriceEntity priceEntity = new MemberPriceEntity();
            priceEntity.setSkuId(dto.getSkuId());
            priceEntity.setMemberLevelId(item.getId());
            priceEntity.setMemberPrice(item.getPrice());
            priceEntity.setAddOther(1); // 是否可叠加
            return priceEntity;
        }).collect(Collectors.toList());
        memberPriceService.saveBatch(memberPriceEntities);
    }

}