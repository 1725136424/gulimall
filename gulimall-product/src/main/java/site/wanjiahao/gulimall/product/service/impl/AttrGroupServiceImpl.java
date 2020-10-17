package site.wanjiahao.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.Query;
import site.wanjiahao.gulimall.product.dao.AttrGroupDao;
import site.wanjiahao.gulimall.product.entity.AttrAttrgroupRelationEntity;
import site.wanjiahao.gulimall.product.entity.AttrEntity;
import site.wanjiahao.gulimall.product.entity.AttrGroupEntity;
import site.wanjiahao.gulimall.product.service.AttrAttrgroupRelationService;
import site.wanjiahao.gulimall.product.service.AttrGroupService;
import site.wanjiahao.gulimall.product.service.AttrService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );
        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catId) {
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        if (catId != null && !catId.equals(-1L)) {
            wrapper.eq("catelog_id", catId);
        }
        String keywords = (String) params.get("key");
        if (!StringUtils.isBlank(keywords)) {
            wrapper.and((item) -> {
                item.eq("attr_group_id", keywords)
                        .or()
                        .like("attr_group_name", keywords)
                        .or()
                        .like("descript", keywords);
            });
        }
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageWithAttr(Map<String, Object> params, Long groupId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("attr_group_id", groupId);
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = attrAttrgroupRelationService.listByMap(map);
        if (attrAttrgroupRelationEntities != null && attrAttrgroupRelationEntities.size() > 0) {
            List<Long> attrGroupIds = attrAttrgroupRelationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
            QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
            wrapper.in("attr_id", attrGroupIds);
            IPage<AttrEntity> page = attrService.page(new Query<AttrEntity>().getPage(params), wrapper);
           return new PageUtils(page);
        }
        return null;
    }

    @Override
    public void deleteRelWithAttr(Long groupId, Long attrId) {
        baseMapper.deleteRelWithAttr(groupId, attrId);
    }

    @Override
    public void deleteRelWithAttr(Long groupId, Long[] attrIds) {
        for (Long attrId : attrIds) {
            deleteRelWithAttr(groupId, attrId);
        }
    }

    @Override
    public PageUtils listOtherAttrWithPage(Long catId, Map<String, Object> params) {
        List<AttrAttrgroupRelationEntity> list = attrAttrgroupRelationService.list();
        List<Long> attrIds = list.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("catelog_id", catId);
        if (attrIds.size() > 0) {
            // 非条件判断
            wrapper.notIn("attr_id", attrIds);
        }
        IPage<AttrEntity> page = attrService.page(new Query<AttrEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Override
    public void saveRel(Long attrGroupId, Long[] attrIds) {
        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
        attrAttrgroupRelationEntity.setAttrGroupId(attrGroupId);
        for (Long attrId : attrIds) {
            attrAttrgroupRelationEntity.setAttrId(attrId);
            attrAttrgroupRelationService.save(attrAttrgroupRelationEntity);
        }
    }

    @Override
    public AttrGroupEntity listById(Long attrGroupId) {
        return baseMapper.selectById(attrGroupId);
    }

    @Override
    public void removeRelation(List<Long> asList) {
        baseMapper.deleteBatchIds(asList);
        if (asList != null && asList.size() > 0) {
            // 删除关系
            QueryWrapper<AttrAttrgroupRelationEntity> wrapper = new QueryWrapper<>();
            wrapper.in("attr_group_id", asList);
            attrAttrgroupRelationService.remove(wrapper);
        }
    }
}