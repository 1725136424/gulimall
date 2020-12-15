package site.wanjiahao.gulimall.ware.listener;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import site.wanjiahao.common.constant.WareRabbitConstant;
import site.wanjiahao.common.to.OrderEntityTo;
import site.wanjiahao.common.to.WareOrderTaskDetailTo;
import site.wanjiahao.gulimall.ware.service.WareSkuService;

import java.io.IOException;

@Component
@RabbitListener(queues = WareRabbitConstant.STOCK_RELEASE_STOCK_QUEUE)
@Slf4j
public class RabbitMQListener {

    @Autowired
    private WareSkuService wareSkuService;

    @RabbitHandler
    public void listenStockUnlock(WareOrderTaskDetailTo wareOrderTaskDetailTo,
                                  Message message,
                                  Channel channel) throws IOException {
        // 关闭消息的自动提交模式
        try {
            wareSkuService.unlock(wareOrderTaskDetailTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("库存消息服务异常{}, 消息重新入队", e.getMessage());
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

    // 订单服务，超时关单卡顿，导致订单状态更新不了，以至于库存一直不扣减状况
    @RabbitHandler
    public void listenStockUnlock(OrderEntityTo orderEntityTo,
                                  Message message,
                                  Channel channel) throws IOException {
        // 关闭消息的自动提交模式
        try {
            wareSkuService.unlock(orderEntityTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("库存消息服务异常{}, 消息重新入队", e.getMessage());
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

}
