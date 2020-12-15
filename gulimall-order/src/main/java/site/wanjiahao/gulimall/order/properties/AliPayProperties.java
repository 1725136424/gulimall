package site.wanjiahao.gulimall.order.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "alipay")
@Data
public class AliPayProperties {

    /**
     * 传输协议
     */
    private String protocol;

    /**
     * 请求网关主机地址
     */
    private String gatewayHost;

    /**
     * 加密算法
     */
    private String signType;

    /**
     * 商户id
     */
    private String appId;

    /**
     * 异步回调地址
     */
    private String notifyUrl;

    /**
     * 商户私钥路径
     */
    private String merchantPrivatePath;

    /**
     * 商户公钥证书路径
     */
    private String merchantCertPath;

    /**
     * 支付宝公钥证书路径
     */
    private String alipayCertPath;

    /**
     * 支付宝根证书路径
     */
    private String alipayRootCertPath;
}
