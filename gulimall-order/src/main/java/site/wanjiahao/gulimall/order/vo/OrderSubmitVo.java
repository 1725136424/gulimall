package site.wanjiahao.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderSubmitVo {

    /**
     * 地址id
     */
    private Long addressId;

    private Integer payType;

    // 无需订单数据 直接在购物车查询即可
    /**
     * 防重复令牌
     */
    private String token;

    /**
     * 积分信息
     */
    private BigDecimal integration;

    /**
     * 应付价格
     */
    private BigDecimal payPrice;

}
