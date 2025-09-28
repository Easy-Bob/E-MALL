package com.bob.mall.product.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * 
 * @author bob
 * @email bsun3217@gmail.com
 * @date 2025-09-25 21:25:58
 */
@Data
@TableName("pms_attr_group")
public class AttrGroupEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	@TableId
	private Long attrGroupId;
	/**
	 * 
	 */
	private String attrGroupName;
	/**
	 * 
	 */
	private Integer sort;
	/**
	 * 
	 */
	private String descript;
	/**
	 * 
	 */
	private String icon;
	/**
	 * 
	 */
	private Long catelogId;

	/**
	 * 修改数据时记录类别信息
	 */
	@TableField(exist = false)
	private Long[] catelogPath;

}
