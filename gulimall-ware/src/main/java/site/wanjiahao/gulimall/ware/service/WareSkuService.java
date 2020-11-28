package site.wanjiahao.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.gulimall.ware.entity.WareSkuEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 16:04:38
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    boolean listHasStockBySkuId(Long skuId);

    Map<Long, Boolean> listHasAllStock();

    Map<Long, Boolean> listStockMap(List<Long> skuIds);

    void lockStock(Map<Long, Integer> lockMap);
}

