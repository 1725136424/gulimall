package site.wanjiahao.gulimall.authserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import site.wanjiahao.gulimall.authserver.vo.Auth2WeiboResponseVo;

import java.util.HashMap;

@SpringBootTest
class GulimallAuthServerApplicationTests {

    @Test
    void contextLoads() {

    }

}

class test {
    @Test
    void test() {
        RestTemplate restTemplate = new RestTemplate();
        String code = "31832bf57653ca1cd295bbcfe1014b09";
        // 获取code码，换取accessToken
        String url = "https://api.weibo.com/oauth2/access_token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("client_id", "2387618014");
        map.add("client_secret", "5975d7d241890a362dcc2fa4de2c6067");
        map.add("grant_type", "authorization_code");
        map.add("redirect_uri", "http://auth.gulimall.com/auth2.0/weibo/success");
        map.add("code", code);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<Auth2WeiboResponseVo> response = restTemplate.postForEntity( url, request, Auth2WeiboResponseVo.class);
        Auth2WeiboResponseVo body = response.getBody();
    }


}
class Test1 {

    @Test
    void test1() {
        RestTemplate restTemplate = new RestTemplate();
        HashMap<String, String> queryMap = new HashMap<>();
        queryMap.put("access_token", "2.00CxHXnFyVMabC4a1b283493O8CSmC");
        queryMap.put("uid", "5312600016");
        String forObject = restTemplate.getForObject("https://api.weibo.com/2/users/show.json?access_token=" + "2.00CxHXnFyVMabC4a1b283493O8CSmC" + "&uid=" + "5312600016", String.class);
        System.out.println(forObject);
    }
}
