/**
  * Copyright 2020 bejson.com 
  */
package site.wanjiahao.gulimall.product.vo;

import lombok.Data;
import site.wanjiahao.common.valid.SaveSpuInfoGroup;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Auto-generated: 2020-10-15 21:40:2
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class BaseAttr {

    @NotNull(message = "分组Id不能为空", groups = SaveSpuInfoGroup.class)
    private Long attrGroupId;

    @NotBlank(message = "分组名称不能为空", groups = SaveSpuInfoGroup.class)
    private String attrGroupName;

    private Integer sort;

    private String descript;

    private String icon;

    @NotNull(message = "分组分类Id不能为空", groups = SaveSpuInfoGroup.class)
    private Long catelogId;

    @Valid
    @NotEmpty(message = "分组属性不能为空", groups = SaveSpuInfoGroup.class)
    private List<AttrEntities> attrEntities;
}