package site.wanjiahao.gulimall.search.vo;

import lombok.Data;
import site.wanjiahao.common.to.ESProductMappingTo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 查询参数响应结果参数
 */
@Data
public class SearchResultVo implements Serializable {

    /**
     * 当前页
     */
    private Integer pageNum;

    /**
     * 每一页记录数
     */
    private Integer pageSize;

    /**
     * 总记录数
     */
    private Long totalCount;
    /**
     * 总页数
     */
    private Integer totalPage;

    /**
     * 商品对象
     */
    private List<ESProductMappingTo> esProductMappingTos;

    /**
     * 品牌对象
     */
    private List<BrandVo> brandVos;

    /**
     * 分类对象
     */
    private List<CategoryVo> categoryVos;

    /**
     * 属性对象
     */
    private List<AttrVo> attrVos;

    /**
     * 各种选择条件
     */
    private List<SelectVo> selectVos;

    /**
     * 价格区间
     */
    private List<String> priceRange;

    /**
     * 已经选择的的唯一标识
     */
    private List<String> selectKey;


    @Data
    public static class BrandVo {

        private Long brandId;

        private String brandName;

        private String brandPic;

    }

    @Data
    public static class AttrVo {

        private Long attrId;

        private String attrName;

        private List<String> attrValue;

    }

    @Data
    public static class SelectVo {

        private String selectName;

        private String selectValue;

        private String skipLink;
    }

    @Data
    public static class CategoryVo {

        private Long catId;

        private String catalogName;
    }

    public List<SelectVo> getSelectVos() {
        if (selectVos == null) {
            selectVos = new ArrayList<>();
        }
        return selectVos;
    }

    public List<String> getSelectKey() {
        if (selectKey == null) {
            selectKey = new ArrayList<>();
        }
        return selectKey;
    }
}
