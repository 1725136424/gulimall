package site.wanjiahao.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.gulimall.order.entity.OrderItemEntity;

import java.util.List;
import java.util.Map;

/**
 * 订单项信息
 *
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 16:00:12
 */
public interface OrderItemService extends IService<OrderItemEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<OrderItemEntity> listByOrderSn(String orderSn);
}

