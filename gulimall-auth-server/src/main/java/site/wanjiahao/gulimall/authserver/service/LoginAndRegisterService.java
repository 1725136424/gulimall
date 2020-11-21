package site.wanjiahao.gulimall.authserver.service;

import site.wanjiahao.common.utils.R;

public interface LoginAndRegisterService {

    R sendCodeAndSaveRedis(String phone, String redisKey);

    boolean verifyCode(String code, String redisKey);
}
