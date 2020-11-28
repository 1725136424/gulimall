package site.wanjiahao.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SettleAccountsVo {

    /**
     * 当前用户id
     */
    private Long userId;

    /**
     * 收货人地址集合
     */
    private List<ReceiveAddressVo> receiveAddressVos;

    /**
     * 订单商品详情，获取购物车中所有选择的购物项
     */
    private List<OrderItemVo> orderItemVos;

    /**
     * 会员的积分信息
     */
    private Integer integration;

    /**
     * 订单总额
     */
    private BigDecimal totalPrice;

    /**
     * 防重复令牌 防止订单重复提交
     *
     */
    private String token;

    public BigDecimal getTotalPrice() {
        BigDecimal total = BigDecimal.ZERO;
        if (orderItemVos != null && orderItemVos.size() > 0) {
            for (OrderItemVo orderItemVo : orderItemVos) {
                total = total.add(orderItemVo.getTotalPrice());
            }
        }
        return total;
    }

    /**
     * 返现信息 用积分信息换取
     */
    private BigDecimal returnPrice;

    public BigDecimal getReturnPrice() {
        // 当前积分的百分之一，为返现价格
        return new BigDecimal(integration * 0.01);
    }

    /**
     * 需要付款
     */
    private BigDecimal payPrice;

    public BigDecimal getPayPrice() {
        return getTotalPrice().subtract(getReturnPrice());
    }
}
