package site.wanjiahao.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.gulimall.ware.entity.PurchaseEntity;
import site.wanjiahao.gulimall.ware.vo.DonePurchaseVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 16:04:38
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<PurchaseEntity> listUnReceive();

    void assignUser(PurchaseEntity purchase);

    void receive(List<Long> purchaseIds);

    void done(DonePurchaseVo donePurchaseVo);
}

