package site.wanjiahao.gulimall.authserver.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import site.wanjiahao.common.utils.NumberUtils;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.gulimall.authserver.feign.ThirdPartFeignService;
import site.wanjiahao.gulimall.authserver.service.LoginAndRegisterService;
import site.wanjiahao.gulimall.authserver.to.VerifyCodeTo;

import java.util.concurrent.TimeUnit;

@Service
public class LoginAndRegisterServiceImpl implements LoginAndRegisterService {

    @Autowired
    private ThirdPartFeignService thirdPartFeignService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public R sendCodeAndSaveRedis(String phone, String redisKey) {
        // 发送验证码
        String code = NumberUtils.createRandom(true, 6);
        VerifyCodeTo verifyCodeTo = new VerifyCodeTo();
        verifyCodeTo.setPhone(phone);
        verifyCodeTo.setCode(code);
        // 加上当前时间戳，防止验证码防刷
        code = code + "_" + System.currentTimeMillis();
        // 发送验证码
        R r = thirdPartFeignService.sendCode(verifyCodeTo);
        if (r.getCode() == 0) {
            // 发送成功 --> 保存验证码至redis中
            stringRedisTemplate.opsForValue().set(redisKey, code, 5, TimeUnit.MINUTES);
            return R.ok();
        } else {
            return R.error(r.getCode(),
                    r.get("msg") + "");
        }
    }

    @Override
    public boolean verifyCode(String code, String redisKey) {
        // 取出redis存入的验证码
        String s = stringRedisTemplate.opsForValue().get(redisKey);
        if (!StringUtils.isBlank(s)) {
            String redisCode = s.split("_")[0];
            return redisCode.equals(code);
        }
        return false;
    }
}
