package site.wanjiahao.common.constant;

/**
 * 秒杀场次上架状态
 */
public enum  SeckillSessionStatusEnum {
    NONE_PUBLISH(0, "未上架"),
    ERROR_PUBLISH(1, "上架失败"),
    PUBLISHED(2, "已上架");

    private final int code;

    private final String msg;

    SeckillSessionStatusEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
