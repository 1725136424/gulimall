package site.wanjiahao.gulimall.authserver.to;

import lombok.Data;

@Data
public class VerifyCodeTo {

    private String phone;

    private String code;
}
