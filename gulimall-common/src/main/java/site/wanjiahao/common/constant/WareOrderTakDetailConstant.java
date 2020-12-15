package site.wanjiahao.common.constant;

public class WareOrderTakDetailConstant {


    public enum OrderTaskDetail {
        LOCK(1, "已锁定"),
        UNLOCK(2, "已经解锁"),
        SUB(3, "以及扣减");

        public final int code;

        public final String message;

        OrderTaskDetail(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

}
