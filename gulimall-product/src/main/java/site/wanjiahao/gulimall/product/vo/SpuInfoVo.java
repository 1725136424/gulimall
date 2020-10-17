/**
  * Copyright 2020 bejson.com 
  */
package site.wanjiahao.gulimall.product.vo;
import lombok.Data;
import site.wanjiahao.common.valid.CheckKg;
import site.wanjiahao.common.valid.SaveSpuInfoGroup;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.math.BigDecimal;
import java.util.List;

/**
 * Auto-generated: 2020-10-15 21:40:2
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class SpuInfoVo {

    @Valid
    @NotEmpty(message = "基本属性不能为空", groups = SaveSpuInfoGroup.class)
    private List<BaseAttr> baseAttr;

    @Null(message = "保存spuId必须为空", groups = SaveSpuInfoGroup.class)
    private Long id;

    @NotBlank(message = "spu名称不能为空", groups = SaveSpuInfoGroup.class)
    private String spuName;

    private String spuDescription;

    @NotNull(message = "必须存在分类", groups = SaveSpuInfoGroup.class)
    private Long catelogId;

    @NotNull(message = "必须存在品牌", groups = SaveSpuInfoGroup.class)
    private Long brandId;

    @NotNull(message = "spu重量不能为空", groups = SaveSpuInfoGroup.class)
    @CheckKg(message = "spu重量单位不合法(小数位1到3位)", groups = SaveSpuInfoGroup.class)
    private BigDecimal weight;

    @Valid
    private Bounds bounds;

    @Valid
    @NotEmpty(message = "描述图片必须存在", groups = SaveSpuInfoGroup.class)
    private List<Description> description;

    @Valid
    @NotEmpty(message = "介绍图集必须存在", groups = SaveSpuInfoGroup.class)
    private List<Images> images;

    @Valid
    @NotEmpty(message = "sku信息不能为空", groups = SaveSpuInfoGroup.class)
    private List<SkuInfo> skuInfo;

}