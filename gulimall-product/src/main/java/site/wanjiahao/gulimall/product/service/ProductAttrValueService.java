package site.wanjiahao.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.gulimall.product.entity.ProductAttrValueEntity;
import site.wanjiahao.gulimall.product.vo.BaseAttr;
import site.wanjiahao.gulimall.product.vo.SimpleAttrGroupWithAttrVo;

import java.util.List;
import java.util.Map;

/**
 * spu属性值
 *
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 16:18:27
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<BaseAttr> listAttrBySpuId(Long spuId);

    List<Long> listPidByColor();

    List<ProductAttrValueEntity> listBaseAttrBySpuId(Long spuId);

    List<SimpleAttrGroupWithAttrVo> listSimpleGroupAndAttr(Long spuId);
}

