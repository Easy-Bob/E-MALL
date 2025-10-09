package com.bob.mall.ware.service.impl;

import com.bob.common.dto.SkuHasStockDto;
import com.bob.common.exception.NoStockExecption;
import com.bob.common.utils.PageUtils;
import com.bob.mall.ware.feign.ProductFeignService;
import com.bob.mall.ware.vo.LockStockResult;
import com.bob.mall.ware.vo.OrderItemVo;
import com.bob.mall.ware.vo.WareSkuLockVO;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bob.common.utils.*;

import com.bob.mall.ware.dao.WareSkuDao;
import com.bob.mall.ware.entity.WareSkuEntity;
import com.bob.mall.ware.service.WareSkuService;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private WareSkuDao wareSkuDao;

    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if(!StringUtils.isEmpty(skuId)){
            wrapper.eq("sku_id", skuId);
        }
        String wareId = (String) params.get("wareId");
        if(!StringUtils.isEmpty(wareId)){
            wrapper.eq("ware_id", wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    /**
     * 入库操作
     * @param skuId 商品编号
     * @param wareId 仓库编号
     * @param skuNum  采购商品的数量
     */
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        // 判断是否有改商品和仓库的入库记录
        List<WareSkuEntity> list = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if(list == null || list.size() == 0){
            // 如果没有就新增商品库存记录
            WareSkuEntity entity = new WareSkuEntity();
            entity.setSkuId(skuId);
            entity.setWareId(wareId);
            entity.setStock(skuNum);
            entity.setStockLocked(0);
            try {
                // 动态的设置商品的名称
                R info = productFeignService.info(skuId); // 通过Feign远程调用商品服务的接口
                Map<String,Object> data = (Map<String, Object>) info.get("skuInfo");
                if(info.getCode() == 0){
                    entity.setSkuName((String) data.get("skuName"));
                }
            }catch (Exception e){

            }
            wareSkuDao.insert(entity); // 插入商品库存记录
        }else{
            // 如果有就更新库存
            wareSkuDao.addStock(skuId,wareId,skuNum);
        }
    }

    @Override
    public List<SkuHasStockDto> getSkusHasStock(List<Long> skuIds) {
        List<SkuHasStockDto> list = skuIds.stream().map(skuId -> {
            Long count = baseMapper.getSkuStock(skuId);
            SkuHasStockDto dto = new SkuHasStockDto();
            dto.setSkuId(skuId);
            dto.setHasStock(count > 0);
            return dto;
        }).collect(Collectors.toList());
        return list;
    }

    /**
     * 锁定库存操作
     * @param vo
     * @return
     */
    @Override
    public Boolean orderLockStock(WareSkuLockVO vo) {
        List<OrderItemVo> items = vo.getItems();

        // 首先找到具有库存的仓库
        List<SkuWareHasStock> collect = items.stream().map(item -> {
            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
            skuWareHasStock.setSkuId(item.getSkuId());

            List<WareSkuEntity> wareSkuEntities = this.baseMapper.listHashStock(item.getSkuId());
            skuWareHasStock.setWareSkuEntities(wareSkuEntities);

            skuWareHasStock.setNum(item.getCount());
            return skuWareHasStock;
        }).collect(Collectors.toList());

        // 遍历仓库
        for (SkuWareHasStock skuWareHasStock : collect) {
            Long skuId = skuWareHasStock.getSkuId();
            List<WareSkuEntity> wareSkuEntities = skuWareHasStock.getWareSkuEntities();
            if(wareSkuEntities == null || wareSkuEntities.isEmpty()){
                // 没有库存了
                throw new NoStockExecption(skuId);
            }

            Integer count = skuWareHasStock.getNum();
            Boolean skuStocked = false; // 表示当前SkuId的库存没有锁定完成
            // 判断当前锁定的商品
            for(WareSkuEntity wareSkuEntity: wareSkuEntities){
                // 循环仓库，找到每个仓库剩余的选定商品数量
                Integer canStock = wareSkuEntity.getStock() - wareSkuEntity.getStockLocked();
                if(count <= canStock){
                    // 库存足够多
                    Integer i = this.baseMapper.lockSkuStock(skuId, wareSkuEntity.getWareId(), count);
                    count = 0;
                    skuStocked = true;
                }else{
                    // 库存不够，提供最大库存数
                    Integer i = this.baseMapper.lockSkuStock(skuId, wareSkuEntity.getWareId(), count);
                    count = count - canStock;
                }
                if(count <= 0){
                    // 所有商品已锁定
                    break;
                }
            }
            if(count > 0){
                // 说明库存没有锁定完
                throw new NoStockExecption(skuId);
            }
            if(!skuStocked){
                throw new NoStockExecption(skuId);
            }
        }
        return true;
    }

    @Data
    class SkuWareHasStock{
        private Long skuId;
        private Integer num;
        private List<WareSkuEntity> wareSkuEntities;
    }

}