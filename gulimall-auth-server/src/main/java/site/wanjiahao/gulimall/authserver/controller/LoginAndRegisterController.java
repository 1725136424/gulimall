package site.wanjiahao.gulimall.authserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import site.wanjiahao.common.code.BizCodeEnum;
import site.wanjiahao.common.utils.Constant;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.gulimall.authserver.feign.MemberFeignService;
import site.wanjiahao.gulimall.authserver.group.LoginGroup;
import site.wanjiahao.gulimall.authserver.group.RegisterGroup;
import site.wanjiahao.gulimall.authserver.service.LoginAndRegisterService;
import site.wanjiahao.gulimall.authserver.vo.RegisterUserVo;

import java.util.HashMap;

@Controller
@RequestMapping("/auth")
@Slf4j
public class LoginAndRegisterController {

    @Autowired
    private LoginAndRegisterService loginAndRegisterService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private MemberFeignService memberFeignService;

    @GetMapping("/user/sendCode")
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone) {
        /**
         * 阿里云短信服务默认实现了短信防刷的功能，这里可以不予处理
         *      短信防刷的实现步骤
         *          1.redis 中存入key(phone): code + "当前时间戳"
         *          2.取出当前时间戳比较，如果小于一分钟就不发送短信
         */
        String key = Constant.CODE_PREFIX + ":" + phone;
        String redisCode = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isBlank(redisCode)) {
            return loginAndRegisterService.sendCodeAndSaveRedis(phone, key);
        } else {
            // 判断当前验证码是否是在一分钟内重复发送的
            long perTime = Long.parseLong(redisCode.split("_")[1]);
            long currentTimeMillis = System.currentTimeMillis();
            if ((currentTimeMillis - perTime) <= 60000) {
                // 小于一分钟 --> 返回限流异常
                return R.error(BizCodeEnum.CURRENT_LIMITING_WITH_CODE.getBizCode(),
                        BizCodeEnum.CURRENT_LIMITING_WITH_CODE.getMessage());
            } else {
                return loginAndRegisterService.sendCodeAndSaveRedis(phone, key);
            }
        }
    }


    @PostMapping("/user/register")
    public String register(@Validated(RegisterGroup.class) RegisterUserVo registerUserVo, Model model) {
        HashMap<String, Object> paramsMap = new HashMap<>();
        // 校验验证码
        String code = registerUserVo.getCode();
        String redisKey = Constant.CODE_PREFIX + ":" + registerUserVo.getPhone();
        boolean isPass = loginAndRegisterService.verifyCode(code, redisKey);
        if (isPass) {
            // 注册用户
            R r = memberFeignService.register(registerUserVo);
            if (r.getCode() == 0) {
                // 删除验证码
                stringRedisTemplate.delete(redisKey);
                return "redirect:http://auth.gulimall.com/login";
            }
            paramsMap.put("exception", r.get("msg"));
        }
        paramsMap.put("code", "验证码校验错误");
        model.addAttribute("msg", paramsMap);
        return "register";
    }

    @PostMapping("/user/login")
    public String login(@Validated(LoginGroup.class) RegisterUserVo registerUserVo, Model model) {
        R r = memberFeignService.login(registerUserVo);
        if (r.getCode() == 0) {
            return "redirect:http://gulimall.com";
        } else {
            HashMap<String, Object> map = new HashMap<>();
            map.put("msg", r.get("msg"));
            model.addAttribute("result", map);
            return "login";
        }
    }


}
