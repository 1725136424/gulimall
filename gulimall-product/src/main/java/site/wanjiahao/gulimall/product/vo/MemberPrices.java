/**
  * Copyright 2020 bejson.com 
  */
package site.wanjiahao.gulimall.product.vo;

import lombok.Data;
import site.wanjiahao.common.valid.CheckMoney;
import site.wanjiahao.common.valid.SaveSpuInfoGroup;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Auto-generated: 2020-10-15 21:40:2
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class MemberPrices {

    @NotNull(message = "会员等级id不能为空", groups = SaveSpuInfoGroup.class)
    private Long memberLevelId;

    @NotBlank(message = "会员名称不能为空", groups = SaveSpuInfoGroup.class)
    private String memberLevelName;

    @NotNull(message = "会员价格不能为空", groups = SaveSpuInfoGroup.class)
    @CheckMoney(message = "会员价格不合法", groups = SaveSpuInfoGroup.class)
    private BigDecimal memberPrice;

    private Integer addOther;
}