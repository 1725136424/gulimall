package site.wanjiahao.gulimall.product.vo;

import lombok.Data;

import java.util.List;

@Data
public class SaleAttrVos {

    private Long attrId;

    private String attrName;

    private List<AttrValuesWithSkuIdsVo> attrValuesWithSkuIdsVos;
}
