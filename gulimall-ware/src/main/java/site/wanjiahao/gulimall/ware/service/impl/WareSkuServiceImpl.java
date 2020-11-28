package site.wanjiahao.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.wanjiahao.common.exception.StockNotEnoughException;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.Query;
import site.wanjiahao.gulimall.ware.dao.WareSkuDao;
import site.wanjiahao.gulimall.ware.entity.WareSkuEntity;
import site.wanjiahao.gulimall.ware.service.WareSkuService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isBlank(key)) {
            wrapper.eq("id", key)
                    .or()
                    .like("sku_name", key);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isBlank(wareId)) {
            wrapper.eq("ware_id", wareId);
        }
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isBlank(skuId)) {
            wrapper.eq("sku_id", skuId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public boolean listHasStockBySkuId(Long skuId) {
        Long count = baseMapper.listHasStockBySkuId(skuId);
        return count != null || count > 0;
    }

    @Override
    public Map<Long, Boolean> listHasAllStock() {
        List<WareSkuEntity> wareSkuEntities = baseMapper.listHasAllStock();
        return wareSkuEntities.stream().collect(Collectors.toMap(WareSkuEntity::getSkuId, item -> item.getStock() > 0));
    }

    @Override
    public Map<Long, Boolean> listStockMap(List<Long> skuIds) {
        return list(new QueryWrapper<WareSkuEntity>().in("sku_id", skuIds))
                .stream()
                .collect(Collectors.toMap(WareSkuEntity::getSkuId, (item) -> (item.getStock() - item.getStockLocked()) > 0));
    }

    @Transactional
    @Override
    public void lockStock(Map<Long, Integer> lockMap) {
        Set<Map.Entry<Long, Integer>> entries = lockMap.entrySet();
        for (Map.Entry<Long, Integer> entry : entries) {
            boolean isUpdate = false;
            Long skuId = entry.getKey();
            Integer num = entry.getValue();
            // TODO 性能调节 查询当前商品存在于那些仓库
            List<Long> wareIds = baseMapper.listWareBySkuId(skuId);
            for (Long wareId : wareIds) {
                // 更新库存
                int update = baseMapper.updateStock(skuId, wareId, num);
                if (update > 0) {
                    // 更新成功
                    isUpdate = true;
                    break;
                }
            }
            if (!isUpdate) {
                // 存在库存未锁定成功
                throw new StockNotEnoughException("库存不足 事务回滚");
            }
        }
    }

}