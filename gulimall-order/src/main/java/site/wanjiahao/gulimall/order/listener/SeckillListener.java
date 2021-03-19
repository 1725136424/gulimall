package site.wanjiahao.gulimall.order.listener;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import site.wanjiahao.common.constant.SeckillConstant;
import site.wanjiahao.common.vo.SeckillSuccessVo;
import site.wanjiahao.gulimall.order.service.OrderService;

import java.io.IOException;

@RabbitListener(queues = SeckillConstant.SECKILL_QUEUE)
@Slf4j
@Component
public class SeckillListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void createOrder(SeckillSuccessVo seckillSuccessVo,
                            Message message,
                            Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("收到订单信息，正在准备创建订单");
            // 保存秒杀订单
            orderService.saveSeckillOrder(seckillSuccessVo);
            channel.basicAck(deliveryTag,false);
        } catch (Exception e) {
            e.printStackTrace();
            channel.basicReject(deliveryTag, true);
        }

    }

}
