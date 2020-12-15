package site.wanjiahao.gulimall.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("mq_message")
public class MQMessageEntity {

    @TableId(type = IdType.INPUT)
    private String messageId;

    private String content;

    private String toChange;

    private String routingKey;

    private String classType;

    private Integer messageStatus;

    private Date createTime;

    private Date updateTime;

}
