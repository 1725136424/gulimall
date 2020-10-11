package site.wanjiahao.gulimall.product.vo;

import lombok.Data;
import site.wanjiahao.gulimall.product.entity.AttrGroupEntity;
import site.wanjiahao.gulimall.product.entity.CategoryEntity;

import java.io.Serializable;
import java.util.List;

@Data
public class AttrGroupVo extends AttrGroupEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 分类实体
     */
    private CategoryEntity category;

    /**
     * 关联的分类id
     */
    private List<Long> catelogIds;

}
