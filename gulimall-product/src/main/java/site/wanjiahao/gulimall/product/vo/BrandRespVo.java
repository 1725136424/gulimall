package site.wanjiahao.gulimall.product.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import site.wanjiahao.gulimall.product.entity.BrandEntity;
import site.wanjiahao.gulimall.product.entity.CategoryEntity;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class BrandRespVo extends BrandEntity implements Serializable {

    // 品牌所关联的分类
    private CategoryEntity category;
}
