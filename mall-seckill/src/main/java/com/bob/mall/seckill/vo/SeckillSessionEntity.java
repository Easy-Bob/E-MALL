package com.bob.mall.seckill.vo;


import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class SeckillSessionEntity {
    /**
     * id
     */
    private Long id;
    /**
     *
     */
    private String name;
    /**
     * ÿ
     */
    private Date startTime;
    /**
     * ÿ
     */
    private Date endTime;
    /**
     *
     */
    private Integer status;
    /**
     *
     */
    private Date createTime;

    // 秒杀活动对应的sku信息
    private List<SeckillSkuRelationEntity> relationEntities;

}
