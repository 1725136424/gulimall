package site.wanjiahao.gulimall.order.listener;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import site.wanjiahao.common.constant.OrderRabbitConstant;
import site.wanjiahao.gulimall.order.entity.OrderEntity;
import site.wanjiahao.gulimall.order.service.OrderService;

import java.io.IOException;

@Component
@RabbitListener(queues = OrderRabbitConstant.ORDER_RELEASE_ORDER_QUEUE)
@Slf4j
public class OrderListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void closeOrder(OrderEntity orderEntity,
                           Message message,
                           Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        // 定时关单
        try {
            orderService.closeOrder(orderEntity);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("定时关单出现错误{}, 消息重新入队", e.getMessage());
            channel.basicReject(deliveryTag, true);
        }
    }
}
