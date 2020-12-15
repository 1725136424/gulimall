package site.wanjiahao.gulimall.ware.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.wanjiahao.gulimall.ware.dao.MQMessageDao;
import site.wanjiahao.gulimall.ware.entity.MQMessageEntity;
import site.wanjiahao.gulimall.ware.service.MQMessageService;

import java.util.List;

@Service("mqMessageServiceImpl")
public class MQMessageServiceImpl extends ServiceImpl<MQMessageDao, MQMessageEntity> implements MQMessageService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public MQMessageEntity listByOne(String messageId) {
        return baseMapper.selectById(messageId);
    }

    @Override
    public void intervalSendMsg() throws ClassNotFoundException {
        // 查询当前未发出的消息
        List<MQMessageEntity> mqMessageEntities = baseMapper.selectUndelivered();
        // 再次发送消息
        /*
        * TODO 可以预见 只要有发送消息的地方就存在，发送消息丢失的问题。
        *      业务庞大起来，发送消息的业务就多起来，因此很多地方都需要保证消息的可靠发送。
        *      所以我们应当考虑把发送消息做成一个单独的中间件供我们使用，这样就避免了不必要的冗余代码
        * */
        for (MQMessageEntity mqMessageEntity : mqMessageEntities) {
            String classType = mqMessageEntity.getClassType();
            String routingKey = mqMessageEntity.getRoutingKey();
            String toChange = mqMessageEntity.getToChange();
            String content = mqMessageEntity.getContent();
            // 装换实体类
            Class<?> aClass = Class.forName(classType);
            Object object = JSON.parseObject(content, aClass);
            // TODO 还是需要保证消息的可靠投递
            CorrelationData correlationData = new CorrelationData();
            correlationData.setId(mqMessageEntity.getMessageId());
            rabbitTemplate.convertAndSend(toChange, routingKey, object, correlationData);
        }

    }
}
