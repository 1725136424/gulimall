package site.wanjiahao.common.code;

/**
 * 五位状态码
 *  1.前两位代表
 *      10 通用
 *      11 商品
 *      12 订单
 *      13 购物车
 *      14 物流
 *      15 用户
 *  2.后三位代表异常类型
 *      000 未知异常
 *      001 参数校验异常
 *
 */
public enum BizCodeEnum {

    VALID_EXCEPTION(10001, "参数校验异常"),
    UNKNOWN_EXCEPTION(10000, "系统未知异常"),
    SEND_CODE_EXCEPTION(10002, "验证码发送异常"),
    CURRENT_LIMITING_WITH_CODE(10003, "验证码限流异常"),
    USERNAME_EXIST_EXCEPTION(15002, "用户名存在异常"),
    USERNAME_UNKNOWN_EXCEPTION(15003, "用户名不存在异常"),
    PASSWORD_ERROR_EXCEPTION(15004, "密码错误异常");

    private Integer bizCode;

    private String message;

    BizCodeEnum (Integer bizCode, String message) {
        this.bizCode = bizCode;
        this.message = message;
    }

    public Integer getBizCode() {
        return bizCode;
    }

    public void setBizCode(Integer bizCode) {
        this.bizCode = bizCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
