/**
  * Copyright 2020 bejson.com 
  */
package site.wanjiahao.gulimall.product.vo;

import lombok.Data;
import site.wanjiahao.common.valid.SaveSpuInfoGroup;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Auto-generated: 2020-10-15 21:40:2
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class Attr {

    @NotNull(message = "sku属性id不能为空", groups = SaveSpuInfoGroup.class)
    private Long attrId;

    @NotBlank(message = "sku属性名称不能为空", groups = SaveSpuInfoGroup.class)
    private String attrName;

    @NotBlank(message = "sku属性值不能为空", groups = SaveSpuInfoGroup.class)
    private String attrValue;
}