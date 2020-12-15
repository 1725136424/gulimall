package site.wanjiahao.common.constant;

public class OrderRabbitConstant {

    public static final String ORDER_EXCHANGE = "order.event.exchange";

    public static final String ORDER_DELAY_QUEUE = "order.delay.queue";

    public static final String ORDER_RELEASE_ORDER_QUEUE = "order.release.order.queue";

    // 订单30min中后自动取消
    public static final int X_MESSAGE_TTL = 1800000;

    public static final String X_DEAD_LETTER_EXCHANGE = ORDER_EXCHANGE;

    public static final String X_DEAD_LETTER_ROUTING_KEY = "order.release";

}
