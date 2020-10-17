package site.wanjiahao.gulimall.product.dao;

import site.wanjiahao.gulimall.product.entity.CategoryBrandRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 品牌分类关联
 * 
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 16:18:27
 */
@Mapper
public interface CategoryBrandRelationDao extends BaseMapper<CategoryBrandRelationEntity> {

    CategoryBrandRelationEntity selectByBrandId(Long brandId);

    void deleteByBrandId(Long brandId);

    List<Long> listCatIdsByBrandId(Long brandId);

    List<Long> listBrandIdsByCatId(Long catId);
}
