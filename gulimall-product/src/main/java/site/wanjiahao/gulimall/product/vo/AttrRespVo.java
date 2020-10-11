package site.wanjiahao.gulimall.product.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import site.wanjiahao.gulimall.product.entity.AttrEntity;
import site.wanjiahao.gulimall.product.entity.CategoryEntity;

@EqualsAndHashCode(callSuper = true)
@Data
public class AttrRespVo extends AttrEntity {

    private CategoryEntity category;

}
