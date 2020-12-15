package site.wanjiahao.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import site.wanjiahao.gulimall.ware.entity.MQMessageEntity;

public interface MQMessageService extends IService<MQMessageEntity> {

    MQMessageEntity listByOne(String messageId);

    void intervalSendMsg() throws ClassNotFoundException;
}
