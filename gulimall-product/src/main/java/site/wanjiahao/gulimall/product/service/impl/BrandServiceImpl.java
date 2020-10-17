package site.wanjiahao.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.Query;
import site.wanjiahao.gulimall.product.dao.BrandDao;
import site.wanjiahao.gulimall.product.entity.BrandEntity;
import site.wanjiahao.gulimall.product.entity.CategoryBrandRelationEntity;
import site.wanjiahao.gulimall.product.entity.CategoryEntity;
import site.wanjiahao.gulimall.product.service.BrandService;
import site.wanjiahao.gulimall.product.service.CategoryBrandRelationService;
import site.wanjiahao.gulimall.product.service.CategoryService;
import site.wanjiahao.gulimall.product.vo.BrandRespVo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<BrandEntity> wrapper = new QueryWrapper<>();
        String keywords = (String) params.get("key");
        if (!StringUtils.isBlank(keywords)) {
            // 不是空白字符串 id 品牌名称 首字母
            wrapper.eq("brand_id", keywords)
                    .or()
                    .like("name", keywords)
                    .or()
                    .like("first_letter", keywords);
        }
        IPage<BrandEntity> page = this.page(new Query<BrandEntity>().getPage(params), wrapper);
        List<BrandEntity> brands = page.getRecords();
        List<BrandRespVo> brandRespVos = brands.stream().map((item) -> {
            BrandRespVo brandRespVo = new BrandRespVo();
            BeanUtils.copyProperties(item, brandRespVo);
            // 获取当前品牌的分类信息
            Long brandId = brandRespVo.getBrandId();
            CategoryBrandRelationEntity rel = categoryBrandRelationService.listByBrandId(brandId);
            if (rel != null) {
                Long catelogId = rel.getCatelogId();
                CategoryEntity categoryEntity = categoryService.listById(catelogId);
                brandRespVo.setCategory(categoryEntity);
            }
            return brandRespVo;
        }).collect(Collectors.toList());
        PageUtils pageUtils = new PageUtils(page);
        pageUtils.setList(brandRespVos);
        return pageUtils;
    }

    @Override
    public List<BrandEntity> listByCatId(Long catId) {
        List<Long> brandIds =  categoryBrandRelationService.listBrandIdsByCatId(catId);
       if (brandIds != null && brandIds.size() > 0) {
           QueryWrapper<BrandEntity> wrapper = new QueryWrapper<>();
           wrapper.in("brand_id", brandIds);
           return baseMapper.selectList(wrapper);
       }
        return null;
    }

    @Override
    public BrandEntity listById(Long brandId) {
        return baseMapper.selectById(brandId);
    }
}