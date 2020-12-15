package site.wanjiahao.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AliPayVo {

    /**
     * 订单实体
     */
    private String subject;

    /**
     * 订单对外交易号
     */
    private String outTradeNo;

    /**
     * 总价
     */
    private BigDecimal totalMount;

    /**
     * 同步回调地址
     */
    private String returnUrl;

}
