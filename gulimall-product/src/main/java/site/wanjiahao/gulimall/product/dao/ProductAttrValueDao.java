package site.wanjiahao.gulimall.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import site.wanjiahao.gulimall.product.entity.ProductAttrValueEntity;
import site.wanjiahao.gulimall.product.vo.SimpleAttrGroupWithAttrVo;

import java.util.List;

/**
 * spu属性值
 * 
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 16:18:27
 */
@Mapper
public interface ProductAttrValueDao extends BaseMapper<ProductAttrValueEntity> {

    List<Long> listPidByColor();

    List<SimpleAttrGroupWithAttrVo> listSimpleGroupAndAttr(Long spuId);
}
