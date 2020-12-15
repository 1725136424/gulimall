package site.wanjiahao.common.constant;

public class MQConstant {


    public enum  MQStatus {

        NEW("新建", 0),
        SENT("已发送", 1),
        ERROR_DELIVERED("错误抵达", 2),
        DELIVERY("已抵达", 3);

        private final String msg;

        private final int code;

        MQStatus(String msg, int code) {
            this.msg = msg;
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public int getCode() {
            return code;
        }
    }

}
