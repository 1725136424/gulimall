package site.wanjiahao.gulimall.product.service.impl;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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

    private List<CategoryEntity> getChildren(CategoryEntity currentItem, List<CategoryEntity> allItem) {
        // 设置当前分类的子分类
        return allItem.stream()
                .filter((item) -> item.getParentCid().equals(currentItem.getCatId()))
                .peek((item) -> item.setChildren(getChildren(item, allItem)))
                .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
                .collect(Collectors.toList());
    }
}