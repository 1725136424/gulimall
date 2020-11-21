package site.wanjiahao.gulimall.member.exception;

public class UsernameUnknownException extends RuntimeException{

    public UsernameUnknownException() {
        super("用户名不存在异常");
    }
}
