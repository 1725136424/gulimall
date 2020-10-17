package site.wanjiahao.gulimall.product.vo;

import lombok.Data;
import site.wanjiahao.gulimall.product.entity.BrandEntity;
import site.wanjiahao.gulimall.product.entity.CategoryEntity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class SpuInfoWithBrandAndCategoryVo implements Serializable {

    /**
     * 商品id
     */
    private Long id;
    /**
     * 商品名称
     */
    private String spuName;
    /**
     * 商品描述
     */
    private String spuDescription;
    /**
     * 所属分类id
     */
    private Long catelogId;
    /**
     * 品牌id
     */
    private Long brandId;
    /**
     *
     */
    private BigDecimal weight;
    /**
     * 上架状态[0 - 下架，1 - 上架]
     */
    private Integer publishStatus;
    /**
     *
     */
    private Date createTime;
    /**
     *
     */
    private Date updateTime;

    /**
     * 品牌实体
     */
    private BrandEntity brandEntity;

    /**
     * 分类实体
     */
    private CategoryEntity categoryEntity;
}
