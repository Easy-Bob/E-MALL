package com.bob.mall.seckill.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 
 * 
 * @author bob
 * @email bsun3217@gmail.com
 * @date 2025-09-25 22:23:24
 */
@Data
public class SeckillSkuRelationEntity{

	/**
	 * id
	 */
	private Long id;
	/**
	 * 
	 */
	private Long promotionId;
	/**
	 * 
	 */
	private Long promotionSessionId;
	/**
	 * 
	 */
	private Long skuId;
	/**
	 * 
	 */
	private BigDecimal seckillPrice;
	/**
	 * 
	 */
	private BigDecimal seckillCount;

	private BigDecimal seckillLimit;
	/**
	 * 
	 */
	private Integer seckillSort;

}
