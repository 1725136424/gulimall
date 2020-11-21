package site.wanjiahao.gulimall.member.vo;

import lombok.Data;

@Data
public class Auth2WeiboResponseVo {

    private String access_token;

    private String remind_in;

    private String expires_in;

    private String uid;

    private String isRealName;

}
