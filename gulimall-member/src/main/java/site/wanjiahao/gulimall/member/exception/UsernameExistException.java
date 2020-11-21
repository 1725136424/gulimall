package site.wanjiahao.gulimall.member.exception;


public class UsernameExistException extends RuntimeException{

    public UsernameExistException() {
        super("用户名或者手机号已经存在");
    }
}
