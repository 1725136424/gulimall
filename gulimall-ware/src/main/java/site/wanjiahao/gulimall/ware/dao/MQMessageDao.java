package site.wanjiahao.gulimall.ware.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import site.wanjiahao.gulimall.ware.entity.MQMessageEntity;

import java.util.List;

@Mapper
public interface MQMessageDao extends BaseMapper<MQMessageEntity> {

    List<MQMessageEntity> selectUndelivered();

}
