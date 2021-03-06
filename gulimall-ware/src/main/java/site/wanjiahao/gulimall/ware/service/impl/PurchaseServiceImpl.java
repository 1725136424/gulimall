package site.wanjiahao.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.wanjiahao.common.utils.Constant;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.Query;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.gulimall.ware.dao.PurchaseDao;
import site.wanjiahao.gulimall.ware.entity.PurchaseDetailEntity;
import site.wanjiahao.gulimall.ware.entity.PurchaseEntity;
import site.wanjiahao.gulimall.ware.entity.WareSkuEntity;
import site.wanjiahao.gulimall.ware.feign.ProductFeignService;
import site.wanjiahao.gulimall.ware.service.PurchaseDetailService;
import site.wanjiahao.gulimall.ware.service.PurchaseService;
import site.wanjiahao.gulimall.ware.service.WareSkuService;
import site.wanjiahao.gulimall.ware.vo.DonePurchaseVo;
import site.wanjiahao.gulimall.ware.vo.Item;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Autowired
    private WareSkuService wareSkuService;

    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isBlank(key)) {
            wrapper.eq("id", key)
                    .or()
                    .eq("assignee_id", key)
                    .or()
                    .like("assignee_name", key);
        }
        String status = (String) params.get("status");
        if (!StringUtils.isBlank(status)) {
            wrapper.eq("status", status);
        }
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<PurchaseEntity> listUnReceive() {
        QueryWrapper<PurchaseEntity> wrapper = new QueryWrapper<>();
        wrapper.in("status", Constant.PurchaseStatus.NEW.getStatus(),
                Constant.PurchaseStatus.ASSIGN.getStatus());
        return baseMapper.selectList(wrapper);
    }

    @Transactional
    @Override
    public void assignUser(PurchaseEntity purchase) {
        // ?????????????????????????????????????????? ????????????????????????
        if (purchase.getStatus().equals(Constant.PurchaseStatus.NEW.getStatus())
                || purchase.getStatus().equals(Constant.PurchaseStatus.ASSIGN.getStatus())) {
            purchase.setStatus(Constant.PurchaseStatus.ASSIGN.getStatus());
            // ?????????????????????
            baseMapper.updateById(purchase);
            // ??????????????????????????????????????????????????????????????????
            Long id = purchase.getId();
            QueryWrapper<PurchaseDetailEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("purchase_id", id);
            List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailService.list(wrapper);
            if (purchaseDetailEntities != null && purchaseDetailEntities.size() > 0) {
                List<PurchaseDetailEntity> detailEntityList = purchaseDetailEntities.stream().peek(item -> item.setStatus(Constant.PurchaseStatus.ASSIGN.getStatus())).collect(Collectors.toList());
                purchaseDetailService.updateBatchById(detailEntityList);
            }
        } else {
            throw new IllegalArgumentException("?????????????????????");
        }

    }

    @Override
    public void receive(List<Long> purchaseIds) {
        // ??????????????????????????????????????????
        boolean isUpdate = true;
        List<PurchaseEntity> purchaseEntities = baseMapper.selectBatchIds(purchaseIds);
        for (PurchaseEntity purchaseEntity : purchaseEntities) {
            if (!purchaseEntity.getStatus().equals(Constant.PurchaseStatus.ASSIGN.getStatus())) {
                isUpdate = false;
                break;
            }
        }
        if (isUpdate) {
            // ??????????????????????????????
            List<PurchaseEntity> collect = purchaseEntities.stream().peek(item -> {
                item.setStatus(Constant.PurchaseStatus.RECEIVE.getStatus());
            }).collect(Collectors.toList());
            updateBatchById(collect);

            // ???????????????????????????????????????
            QueryWrapper<PurchaseDetailEntity> wrapper = new QueryWrapper<>();
            if (purchaseIds.size() > 0) {
                wrapper.in("purchase_id", purchaseIds);
            }
            List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailService.list(wrapper);
            if (purchaseDetailEntities != null && purchaseDetailEntities.size() > 0) {
                List<PurchaseDetailEntity> list = purchaseDetailEntities.stream().peek(item -> item.setStatus(Constant.PurchaseStatus.RECEIVE.getStatus())).collect(Collectors.toList());
                purchaseDetailService.updateBatchById(list);
            }
        } else {
            throw new IllegalArgumentException("????????????????????????");
        }
    }

    @Transactional
    @Override
    public void done(DonePurchaseVo donePurchaseVo) {
        Long purchaseId = donePurchaseVo.getId();
        PurchaseEntity purchaseEntity = baseMapper.selectById(purchaseId);
        Integer status = purchaseEntity.getStatus();
        if (status.equals(Constant.PurchaseStatus.RECEIVE.getStatus())) {
            BigDecimal  totalPrice = BigDecimal.ZERO;
            boolean isComplete = true;
            List<Item> items = donePurchaseVo.getItems();
            // ??????????????????????????????
            for (Item item : items) {
                if (!item.getStatus().equals(Constant.PurchaseStatus.COMPLETE.getStatus())) {
                    isComplete = false;
                    break;
                }
            }
            if (isComplete) {
                purchaseEntity.setStatus(Constant.PurchaseStatus.COMPLETE.getStatus());
            } else {
                purchaseEntity.setStatus(Constant.PurchaseStatus.FAIL.getStatus());
            }
            // ????????????????????????
            List<PurchaseDetailEntity> purchaseDetailEntityList = items.stream().map(item -> {
                Long itemId = item.getItemId();
                Integer itemStatus = item.getStatus();
                if (!StringUtils.isBlank(item.getReason())) {
                    log.info(item.getReason());
                }
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setSkuPrice(item.getPrice());
                purchaseDetailEntity.setId(itemId);
                purchaseDetailEntity.setStatus(itemStatus);
                return purchaseDetailEntity;
            }).collect(Collectors.toList());
            // ????????????
            purchaseDetailService.updateBatchById(purchaseDetailEntityList);
            // ??????
            List<Long> ids = purchaseDetailEntityList.stream().map(PurchaseDetailEntity::getId).collect(Collectors.toList());
            List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailService.listByIds(ids);
            // ???????????????????????????
            List<WareSkuEntity> wareSkuEntityList = purchaseDetailEntities.stream().map(item -> {
                Integer status1 = item.getStatus();
                Long skuId = item.getSkuId();
                Long wareId = item.getWareId();
                if (status1.equals(Constant.PurchaseStatus.COMPLETE.getStatus())) {
                    // ?????????????????????
                    QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
                    wrapper.eq("sku_id", skuId);
                    wrapper.eq("ware_id", wareId);
                    List<WareSkuEntity> list = wareSkuService.list(wrapper);
                    // ????????????????????????????????????
                    WareSkuEntity wareSkuEntity;
                    if (list != null && list.size() > 0) {
                        wareSkuEntity = list.get(0);
                    } else {
                        // ????????????????????????
                        wareSkuEntity = new WareSkuEntity();
                        wareSkuEntity.setSkuId(skuId);
                        wareSkuEntity.setWareId(wareId);
                        wareSkuEntity.setStock(0);
                        wareSkuEntity.setStockLocked(0);
                        try {
                            // ????????????????????????
                            R result = productFeignService.info(skuId);
                            if (result.getCode() == 0) {
                                Map<String, Object> data = (Map<String, Object>) result.get("skuInfo");
                                wareSkuEntity.setSkuName((String) data.get("skuName"));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            wareSkuEntity.setSkuName("");
                        }
                    }
                    // ????????????
                    wareSkuEntity.setStock(wareSkuEntity.getStock() + item.getSkuNum());
                    return wareSkuEntity;
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
            // ??????????????????
            wareSkuService.saveOrUpdateBatch(wareSkuEntityList);
            // ???????????????
            for (PurchaseDetailEntity purchaseDetailEntity : purchaseDetailEntities) {
                if (purchaseDetailEntity.getStatus().equals(Constant.PurchaseStatus.COMPLETE.getStatus())) {
                    totalPrice = totalPrice.add(purchaseDetailEntity.getSkuPrice().multiply(new BigDecimal(purchaseDetailEntity.getSkuNum())));
                }
            }
            // ?????????????????????
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setAmount(totalPrice);
            baseMapper.updateById(purchaseEntity);
        } else {
            throw new IllegalArgumentException("????????????????????????");
        }
    }

}