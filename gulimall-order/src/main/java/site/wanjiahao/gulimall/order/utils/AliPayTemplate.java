package site.wanjiahao.gulimall.order.utils;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import com.alipay.easysdk.payment.page.models.AlipayTradePagePayResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import site.wanjiahao.gulimall.order.properties.AliPayProperties;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@EnableConfigurationProperties(AliPayProperties.class)
@Component
public class AliPayTemplate {

    @Autowired
    private AliPayProperties aliPayProperties;

    public String pagePay(String subject, String outTradeNo, String totalAmount, String returnUrl) throws Exception {
        // 1. 设置参数（全局只需设置一次）
        Factory.setOptions(getOptions());
        // 2. 发起API调用
        AlipayTradePagePayResponse response = Factory.Payment.Page()
                // 收单
                .optional("timeout_express", "60s")
                .pay(subject, outTradeNo, totalAmount, returnUrl);
        // 3. 处理响应或异常
        return response.body;
    }

    private Config getOptions() throws IOException {
        Config config = new Config();
        config.protocol = aliPayProperties.getProtocol();
        config.gatewayHost = aliPayProperties.getGatewayHost();
        config.signType = aliPayProperties.getSignType();
        config.appId = aliPayProperties.getAppId();
        // 为避免私钥随源码泄露，推荐从文件中读取私钥字符串而不是写入源码中
        InputStream resourceAsStream = AliPayTemplate.class.getClassLoader().getResourceAsStream(aliPayProperties.getMerchantPrivatePath());
        assert resourceAsStream != null;
        BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(resourceAsStream));
        config.merchantPrivateKey = inputStreamReader.readLine();
        //注：证书文件路径支持设置为文件系统中的路径或CLASS_PATH中的路径，优先从文件系统中加载，加载失败后会继续尝试从CLASS_PATH中加载
        config.merchantCertPath = aliPayProperties.getMerchantCertPath();
        config.alipayCertPath = aliPayProperties.getAlipayCertPath();
        config.alipayRootCertPath = aliPayProperties.getAlipayRootCertPath();
        //注：如果采用非证书模式，则无需赋值上面的三个证书路径，改为赋值如下的支付宝公钥字符串即可
//        config.alipayPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwGMEkwxv/1dfECorhpbX1dtp4PJfaxbX9H1Wz7vpbdXuzviNWNeuFc9fVpu9nK1chrbXgDpD8nJHmiryBMUm9zGbPrU+u/AsTsSekY72FEeC3kaHkLOUwFEgKh59wGkMvH3TLRZUXJVTfa4KZO6hA/0xclMLdH0E8R1ZZi+K2Z2VlFZNRtxrLIcxk2RSa3hSeyj23BKONFmTWxdRUi2QV1VYGAYoxl+WWFGLmQMu47YdLkkFJoCJiyNRyW1eigD7JIIUe50cWZpYiaftIeUWWZhK8vGHkUrTFMEvWVQtGlbcLEpI9PhZttZWbIW4Sk9+ZozyMuMKMCN9Qxgk6/yAqwIDAQAB";
        //可设置异步通知接收服务地址（可选）
        config.notifyUrl = aliPayProperties.getNotifyUrl();
        //可设置AES密钥，调用AES加解密相关接口时需要（可选）
//        config.encryptKey = "<-- 请填写您的AES密钥，例如：aa4BtZ4tspm2wnXLb1ThQA== -->";
        return config;
    }

}
