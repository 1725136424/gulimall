package site.wanjiahao.common.constant;

public class WareRabbitConstant {

    public static final String STOCK_EXCHANGE = "stock.event.exchange";

    public static final String STOCK_DELAY_QUEUE = "stock.delay.queue";

    public static final String STOCK_RELEASE_STOCK_QUEUE = "stock.release.stock.queue";

    // 库存40min钟后自动解锁
    public static final int X_MESSAGE_TTL = 2400000;

    public static final String X_DEAD_LETTER_EXCHANGE = STOCK_EXCHANGE;

    public static final String X_DEAD_LETTER_ROUTING_KEY = "stock.release";

}
