package site.wanjiahao.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.gulimall.coupon.entity.SeckillSessionEntity;

import java.util.List;
import java.util.Map;

/**
 * 秒杀活动场次
 *
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 15:55:38
 */
public interface SeckillSessionService extends IService<SeckillSessionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SeckillSessionEntity> getThreeSession();

    void publish(List<Long> sessionIds);
}

