package site.wanjiahao.gulimall.authserver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import site.wanjiahao.common.constant.AuthServerConstant;
import site.wanjiahao.common.constant.PageConstant;

import javax.servlet.http.HttpSession;

@Controller
public class PageController {

    @RequestMapping({"/", "/login"})
    public String login(HttpSession session) {
        Object sessionUser = session.getAttribute(AuthServerConstant.SESSION_USER);
        if (sessionUser != null) {
            return "redirect:" + PageConstant.HOME_URL;
        }
        return "login";
    }
}
