package site.wanjiahao.gulimall.thirdpart.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.cloud.alicloud.sms")
@Data
public class SMSProperties {

    private String accessKeyId;

    private String accessSecret;

    private String sysDomain;

    private String sysVersion;

    private String regionId;

    private String signName;

    private String templateCode;

}
