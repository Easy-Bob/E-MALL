package com.bob.mall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * sku
 * 
 * @author bob
 * @email bsun3217@gmail.com
 * @date 2025-09-25 21:25:59
 */
@Data
@TableName("pms_sku_info")
public class SkuInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * skuId
	 */
	@TableId
	private Long skuId;
	/**
	 * spuId
	 */
	private Long spuId;
	/**
	 * sku
	 */
	private String skuName;
	/**
	 * sku
	 */
	private String skuDesc;
	/**
	 * 
	 */
	private Long catalogId;
	/**
	 * Ʒ
	 */
	private Long brandId;
	/**
	 * Ĭ
	 */
	private String skuDefaultImg;
	/**
	 * 
	 */
	private String skuTitle;
	/**
	 * 
	 */
	private String skuSubtitle;
	/**
	 * 
	 */
	private BigDecimal price;
	/**
	 * 
	 */
	private Long saleCount;

}
