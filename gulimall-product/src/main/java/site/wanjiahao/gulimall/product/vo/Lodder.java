/**
  * Copyright 2020 bejson.com 
  */
package site.wanjiahao.gulimall.product.vo;

import lombok.Data;
import site.wanjiahao.common.valid.CheckMoney;
import site.wanjiahao.common.valid.SaveSpuInfoGroup;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Auto-generated: 2020-10-15 21:40:2
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class Lodder {

    @NotNull(message = "满多少件不能为空", groups = SaveSpuInfoGroup.class)
    private Integer fullCount;

    @NotNull(message = "打折折扣不能为空", groups = SaveSpuInfoGroup.class)
    @DecimalMin(value = "0", groups = SaveSpuInfoGroup.class)
    @DecimalMax(value = "1", groups = SaveSpuInfoGroup.class)
    @CheckMoney(message = "折扣信息不合法", groups = SaveSpuInfoGroup.class)
    private BigDecimal discount;

    private Integer addOther;
}