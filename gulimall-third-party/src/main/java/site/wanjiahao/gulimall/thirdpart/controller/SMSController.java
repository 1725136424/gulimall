package site.wanjiahao.gulimall.thirdpart.controller;

import com.alibaba.fastjson.JSON;
import com.aliyuncs.CommonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.wanjiahao.common.code.BizCodeEnum;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.gulimall.thirdpart.component.SMS;
import site.wanjiahao.gulimall.thirdpart.to.VerifyCodeTo;
import site.wanjiahao.gulimall.thirdpart.vo.SMSResponseVo;

@RestController
@RequestMapping("/third-party")
public class SMSController {

    @Autowired
    private SMS sms;

    @PostMapping("/sms/sendCode")
    public R sendCode(@RequestBody VerifyCodeTo verifyCodeTo) {
        CommonResponse commonResponse = sms.sendSms(SMS.SEND_ACTION, verifyCodeTo.getPhone(), verifyCodeTo.getCode());
        String data = commonResponse.getData();
        SMSResponseVo smsResponseVo = JSON.parseObject(data, SMSResponseVo.class);
        if ("OK".equals(smsResponseVo.getCode())) {
            return R.ok(smsResponseVo.getMessage());
        } else {
            return R.error(BizCodeEnum.SEND_CODE_EXCEPTION.getBizCode(), smsResponseVo.getMessage());
        }
    }
}
