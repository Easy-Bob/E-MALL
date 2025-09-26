package com.bob.mall.product.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * 
 * 
 * @author bob
 * @email bsun3217@gmail.com
 * @date 2025-09-25 21:25:58
 */
@Data
@TableName("pms_category")
public class CategoryEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	@TableId
	private Long catId;
	/**
	 * 
	 */
	private String name;
	/**
	 * 
	 */
	private Long parentCid;
	/**
	 * 
	 */
	private Integer catLevel;
	/**
	 * 
	 */
	private Integer showStatus;
	/**
	 * 
	 */
	private Integer sort;
	/**
	 * ͼ
	 */
	private String icon;
	/**
	 * 
	 */
	private String productUnit;
	/**
	 * 
	 */
	private Integer productCount;

	@TableField(exist = false)
	private List<CategoryEntity> childrens;
}
