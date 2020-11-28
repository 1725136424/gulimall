package site.wanjiahao.common.constant;

public class OrderConstant {

    public static final String ORDER_TOKEN_PREFIX = "order:token";

    public static final float PRICE_INTEGRATION_RATE = 0.01f;

    public enum  OrderSource {

        PC(0, "pc"), APP(1, "app");

        private final int code;

        private final String desc;

        OrderSource(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public int getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}
