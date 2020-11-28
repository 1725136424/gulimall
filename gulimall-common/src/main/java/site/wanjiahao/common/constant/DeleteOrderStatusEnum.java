package site.wanjiahao.common.constant;

public enum DeleteOrderStatusEnum {
    UN_DELETE(0,"未删除"),
    DELETE(1,"已删除");

    private final Integer code;
    private final String msg;

    DeleteOrderStatusEnum(Integer code, String msg) {
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
