package site.wanjiahao.gulimall.thirdpart;

import com.aliyun.oss.OSS;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import site.wanjiahao.gulimall.thirdpart.component.SMS;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

@SpringBootTest
class GulimallThirdPartyApplicationTests {

    @Autowired
    private OSS ossClient;

    @Value("${spring.cloud.alicloud.oss.bucket-name}")
    private String bucketName;

    @Autowired
    private SMS sms;

    @Test
    void contextLoads() throws FileNotFoundException {
        ossClient.putObject(bucketName, "timg.jpg", new FileInputStream("C:\\Users\\ASUS\\Pictures\\timg.jpg"));
    }

    @Test
    void test4() {
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou",
                "*", "*");
        IAcsClient client = new DefaultAcsClient(profile);
        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        request.putQueryParameter("PhoneNumbers", "18307008805");
        request.putQueryParameter("SignName", "豪大大博客");
        request.putQueryParameter("TemplateCode", "SMS_205434954");
        request.putQueryParameter("TemplateParam", "{\"code\": \"666666\"}");
        try {
            CommonResponse response = client.getCommonResponse(request);
            System.out.println(response.getData());
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }

    @Test
    void test5() {
        CommonResponse commonResponse = sms.sendSms(SMS.SEND_ACTION, "18307008805", "777777");
        System.out.println(commonResponse);
    }
}

