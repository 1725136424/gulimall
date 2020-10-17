package site.wanjiahao.gulimall.product.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import site.wanjiahao.gulimall.product.entity.AttrEntity;
import site.wanjiahao.gulimall.product.entity.AttrGroupEntity;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class AttrGroupWithAttrVo extends AttrGroupEntity {

    // 当前属性组关联的属性
    List<AttrEntity> attrEntities;

}
