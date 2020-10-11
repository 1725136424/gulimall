package site.wanjiahao.gulimall.product.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.Query;

import site.wanjiahao.gulimall.product.dao.CategoryBrandRelationDao;
import site.wanjiahao.gulimall.product.entity.CategoryBrandRelationEntity;
import site.wanjiahao.gulimall.product.service.CategoryBrandRelationService;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public CategoryBrandRelationEntity listByBrandId(Long brandId) {
        return baseMapper.selectByBrandId(brandId);
    }

    @Transactional
    @Override
    public void saveRel(CategoryBrandRelationEntity categoryBrandRelation) {
        // 删除当前品牌对应的分类
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();
        deleteByCatIdAndBrandId(catelogId, brandId);
        baseMapper.insert(categoryBrandRelation);
    }

    @Override
    public List<Long> listCatIdsByBrandId(Long brandId) {
        return baseMapper.listCatIdsByBrandId(brandId);
    }

    @Override
    public void deleteByCatIdAndBrandId(Long catId, Long brandId) {
        QueryWrapper<CategoryBrandRelationEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("catelog_id", catId);
        wrapper.eq("brand_id", brandId);
        baseMapper.delete(wrapper);
    }
}