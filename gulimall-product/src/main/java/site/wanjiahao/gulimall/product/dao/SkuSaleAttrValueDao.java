package site.wanjiahao.gulimall.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import site.wanjiahao.gulimall.product.entity.SkuSaleAttrValueEntity;
import site.wanjiahao.gulimall.product.vo.Attr;
import site.wanjiahao.gulimall.product.vo.SaleAttrVos;

import java.util.List;

/**
 * sku销售属性&值
 * 
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-15 21:07:34
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {

    List<SaleAttrVos> listSaleAttrBySkuIds(List<Long> skuIds);

    List<Attr> infoAttr(@Param("skuId") Long skuId);
}
