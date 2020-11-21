package site.wanjiahao.gulimall.member.exception;

public class PasswordErrorException extends RuntimeException{

    public PasswordErrorException() {
        super("密码错误异常");
    }
}
