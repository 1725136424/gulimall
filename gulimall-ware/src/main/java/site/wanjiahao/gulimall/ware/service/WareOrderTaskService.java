package site.wanjiahao.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.gulimall.ware.entity.WareOrderTaskEntity;

import java.util.Map;

/**
 * 库存工作单
 *
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 16:04:38
 */
public interface WareOrderTaskService extends IService<WareOrderTaskEntity> {

    PageUtils queryPage(Map<String, Object> params);

    WareOrderTaskEntity listById(Long taskId);

    WareOrderTaskEntity getByOrderSn(String orderSn);
}

