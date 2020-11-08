/**
  * Copyright 2020 bejson.com 
  */
package site.wanjiahao.gulimall.product.vo;
import lombok.Data;
import site.wanjiahao.common.valid.CheckMoney;
import site.wanjiahao.common.valid.SaveSpuInfoGroup;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * Auto-generated: 2020-10-15 21:40:2
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class SkuInfo {

    @NotBlank(message = "sku图片不能为空", groups = SaveSpuInfoGroup.class)
    private String skuName;

    @NotBlank(message = "sku标题不能为空", groups = SaveSpuInfoGroup.class)
    private String skuTitle;

    @NotBlank(message = "sku副标题不能为空", groups = SaveSpuInfoGroup.class)
    private String skuSubtitle;

    @NotNull(message = "sku价格不能为空", groups = SaveSpuInfoGroup.class)
    @CheckMoney(message = "sku金额信息不合法", groups = SaveSpuInfoGroup.class)
    private BigDecimal price;

    @Valid
    private Lodder lodder;

    @Valid
    private Reduction reduction;

    @Valid
    private List<MemberPrices> memberPrices;

    @Valid
    @NotEmpty(message = "sku图片不能为空", groups = SaveSpuInfoGroup.class)
    private List<SkuImages> skuImages;

    @Valid
    @NotEmpty(message = "sku属性不能为空", groups = SaveSpuInfoGroup.class)
    private List<Attr> attrs;
}