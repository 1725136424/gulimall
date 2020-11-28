package site.wanjiahao.gulimall.order.interceptor;

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
        if (session != null) {
            MemberEntityVo user = (MemberEntityVo) session.getAttribute(AuthServerConstant.SESSION_USER);
            if (user != null) {
                threadLocal.set(user);
                return true;
            } else {
                response.sendRedirect("http://auth.gulimall.com/login");
                return false;
            }
        }
        return true;
    }
}
