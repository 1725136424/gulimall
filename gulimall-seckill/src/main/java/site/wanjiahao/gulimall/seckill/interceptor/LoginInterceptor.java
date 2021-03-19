package site.wanjiahao.gulimall.seckill.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import site.wanjiahao.common.constant.AuthServerConstant;
import site.wanjiahao.common.vo.MemberEntityVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginInterceptor implements HandlerInterceptor {

    public static final ThreadLocal<MemberEntityVo> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        MemberEntityVo memberEntityVo = (MemberEntityVo) session.getAttribute(AuthServerConstant.SESSION_USER);
        if (memberEntityVo != null) {
            threadLocal.set(memberEntityVo);
            return true;
        } else {
            // 跳转登录页面
            response.sendRedirect("http://auth.gulimall.com/");
        }
        return false;
    }
}
