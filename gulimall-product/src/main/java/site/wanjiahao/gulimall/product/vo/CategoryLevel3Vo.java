package site.wanjiahao.gulimall.product.vo;

import lombok.Data;

@Data
public class CategoryLevel3Vo {

    /**
     * 二级分类Id
     */
    private Long catalog2Id;

    /**
     * 三级分类Id
     */
    private Long id;

    /**
     * 三级分类名称
     */
    private String name;
}
