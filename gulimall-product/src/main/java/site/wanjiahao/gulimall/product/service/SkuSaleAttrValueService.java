package site.wanjiahao.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.gulimall.product.entity.SkuSaleAttrValueEntity;

import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-15 21:07:34
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

