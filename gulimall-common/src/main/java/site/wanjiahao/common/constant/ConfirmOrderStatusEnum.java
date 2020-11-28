package site.wanjiahao.common.constant;

public enum ConfirmOrderStatusEnum {
    UN_CONFIRM(0,"未确认"),
    CONFIRM(1,"已确认");

    private final Integer code;
    private final String msg;

    ConfirmOrderStatusEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
