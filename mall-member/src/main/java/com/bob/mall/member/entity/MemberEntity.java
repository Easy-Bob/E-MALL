package com.bob.mall.member.entity;

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
 * @date 2025-09-25 23:21:28
 */
@Data
@TableName("ums_member")
public class MemberEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * 
	 */
	private Long levelId;
	/**
	 * 
	 */
	private String username;
	/**
	 * 
	 */
	private String password;
	/**
	 * 
	 */
	private String nickname;
	/**
	 * 
	 */
	private String mobile;
	/**
	 * 
	 */
	private String email;
	/**
	 * ͷ
	 */
	private String header;
	/**
	 * 
	 */
	private Integer gender;
	/**
	 * 
	 */
	private Date birth;
	/**
	 * 
	 */
	private String city;
	/**
	 * ְҵ
	 */
	private String job;
	/**
	 * 
	 */
	private String sign;
	/**
	 * 
	 */
	private Integer sourceType;
	/**
	 * 
	 */
	private Integer integration;
	/**
	 * 
	 */
	private Integer growth;
	/**
	 * 
	 */
	private Integer status;
	/**
	 * ע
	 */
	private Date createTime;

	private String socialUid;

}
