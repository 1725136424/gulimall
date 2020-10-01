package site.wanjiahao.gulimall.order.dao;

import site.wanjiahao.gulimall.order.entity.OrderSettingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单配置信息
 * 
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 16:00:12
 */
@Mapper
public interface OrderSettingDao extends BaseMapper<OrderSettingEntity> {
	
}
