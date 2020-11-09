package site.wanjiahao.gulimall.product.vo;

import lombok.Data;

import java.util.List;

@Data
public class SimpleAttrGroupWithAttrVo {

    private String attrGroupName;

    private List<SimpleBaseAttrVo> simpleBaseAttrVos;
}
