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
public class Images {

    @NotBlank(message = "介绍图片URL不能为空", groups = SaveSpuInfoGroup.class)
    @URL(message = "介绍图片必须是URL", groups = SaveSpuInfoGroup.class)
    private String url;

    @NotBlank(message = "介绍图片名称不能为空", groups = SaveSpuInfoGroup.class)
    private String name;

    private Integer sort;

    @NotNull(message = "默认图片参数不能为空", groups = SaveSpuInfoGroup.class)
    private Integer defaultImage;

}