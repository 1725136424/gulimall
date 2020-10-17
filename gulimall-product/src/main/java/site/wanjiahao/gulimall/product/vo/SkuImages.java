/**
  * Copyright 2020 bejson.com 
  */
package site.wanjiahao.gulimall.product.vo;

import lombok.Data;
import org.hibernate.validator.constraints.URL;
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
public class SkuImages {

    @NotBlank(message = "sku图片不能为空", groups = SaveSpuInfoGroup.class)
    @URL(message = "sku图片必须是url", groups = SaveSpuInfoGroup.class)
    private String url;

    @NotBlank(message = "sku图片名称不能为空", groups = SaveSpuInfoGroup.class)
    private String name;

    private Integer sort;

    @NotNull(message = "sku默认图片不能为空", groups = SaveSpuInfoGroup.class)
    private Integer defaultImage;
}