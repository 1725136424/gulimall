package site.wanjiahao.gulimall.ware.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class Item implements Serializable {

    private Long itemId;

    private Integer status;

    private BigDecimal price;

    private String reason;
}
