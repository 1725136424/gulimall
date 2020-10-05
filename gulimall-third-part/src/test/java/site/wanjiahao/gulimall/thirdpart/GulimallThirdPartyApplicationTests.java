package site.wanjiahao.gulimall.thirdpart;

import com.aliyun.oss.OSS;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

@SpringBootTest
class GulimallThirdPartyApplicationTests {

    @Autowired
    private OSS ossClient;

    @Value("${spring.cloud.alicloud.oss.bucket-name}")
    private String bucketName;
    @Test
    void contextLoads() throws FileNotFoundException {
        ossClient.putObject(bucketName, "timg.jpg", new FileInputStream("C:\\Users\\ASUS\\Pictures\\timg.jpg"));
    }

}
