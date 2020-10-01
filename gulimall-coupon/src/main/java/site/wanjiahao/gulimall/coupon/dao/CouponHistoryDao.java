package site.wanjiahao.gulimall.coupon.dao;

import site.wanjiahao.gulimall.coupon.entity.CouponHistoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券领取历史记录
 * 
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 15:55:38
 */
@Mapper
public interface CouponHistoryDao extends BaseMapper<CouponHistoryEntity> {
	
}
