package site.wanjiahao.gulimall.cart.interceptor;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import site.wanjiahao.common.constant.AuthServerConstant;
import site.wanjiahao.common.constant.CartConstant;
import site.wanjiahao.common.constant.CookieConstant;
import site.wanjiahao.common.vo.MemberEntityVo;
import site.wanjiahao.gulimall.cart.pojo.UserInfo;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * 当前拦截器的总体思路是
 *      1.保存当前登录用户的id
 *      2.保存临时账户的id
 *          1).如果是保存临时key的话，可以想到，临时key使用的是UUID不保存起来，就会有消失的可能
 *          2).如果是保存到ThreadLocal中，这能保存当前请求，显然不可取
 *          3).所以可以保存在cookie中，并且设置过去时间，需要用时，就可以去cookie中取值
 *   这两步主要对应redis中的两种购物车登录购物车，临时购物车
 */
public class ThreadLocalInterceptor implements HandlerInterceptor {

    public static final ThreadLocal<UserInfo> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 判断当前用户是否登录
        UserInfo userInfo = new UserInfo();
        HttpSession session = request.getSession();
        MemberEntityVo memberEntityVo = (MemberEntityVo) session.getAttribute(AuthServerConstant.SESSION_USER);
        if (memberEntityVo != null) {
            userInfo.setUserId(memberEntityVo.getId() + "");
        }
        // 提取当时的临时key
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(CartConstant.USER_TEMP_KEY)) {
                    // 设置临时key
                    userInfo.setUserKey(cookie.getValue());
                    break;
                }
            }
        }
        if (StringUtils.isBlank(userInfo.getUserKey())) {
            // 创建一个临时key
            String randomUserKey = UUID.randomUUID().toString().replace("-", "");
            userInfo.setUserKey(randomUserKey);
        }
        threadLocal.set(userInfo);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 判断当前浏览器是否存在user-key这个建 没有保存
        Cookie[] cookies = request.getCookies();
        boolean flag = false;
       if (cookies != null && cookies.length > 0) {
           for (Cookie cookie : cookies) {
               if (cookie.getName().equals(CartConstant.USER_TEMP_KEY)) {
                   flag = true;
                   break;
               }
           }
       }
        if (!flag) {
            UserInfo userInfo = threadLocal.get();
            String userKey = userInfo.getUserKey();
            // 保存至cookie中 cookie规则 一个月过期，作用于整个工程，gulimall.com
            Cookie cookie = new Cookie(CartConstant.USER_TEMP_KEY, userKey);
            cookie.setDomain(CookieConstant.DOMAIN);
            cookie.setMaxAge(CookieConstant.MAX_AGE);
            response.addCookie(cookie);
        }
    }
}
