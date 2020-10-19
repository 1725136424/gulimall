package site.wanjiahao.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.wanjiahao.common.utils.Constant;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.Query;
import site.wanjiahao.gulimall.ware.dao.PurchaseDetailDao;
import site.wanjiahao.gulimall.ware.entity.PurchaseDetailEntity;
import site.wanjiahao.gulimall.ware.entity.PurchaseEntity;
import site.wanjiahao.gulimall.ware.service.PurchaseDetailService;
import site.wanjiahao.gulimall.ware.service.PurchaseService;
import site.wanjiahao.gulimall.ware.vo.MergeVo;

import java.util.Collections;
import java.util.List;
import java.util.Map;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Autowired
    private PurchaseService purchaseService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<PurchaseDetailEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isBlank(key)) {
            wrapper.eq("id", key)
                    .or()
                    .eq("sku_id", key);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isBlank(wareId)) {
            wrapper.eq("ware_id", wareId);
        }
        String status = (String) params.get("status");
        if (!StringUtils.isBlank(status)) {
            wrapper.eq("status", status);
        }
        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void merge(MergeVo mergeVo) {
        boolean isUpdate = true;
        // 判断当前所欲采购单项的状态是否为新生或者已经分配
        List<Long> purchaseDetailIds = mergeVo.getPurchaseDetailIds();
        List<PurchaseDetailEntity> purchaseDetailEntities = baseMapper.selectBatchIds(purchaseDetailIds);
        for (int i = 0; i < purchaseDetailEntities.size(); i++) {
            PurchaseDetailEntity purchaseDetailEntity = purchaseDetailEntities.get(i);
            Integer status = purchaseDetailEntity.getStatus();
            if (!status.equals(Constant.PurchaseStatus.NEW.getStatus())
                    && !status.equals(Constant.PurchaseStatus.ASSIGN.getStatus())) {
                isUpdate = false;
                break;
            }
        }
        if (isUpdate) {
            boolean flag = false;
            Long purchaseId = mergeVo.getPurchaseId();
            if (purchaseId != null) {
                List<PurchaseEntity> purchaseEntities = purchaseService.listByIds(Collections.singletonList(purchaseId));
                if (purchaseEntities != null && purchaseEntities.size() > 0) {
                    PurchaseEntity purchaseEntity = purchaseEntities.get(0);
                    if (purchaseEntity.getStatus() == Constant.PurchaseStatus.ASSIGN.getStatus()) {
                        flag = true;
                    }
                }
            } else {
                // 创建需求单合并
                PurchaseEntity purchaseEntity = new PurchaseEntity();
                purchaseEntity.setPriority(1);
                purchaseEntity.setStatus(Constant.PurchaseStatus.NEW.getStatus());
                purchaseService.save(purchaseEntity);
                purchaseId = purchaseEntity.getId();
            }
            // 合并
            for (PurchaseDetailEntity purchaseEntity: purchaseDetailEntities) {
                // 修改采购单项状态
                purchaseEntity.setPurchaseId(purchaseId);

                if (flag) {
                    purchaseEntity.setStatus(Constant.PurchaseStatus.ASSIGN.getStatus());
                }
                baseMapper.updateById(purchaseEntity);
            }
        } else {
            // 抛出异常
            throw new IllegalArgumentException("采购单状态错误(请选择新建或者分配的采购单)");
        }

    }
}