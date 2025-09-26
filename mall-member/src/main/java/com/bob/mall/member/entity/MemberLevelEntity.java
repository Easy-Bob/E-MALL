package com.bob.mall.member.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * 
 * @author bob
 * @email bsun3217@gmail.com
 * @date 2025-09-25 23:21:28
 */
@Data
@TableName("ums_member_level")
public class MemberLevelEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * 
	 */
	private String name;
	/**
	 * 
	 */
	private Integer growthPoint;
	/**
	 * 
	 */
	private Integer defaultStatus;
	/**
	 * 
	 */
	private BigDecimal freeFreightPoint;
	/**
	 * ÿ
	 */
	private Integer commentGrowthPoint;
	/**
	 * 
	 */
	private Integer priviledgeFreeFreight;
	/**
	 * 
	 */
	private Integer priviledgeMemberPrice;
	/**
	 * 
	 */
	private Integer priviledgeBirthday;
	/**
	 * 
	 */
	private String note;

}
