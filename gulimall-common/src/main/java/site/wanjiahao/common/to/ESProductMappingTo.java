package site.wanjiahao.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * ES映射sku实体类
 */
@Data
public class ESProductMappingTo {

    private Long spuId;

    // 需要注意的是当前产品是根据不同的颜色来划分，并不是根据另外一些销售属性划分
    private List<Product> products;

    private List<Attr> attrs;

    private Brand brand;

    private Category category;

    // 添加独立搜索字段 --> 取当前商品的第一个来搜索
    private String skuTitle;

    private BigDecimal skuPrice;

    private String skuImg;

    private Long saleCount;

    private Float hotScore;

    private Boolean hasStock;

    @Data
    public static class Product {

        private Long skuId;

        private String skuTitle;

        private String skuImg;

        private Long saleCount;

        private BigDecimal skuPrice;

        private Boolean hasStock;

        private Float hotScore;
    }

    @Data
    public static class Brand {

        private Long brandId;

        private String brandName;

        private String brandImg;
    }

    @Data
    public static class Category {

        private Long categoryId;

        private String categoryName;
    }

    @Data
    public static class Attr {

        private Long attrId;

        private String attrName;

        private List<String> attrValue;
    }
}
