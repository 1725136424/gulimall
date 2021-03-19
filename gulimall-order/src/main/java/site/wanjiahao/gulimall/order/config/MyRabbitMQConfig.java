package site.wanjiahao.gulimall.order.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import site.wanjiahao.common.constant.OrderRabbitConstant;
import site.wanjiahao.common.constant.SeckillConstant;

import java.util.HashMap;

@Configuration
@Slf4j
public class MyRabbitMQConfig {

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // 库存服务交换机
    @Bean
    public Exchange wareEventExchange() {
        // topic交换机 String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        return new TopicExchange(OrderRabbitConstant.ORDER_EXCHANGE, true, false, null);
    }

    // 监听超时队列
    @Bean
    public Queue queue() {
        return new Queue(OrderRabbitConstant.ORDER_RELEASE_ORDER_QUEUE, false, false, false);
    }

    // 库存服务延迟队列
    @Bean
    public Queue wareDelayORDERQueue() {
        // String name, boolean durable, boolean exclusive, boolean autoDelete,
        //			@Nullable Map<String, Object> arguments
        //       map.put("x-message-ttl", 10000);//message在该队列queue的存活时间最大为10秒
        //       map.put("x-dead-letter-exchange", DELAY_EXCHANGE); //x-dead-letter-exchange参数是设置该队列的死信交换器（DLX）
        //       map.put("x-dead-letter-routing-key", DELAY_ROUTING_KEY);//x-dead-letter-routing-key参数是给这个DLX指定路由键
        HashMap<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", OrderRabbitConstant.X_MESSAGE_TTL);
        args.put("x-dead-letter-exchange", OrderRabbitConstant.X_DEAD_LETTER_EXCHANGE);
        args.put("x-dead-letter-routing-key", OrderRabbitConstant.X_DEAD_LETTER_ROUTING_KEY);
        return new Queue(OrderRabbitConstant.ORDER_DELAY_QUEUE, false, false, false, args);
    }

    // 创建绑定关系
    @Bean
    public Binding delayQueueBinding() {
        // String destination, DestinationType destinationType, String exchange, String routingKey,
        //			@Nullable Map<String, Object> arguments
        return new Binding(OrderRabbitConstant.ORDER_DELAY_QUEUE,
                Binding.DestinationType.QUEUE,
                OrderRabbitConstant.ORDER_EXCHANGE,
                OrderRabbitConstant.ORDER_DELAY_QUEUE,
                null);
    }

    @Bean
    public Binding releaseQueueBinding() {
        return new Binding(OrderRabbitConstant.ORDER_RELEASE_ORDER_QUEUE,
                Binding.DestinationType.QUEUE,
                OrderRabbitConstant.ORDER_EXCHANGE,
                "order.release.#",
                null);
    }

    // 秒杀服务 队列消锋
    @Bean
    public Queue seckillQueue() {
        /*
        * String name, boolean durable, boolean exclusive, boolean autoDelete,
			@Nullable Map<String, Object> arguments
        * */
        return new Queue(SeckillConstant.SECKILL_QUEUE, true, false, false);
    }

    // 绑定关系
    @Bean
    public Binding seckillBinding() {
        /*
        * String destination, DestinationType destinationType, String exchange, String routingKey,
			@Nullable Map<String, Object> arguments
        * */
        return new Binding(SeckillConstant.SECKILL_QUEUE,
                Binding.DestinationType.QUEUE,
                OrderRabbitConstant.ORDER_EXCHANGE,
                SeckillConstant.SECKILL_QUEUE,
                null);
    }
}
