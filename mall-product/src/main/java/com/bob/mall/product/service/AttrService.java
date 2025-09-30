package com.bob.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bob.common.utils.PageUtils;
import com.bob.mall.product.entity.AttrEntity;
import com.bob.mall.product.entity.ProductAttrValueEntity;
import com.bob.mall.product.vo.AttrGroupRelationVO;
import com.bob.mall.product.vo.AttrResponseVo;
import com.bob.mall.product.vo.AttrVO;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author bob
 * @email bsun3217@gmail.com
 * @date 2025-09-25 21:25:58
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<AttrEntity> getRelationAttr(Long attrgroupId);

    void deleteRelation(AttrGroupRelationVO[] vos);

    void saveAttr(AttrVO vo);

    PageUtils queryBasePage(Map<String, Object> params, Long catelogId, String attrType);

    AttrResponseVo getAttrInfo(Long attrId);

    void updateBaseAttr(AttrVO attr);

    void removeByIdsDetail(Long[] attrIds);

    PageUtils getNoAttrRelation(Map<String, Object> params, Long attrgroupId);


    List<Long> selectSearchAttrIds(List<Long> attrIds);
}

