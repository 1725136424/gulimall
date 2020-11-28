package site.wanjiahao.gulimall.order.vo;

import lombok.Data;
import site.wanjiahao.gulimall.order.entity.OrderEntity;

@Data
public class OrderResponseVo {

    /**
     * 订单实体
     */
    private OrderEntity orderEntity;

    /**
     * code状态码 0下单成功 1下单失败
     */
    private Integer code;

}
