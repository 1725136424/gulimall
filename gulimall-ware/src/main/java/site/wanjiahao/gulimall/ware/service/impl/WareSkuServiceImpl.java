package site.wanjiahao.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.wanjiahao.common.constant.WareRabbitConstant;
import site.wanjiahao.common.constant.OrderStatusEnum;
import site.wanjiahao.common.constant.WareOrderTakDetailConstant;
import site.wanjiahao.common.exception.StockNotEnoughException;
import site.wanjiahao.common.to.LockStockTo;
import site.wanjiahao.common.to.OrderEntityTo;
import site.wanjiahao.common.to.WareOrderTaskDetailTo;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.Query;
import site.wanjiahao.gulimall.ware.dao.WareSkuDao;
import site.wanjiahao.gulimall.ware.entity.WareOrderTaskDetailEntity;
import site.wanjiahao.gulimall.ware.entity.WareOrderTaskEntity;
import site.wanjiahao.gulimall.ware.entity.WareSkuEntity;
import site.wanjiahao.gulimall.ware.feign.OrderFeignService;
import site.wanjiahao.gulimall.ware.service.WareOrderTaskDetailService;
import site.wanjiahao.gulimall.ware.service.WareOrderTaskService;
import site.wanjiahao.gulimall.ware.service.WareSkuService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    private WareOrderTaskService wareOrderTaskService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private OrderFeignService orderFeignService;

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
    public void lockStock(LockStockTo lockStockTo) {
        Map<Long, Integer> lockMap = lockStockTo.getLockMap();
        Set<Map.Entry<Long, Integer>> entries = lockMap.entrySet();
        // ?????????????????????
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(lockStockTo.getOrderSn());
        wareOrderTaskService.save(wareOrderTaskEntity);
        for (Map.Entry<Long, Integer> entry : entries) {
            boolean isUpdate = false;
            Long skuId = entry.getKey();
            Integer num = entry.getValue();
            // TODO ???????????? ???????????????????????????????????????
            List<Long> wareIds = baseMapper.listWareBySkuId(skuId);
            for (Long wareId : wareIds) {
                // ????????????
                int update = baseMapper.updateStock(skuId, wareId, num);
                if (update > 0) {
                    // ????????????????????? ????????????
                    WareOrderTaskDetailTo wareOrderTaskDetailTo = saveStockItem(wareOrderTaskEntity.getId(), skuId, wareId, num);
                    // ???????????????????????????????????????
                    rabbitTemplate.convertAndSend(WareRabbitConstant.STOCK_EXCHANGE, WareRabbitConstant.STOCK_DELAY_QUEUE, wareOrderTaskDetailTo);
                    // ????????????
                    isUpdate = true;
                    break;
                }
            }
            if (!isUpdate) {
                // ???????????????????????????
                throw new StockNotEnoughException("???????????? ????????????");
            }
        }
    }

    @Transactional
    @Override
    public void unlock(WareOrderTaskDetailTo wareOrderTaskDetailTo) {
        // 1.??????????????????id????????? ?????????????????????????????? ??????????????????
        if (wareOrderTaskDetailTo != null) {
            Long id = wareOrderTaskDetailTo.getId();
            WareOrderTaskDetailEntity dataBaseEntity = wareOrderTaskDetailService.listById(id);
            // ??????????????????????????????????????????????????????
            if (dataBaseEntity != null && dataBaseEntity.getLockStatus().equals(WareOrderTakDetailConstant.OrderTaskDetail.LOCK.getCode())) {
                // ????????????????????????
                Long taskId = dataBaseEntity.getTaskId();
                WareOrderTaskEntity wareOrderTaskEntity = wareOrderTaskService.listById(taskId);
                String orderSn = wareOrderTaskEntity.getOrderSn();
                // ?????????????????? ??????????????????????????????????????????
                OrderEntityTo resOrder = orderFeignService.getByOrderSn(orderSn);
                if (resOrder == null) {
                    // ????????????????????????????????????????????????????????????
                    baseMapper.unlock(wareOrderTaskDetailTo.getSkuId(),
                            wareOrderTaskDetailTo.getWareId(),
                            wareOrderTaskDetailTo.getSkuNum());
                    // ????????????????????????????????????
                    dataBaseEntity.setLockStatus(WareOrderTakDetailConstant.OrderTaskDetail.UNLOCK.getCode());
                    wareOrderTaskDetailService.updateById(dataBaseEntity);
                } else {
                    // ????????????????????????????????????????????????????????????
                    if (resOrder.getStatus().equals(OrderStatusEnum.CANCLED.getCode())) {
                        baseMapper.unlock(wareOrderTaskDetailTo.getSkuId(),
                                wareOrderTaskDetailTo.getWareId(),
                                wareOrderTaskDetailTo.getSkuNum());
                        // ????????????????????????????????????
                        dataBaseEntity.setLockStatus(WareOrderTakDetailConstant.OrderTaskDetail.UNLOCK.getCode());
                        wareOrderTaskDetailService.updateById(dataBaseEntity);
                    }
                }
            }
        }
    }

    @Transactional
    @Override
    public void unlock(OrderEntityTo orderEntityTo) {
        String orderSn = orderEntityTo.getOrderSn();
        // ???????????????????????????
        WareOrderTaskEntity wareOrderTaskEntity = wareOrderTaskService.getByOrderSn(orderSn);
        if (wareOrderTaskEntity != null) {
            // ???????????????????????????
            Long id = wareOrderTaskEntity.getId();
            List<WareOrderTaskDetailEntity> wareOrderTaskDetailEntities = wareOrderTaskDetailService.listByTaskId(id);
            for (WareOrderTaskDetailEntity wareOrderTaskDetailEntity : wareOrderTaskDetailEntities) {
                baseMapper.unlock(wareOrderTaskDetailEntity.getSkuId(),
                        wareOrderTaskDetailEntity.getWareId(),
                        wareOrderTaskDetailEntity.getSkuNum());
                // ????????????????????????????????????
                wareOrderTaskDetailEntity.setLockStatus(WareOrderTakDetailConstant.OrderTaskDetail.UNLOCK.getCode());
                wareOrderTaskDetailService.updateById(wareOrderTaskDetailEntity);
            }
        }
        // ?????????????????????????????????????????????
    }

    private WareOrderTaskDetailTo saveStockItem(Long stockId, Long skuId, Long wareId, Integer num) {
        WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity();
        wareOrderTaskDetailEntity.setSkuId(skuId);
        wareOrderTaskDetailEntity.setSkuNum(num);
        wareOrderTaskDetailEntity.setTaskId(stockId);
        wareOrderTaskDetailEntity.setWareId(wareId);
        wareOrderTaskDetailEntity.setLockStatus(WareOrderTakDetailConstant.OrderTaskDetail.LOCK.getCode());
        wareOrderTaskDetailService.save(wareOrderTaskDetailEntity);
        WareOrderTaskDetailTo wareOrderTaskDetailTo = new WareOrderTaskDetailTo();
        BeanUtils.copyProperties(wareOrderTaskDetailEntity, wareOrderTaskDetailTo);
        return wareOrderTaskDetailTo;
    }

}