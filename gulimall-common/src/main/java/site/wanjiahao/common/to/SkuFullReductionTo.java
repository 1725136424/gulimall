package site.wanjiahao.common.to;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SkuFullReductionTo implements Serializable {

    /**
     * id
     */
    private Long id;
    /**
     * spu_id
     */
    private Long skuId;
    /**
     * 满多少
     */
    private BigDecimal fullPrice;
    /**
     * 减多少
     */
    private BigDecimal reducePrice;
    /**
     * 是否参与其他优惠
     */
    private Integer addOther;
}
