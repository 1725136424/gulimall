package site.wanjiahao.gulimall.ware.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import site.wanjiahao.common.constant.MQConstant;
import site.wanjiahao.gulimall.ware.entity.MQMessageEntity;
import site.wanjiahao.gulimall.ware.service.MQMessageService;

import javax.annotation.PostConstruct;

@Configuration
@Slf4j
public class AckConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private MQMessageService mqMessageService;

    // 消息可靠发送配置
    @PostConstruct
    public void initAck() {
        System.out.println("111111111111111");
        /*     // 确认抵达Broker的回调
         *
         * correlationData 消息的关联表示
         * ack 消息是否成功抵达Broker boolean
         * cause 发生异常的原因*/

        rabbitTemplate.setConfirmCallback(((correlationData, ack, cause) -> {
            if (correlationData != null) {
                // 成功收到
                String id = correlationData.getId();
                MQMessageEntity mqMessageEntity = mqMessageService.listByOne(id);
                if (ack) {
                    if (mqMessageEntity != null && (mqMessageEntity.getMessageStatus() == MQConstant.MQStatus.NEW.getCode()
                            || mqMessageEntity.getMessageStatus() == MQConstant.MQStatus.SENT.getCode())) {
                        // 修改状态信息
                        mqMessageEntity.setMessageStatus(MQConstant.MQStatus.DELIVERY.getCode());
                        mqMessageService.updateById(mqMessageEntity);
                    }
                } else {
                    // 未成功收到
                    if (mqMessageEntity != null && (mqMessageEntity.getMessageStatus() == MQConstant.MQStatus.NEW.getCode()
                            || mqMessageEntity.getMessageStatus() == MQConstant.MQStatus.SENT.getCode())) {
                        // 修改状态信息
                        mqMessageEntity.setMessageStatus(MQConstant.MQStatus.ERROR_DELIVERED.getCode());
                        mqMessageService.updateById(mqMessageEntity);
                    }
                    log.error(cause);
                }
            }
        }));

        // 抵达Broker，但是错误入队回调
        /*      *
         *message 错误的消息
         *replyCode 回复的状态码
         *replyText 回复的文本内容
         *exchange 交换机
         *routingKey 路由键*/

        rabbitTemplate.setReturnCallback(((message, replyCode, replyText, exchange, routingKey) -> {
            String correlationId = message.getMessageProperties().getHeader("spring_returned_message_correlation");
            MQMessageEntity mqMessageEntity = mqMessageService.listByOne(correlationId);
            if (mqMessageEntity != null &&
                    (mqMessageEntity.getMessageStatus() == MQConstant.MQStatus.NEW.getCode()
                            || mqMessageEntity.getMessageStatus() == MQConstant.MQStatus.SENT.getCode())) {
                // 修改状态信息
                mqMessageEntity.setMessageStatus(MQConstant.MQStatus.ERROR_DELIVERED.getCode());
                mqMessageService.updateById(mqMessageEntity);
            }
        }));
    }

}
