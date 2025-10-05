package com.bob.mall.product.vo;

import com.bob.mall.product.entity.SkuImagesEntity;
import com.bob.mall.product.entity.SkuInfoEntity;
import com.bob.mall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * 商品详情页的数据对象
 */
@Data
public class SpuItemVO {
    // 1.sku的基本信息 pms_sku_info
    SkuInfoEntity info;
    // 2.是否有库存
    boolean hasStock = true;
    // 3.sku的图片信息pms_sku_images
    List<SkuImagesEntity> images;
    // 4.获取spu中的销售属性的组合
    List<SkuItemSaleAttrVo> saleAttrs;
    // 5.获取SPU的介绍
    SpuInfoDescEntity desc;
    // 6.获取SPU的规格参数
    List<SpuItemGroupAttrVo> baseAttrs;

}