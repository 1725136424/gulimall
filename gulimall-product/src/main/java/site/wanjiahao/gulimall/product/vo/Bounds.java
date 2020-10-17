/**
  * Copyright 2020 bejson.com 
  */
package site.wanjiahao.gulimall.product.vo;

import lombok.Data;
import site.wanjiahao.common.valid.CheckMoney;
import site.wanjiahao.common.valid.SaveSpuInfoGroup;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Auto-generated: 2020-10-15 21:40:2
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class Bounds {

    @NotNull(message = "成长积分不能为空", groups = SaveSpuInfoGroup.class)
    @CheckMoney(message = "积分信息不合法", groups = SaveSpuInfoGroup.class)
    private BigDecimal growBounds;

    @NotNull(message = "购物积分不能为空", groups = SaveSpuInfoGroup.class)
    @CheckMoney(message = "积分信息不合法", groups = SaveSpuInfoGroup.class)
    private BigDecimal buyBounds;

}