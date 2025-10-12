package com.bob.mall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bob.common.utils.PageUtils;
import com.bob.common.utils.Query;
import com.bob.common.utils.R;
import com.bob.mall.product.entity.SkuImagesEntity;
import com.bob.mall.product.entity.SpuInfoDescEntity;
import com.bob.mall.product.entity.SpuInfoEntity;
import com.bob.mall.product.feign.SeckillFeignService;
import com.bob.mall.product.service.*;
import com.bob.mall.product.vo.SeckillVO;
import com.bob.mall.product.vo.SkuItemSaleAttrVo;
import com.bob.mall.product.vo.SpuItemGroupAttrVo;
import com.bob.mall.product.vo.SpuItemVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;


import com.bob.mall.product.dao.SkuInfoDao;
import com.bob.mall.product.entity.SkuInfoEntity;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    private SkuInfoDao skuInfoDao;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private SeckillFeignService seckillFeignService;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * SKU 信息检索的方法
     * 类别
     * 品牌
     * 价格区间
     * 检索的关键字
     * 分页查询
     *
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();
        // 检索关键字
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and(w->{
                w.eq("sku_id",key).or().like("sku_name",key);
            });
        }

        // 分类
        String catalogId = (String)params.get("catalogId");
        if(!StringUtils.isEmpty(catalogId) && !"0".equalsIgnoreCase(catalogId)){
            wrapper.eq("catalog_id",catalogId);
        }
        // 品牌
        String brandId = (String)params.get("brandId");
        if(!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_id",brandId);
        }
        // 价格区间
        String min = (String) params.get("min");
        if(!StringUtils.isEmpty(min)){
            wrapper.ge("price",min);
        }
        String max = (String) params.get("max");
        if(!StringUtils.isEmpty(max)){
            try {
                // 如果max=0那么我们也不需要加这个条件
                BigDecimal bigDecimal = new BigDecimal(max);
                if(bigDecimal.compareTo(new BigDecimal(0)) == 1){
                    // 说明 max > 0
                    wrapper.le("price",max);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    /**
     * 根据spuId 查询所有sku
     * @param spuId
     * @return
     */
    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        List<SkuInfoEntity> list = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
        return list;
    }

    @Override
    public SpuItemVO item(Long skuId) throws ExecutionException, InterruptedException {
        SpuItemVO vo = new SpuItemVO();
        CompletableFuture<SkuInfoEntity> skuInfoFuture = CompletableFuture.supplyAsync(() -> {
            // 1. sku基本信息 pms_sku_info
            SkuInfoEntity skuInfoEntity = getById(skuId);
            vo.setInfo(skuInfoEntity);

            return skuInfoEntity;
        }, threadPoolExecutor);

        CompletableFuture<Void> saleFuture = skuInfoFuture.thenAcceptAsync((res) -> {
            // 3. spu销售属性组合
            List<SkuItemSaleAttrVo> saleAttrs = skuSaleAttrValueService.getSkuSaleAttrValueBySpuId(res.getSpuId());
            vo.setSaleAttrs(saleAttrs);
        }, threadPoolExecutor);

        CompletableFuture<Void> spuFuture = skuInfoFuture.thenAcceptAsync((res) -> {
            // 4. spu分组
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(res.getSpuId());
            vo.setDesc(spuInfoDescEntity);
        }, threadPoolExecutor);

        CompletableFuture<Void> groupFuture = skuInfoFuture.thenAcceptAsync((res) -> {
            // 5. spu规格参数
            List<SpuItemGroupAttrVo> baseAttrs = attrGroupService.getAttrGroupWithSpuId(res.getSpuId(), res.getCatalogId());
            vo.setBaseAttrs(baseAttrs);
        }, threadPoolExecutor);

        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            // 2. sku图片信息 pms_sku_images
            List<SkuImagesEntity> images = skuImagesService.getImagesBySkuId(skuId);
            vo.setImages(images);
        }, threadPoolExecutor);

            // 3. 秒杀商品信息
        CompletableFuture<Void> seckillFuture = CompletableFuture.runAsync(() -> {
            // 查询商品的秒杀活动
            R r = seckillFeignService.getSeckillSessionBySkuId(skuId);

            Object data = r.get("data");
            SeckillVO seckillVO = JSON.parseObject(data.toString(), SeckillVO.class);

            vo.setSeckillVO(seckillVO);
        }, threadPoolExecutor);


        CompletableFuture.allOf(saleFuture, spuFuture, groupFuture,imageFuture, seckillFuture).get();

        return vo;
    }

    @Override
    public List<String> getSkuSaleAttrs(Long skuId)
    {
        return this.skuInfoDao.getSkuSaleAttrs(skuId);
    }


}