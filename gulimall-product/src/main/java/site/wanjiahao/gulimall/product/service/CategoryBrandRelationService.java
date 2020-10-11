package site.wanjiahao.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.gulimall.product.entity.CategoryBrandRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 16:18:27
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    CategoryBrandRelationEntity listByBrandId(Long brandId);

    void saveRel(CategoryBrandRelationEntity categoryBrandRelation);

     List<Long> listCatIdsByBrandId(Long brandId);

    void deleteByCatIdAndBrandId(Long catId, Long brandId);

}

