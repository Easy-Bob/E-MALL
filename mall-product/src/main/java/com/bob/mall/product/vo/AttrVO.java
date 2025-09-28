package com.bob.mall.product.vo;

import lombok.Data;

@Data
public class AttrVO {
    private Long attrId;
    private String attrName;
    private Integer SearchType;
    private String icon;
    private String valueSelect;
    private Integer attrType;
    private Long eanble;
    private Long catelogId;
    private Integer showDesc;
    private Long attrGroupId;
}
