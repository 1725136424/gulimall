package site.wanjiahao.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.gulimall.order.entity.OrderEntity;
import site.wanjiahao.gulimall.order.vo.OrderResponseVo;
import site.wanjiahao.gulimall.order.vo.OrderSubmitVo;
import site.wanjiahao.gulimall.order.vo.SettleAccountsVo;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 16:00:12
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    SettleAccountsVo structureSettleAccountsVo() throws ExecutionException, InterruptedException;

    OrderResponseVo buildOrderResponseVo(OrderSubmitVo orderSubmitVo) throws ExecutionException, InterruptedException;
}

