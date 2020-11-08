package site.wanjiahao.gulimall.product.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class IndexCategoryLevel2RespVo implements Serializable {

    /**
     * 一级分类Id
     */
    private Long catalog1Id;

    /**
     * 三级分类列表
     */
    private List<CategoryLevel3Vo> catalog3List;

    /**
     * 二级分类Id
     */
    private Long id;

    /**
     * 二级分类名称
     */
    private String name;
}
