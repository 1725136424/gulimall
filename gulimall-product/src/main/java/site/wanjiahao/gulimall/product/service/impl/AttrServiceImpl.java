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
import site.wanjiahao.gulimall.product.dao.AttrDao;
import site.wanjiahao.gulimall.product.entity.AttrAttrgroupRelationEntity;
import site.wanjiahao.gulimall.product.entity.AttrEntity;
import site.wanjiahao.gulimall.product.entity.CategoryEntity;
import site.wanjiahao.gulimall.product.service.AttrAttrgroupRelationService;
import site.wanjiahao.gulimall.product.service.AttrService;
import site.wanjiahao.gulimall.product.service.CategoryService;
import site.wanjiahao.gulimall.product.vo.AttrRespVo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, String type, Long catId) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("attr_type", type);
        if (!catId.equals(-1L)) {
            // 当前分类下
            wrapper.eq("catelog_id", catId);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isBlank(key)) {
            wrapper.and((item) -> {
                item.eq("attr_id", key)
                        .or()
                        .like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                wrapper
        );
        List<AttrEntity> attrs = page.getRecords();
        List<AttrRespVo> attrRespVos = attrs.stream().map((item) -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(item, attrRespVo);
            Long catelogId = attrRespVo.getCatelogId();
            CategoryEntity categoryEntity = categoryService.listById(catelogId);
            attrRespVo.setCategory(categoryEntity);
            return attrRespVo;
        }).collect(Collectors.toList());
        PageUtils pageUtils = new PageUtils(page);
        pageUtils.setList(attrRespVos);
        return pageUtils;
    }

    @Override
    public AttrEntity listById(Long attrId) {
        return baseMapper.selectById(attrId);
    }

    @Override
    public void removeRelation(List<Long> asList) {
        baseMapper.deleteBatchIds(asList);
        if (asList != null && asList.size() > 0) {
            // 删除关系
            QueryWrapper<AttrAttrgroupRelationEntity> wrapper = new QueryWrapper<>();
            wrapper.in("attr_id", asList);
            attrAttrgroupRelationService.remove(wrapper);
        }
    }
}