package site.wanjiahao.common.to;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 积分传输对象
 */
@Data
public class SpuBoundsTo implements Serializable {

    private Long id;

    private Long spuId;

    private BigDecimal growBounds;

    private BigDecimal buyBounds;

    private Integer work;

}
