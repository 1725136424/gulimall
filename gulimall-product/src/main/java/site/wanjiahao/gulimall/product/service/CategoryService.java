package site.wanjiahao.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.gulimall.product.entity.CategoryEntity;
import site.wanjiahao.gulimall.product.vo.IndexCategoryLevel2RespVo;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 16:18:27
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    List<CategoryEntity> listCategoryByPcid(Long pcid);

    void updateBatch(List<CategoryEntity> categoryEntities);

    List<Long> listCategoryPath(Long catelogId);

    CategoryEntity listById(Long catelogId);

    PageUtils listWithPageByBranId(Map<String, Object> params, Long brandId);

    Map<String, List<IndexCategoryLevel2RespVo>> listCateLevel2();

}

