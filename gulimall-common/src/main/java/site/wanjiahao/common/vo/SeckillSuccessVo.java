package site.wanjiahao.common.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillSuccessVo {

    private Long memberId;

    private String orderSn;

    private Long skuId;

    private String skuTitle;

    private String skuImage;

    private BigDecimal skuPrice;

    private Integer totalMount;
}
