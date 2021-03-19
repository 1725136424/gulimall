package site.wanjiahao.gulimall.coupon.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import site.wanjiahao.gulimall.coupon.entity.SeckillSessionEntity;

/**
 * 秒杀活动场次
 * 
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 15:55:38
 */
@Mapper
public interface SeckillSessionDao extends BaseMapper<SeckillSessionEntity> {

    void publish(Long id);
}
