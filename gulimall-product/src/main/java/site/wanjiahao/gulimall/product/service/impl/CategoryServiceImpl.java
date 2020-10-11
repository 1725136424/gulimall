package site.wanjiahao.gulimall.product.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.Query;

import site.wanjiahao.gulimall.product.dao.CategoryDao;
import site.wanjiahao.gulimall.product.entity.CategoryEntity;
import site.wanjiahao.gulimall.product.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationServiceImpl categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );
        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 1. 查询所有分类数据
        List<CategoryEntity> entities = baseMapper.selectList(null);
        // 2. 处理数据
        return entities.stream()
                .filter((item) -> item.getParentCid() == 0)
                .peek((item) -> item.setChildren(getChildren(item, entities)))
                .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryEntity> listCategoryByPcid(Long pcid) {
        QueryWrapper<CategoryEntity> query = new QueryWrapper<>();
        query.eq("parent_cid", pcid);
        return baseMapper.selectList(query);
    }

    @Override
    public void updateBatch(List<CategoryEntity> categoryEntities) {
        updateBatchById(categoryEntities);
        for (CategoryEntity categoryEntity : categoryEntities) {
            if (categoryEntity.getChildren() != null) {
                updateBatch(categoryEntity.getChildren());
            }
        }
    }

    @Override
    public List<Long> listCategoryPath(Long catelogId) {
        List<Long> longs = new ArrayList<>();
        collectCIds(catelogId, longs);
        Collections.reverse(longs);
        return longs;
    }

    @Override
    public CategoryEntity listById(Long catelogId) {
        return baseMapper.selectById(catelogId);
    }

    private void collectCIds(Long catelogId, List<Long> longs) {
        CategoryEntity categoryEntity = baseMapper.selectById(catelogId);
        if (categoryEntity != null) {
            longs.add(catelogId);
            collectCIds(categoryEntity.getParentCid(), longs);
        }
    }

    @Override
    public PageUtils listWithPageByBranId(Map<String, Object> params, Long brandId) {
        // 查询品牌下的所有分类
        List<Long> catIds = categoryBrandRelationService.listCatIdsByBrandId(brandId);
        QueryWrapper<CategoryEntity> wrapper = new QueryWrapper<>();
        if (catIds != null && catIds.size() > 0) {
            wrapper.in("cat_id", catIds);
            IPage<CategoryEntity> page = this.page(new Query<CategoryEntity>().getPage(params), wrapper);
            return new PageUtils(page);
        } else {
            return null;
        }
    }

    private List<CategoryEntity> getChildren(CategoryEntity currentItem, List<CategoryEntity> allItem) {
        // 设置当前分类的子分类
        return allItem.stream()
                .filter((item) -> item.getParentCid().equals(currentItem.getCatId()))
                .peek((item) -> item.setChildren(getChildren(item, allItem)))
                .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
                .collect(Collectors.toList());
    }
}