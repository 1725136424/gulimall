package site.wanjiahao.gulimall.authserver.controller;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import site.wanjiahao.common.constant.AuthServerConstant;
import site.wanjiahao.common.constant.PageConstant;
import site.wanjiahao.common.constant.WeiBoConstant;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.common.vo.MemberEntityVo;
import site.wanjiahao.gulimall.authserver.feign.MemberFeignService;
import site.wanjiahao.gulimall.authserver.vo.Auth2WeiboResponseVo;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/auth2.0")
@Slf4j
public class Auth2Controller {

    @Autowired
    MemberFeignService memberFeignService;


    @GetMapping("/weibo/success")
    public String weiboSuccess(@RequestParam("code") String code, HttpSession httpSession) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            // 获取code码，换取accessToken
            String url = WeiBoConstant.AUTH_SERVER_URL;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
            map.add("client_id", WeiBoConstant.CLIENT_ID);
            map.add("client_secret", WeiBoConstant.CLIENT_SECRET);
            map.add("grant_type", WeiBoConstant.GRANT_TYPE);
            map.add("redirect_uri", WeiBoConstant.CALL_BACK_URL);
            map.add("code", code);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<Auth2WeiboResponseVo> response = restTemplate.postForEntity( url, request, Auth2WeiboResponseVo.class);
            Auth2WeiboResponseVo body = response.getBody();
            // 登录认证
            R r = memberFeignService.auth2Login(body);
            if (r.getCode() == 0) {
                Object user = r.get("user");
                String s = JSON.toJSONString(user);
                MemberEntityVo memberEntityVo = JSON.parseObject(s, MemberEntityVo.class);
                httpSession.setAttribute(AuthServerConstant.SESSION_USER, memberEntityVo);
            }
            return "redirect:" + PageConstant.HOME_URL;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "redirect:" + PageConstant.LOGIN_URL;
        }
    }
}
