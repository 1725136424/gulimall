package site.wanjiahao.common.to;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SkuLadderTo implements Serializable {

    private Long id;

    private Long skuId;

    private Integer fullCount;

    private BigDecimal discount;

    private BigDecimal price;

    private Integer addOther;
}
