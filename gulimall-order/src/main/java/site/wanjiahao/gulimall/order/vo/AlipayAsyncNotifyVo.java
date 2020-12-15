package site.wanjiahao.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class AlipayAsyncNotifyVo {

    /**
     * 订单号（对外业务号）
     */
    private String out_trade_no;
    /**
     * 支付宝交易流水号
     */
    private String trade_no;
    /**
     * 支付总金额
     */
    private BigDecimal total_amount;
    /**
     * 交易内容
     */
    private String subject;
    /**
     * 支付状态
     */
    private String trade_status;
    /**
     * 创建时间
     */
    private Date gmt_create;
    /**
     * 确认时间
     */
    private Date gmt_payment;
    /**
     * 回调内容
     */
    private String passback_params;
    /**
     * 回调时间
     */
    private Date notify_time;

}
