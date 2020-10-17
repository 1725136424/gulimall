/**
  * Copyright 2020 bejson.com 
  */
package site.wanjiahao.gulimall.product.vo;

import lombok.Data;
import site.wanjiahao.common.valid.SaveSpuInfoGroup;

import javax.validation.constraints.NotNull;

/**
 * Auto-generated: 2020-10-15 21:40:2
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class AttrEntities {

    @NotNull(message = "属性id不能为空", groups = SaveSpuInfoGroup.class)
    private Long attrId;

    @NotNull(message = "属性名称不能为空", groups = SaveSpuInfoGroup.class)
    private String attrName;

    private Integer searchType;

    private String icon;

    private String valueSelect;

    private Integer valueType;

    private Integer attrType;

    private Integer enable;

    private Integer catelogId;

    @NotNull(message = "快速展示不能为空", groups = SaveSpuInfoGroup.class)
    private Integer showDesc;

    @NotNull(message = "属性值不能为空", groups = SaveSpuInfoGroup.class)
    private String attrValue;
}