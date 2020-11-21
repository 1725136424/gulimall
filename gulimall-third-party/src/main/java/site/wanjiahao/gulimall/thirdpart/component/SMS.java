package site.wanjiahao.gulimall.thirdpart.component;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import site.wanjiahao.gulimall.thirdpart.properties.SMSProperties;

@Component
@EnableConfigurationProperties(SMSProperties.class)
public class SMS {

    @Autowired
    private SMSProperties smsProperties;

    public static final String SEND_ACTION = "SendSms";

    public CommonResponse sendSms(String action, String phone, String code) {
        DefaultProfile profile = DefaultProfile.getProfile(smsProperties.getRegionId(),
                smsProperties.getAccessKeyId(), smsProperties.getAccessSecret());
        IAcsClient client = new DefaultAcsClient(profile);
        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain(smsProperties.getSysDomain());
        request.setSysVersion(smsProperties.getSysVersion());
        request.setSysAction(action);
        request.putQueryParameter("RegionId", smsProperties.getRegionId());
        request.putQueryParameter("PhoneNumbers", phone);
        request.putQueryParameter("SignName", smsProperties.getSignName());
        request.putQueryParameter("TemplateCode", smsProperties.getTemplateCode());
        request.putQueryParameter("TemplateParam", "{\"code\": \"" + code + "\"}");
        try {
            return client.getCommonResponse(request);
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return null;
    }
}
