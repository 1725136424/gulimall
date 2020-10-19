package site.wanjiahao.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.Query;
import site.wanjiahao.gulimall.product.dao.ProductAttrValueDao;
import site.wanjiahao.gulimall.product.entity.AttrEntity;
import site.wanjiahao.gulimall.product.entity.AttrGroupEntity;
import site.wanjiahao.gulimall.product.entity.ProductAttrValueEntity;
import site.wanjiahao.gulimall.product.service.AttrAttrgroupRelationService;
import site.wanjiahao.gulimall.product.service.AttrGroupService;
import site.wanjiahao.gulimall.product.service.ProductAttrValueService;
import site.wanjiahao.gulimall.product.vo.AttrEntities;
import site.wanjiahao.gulimall.product.vo.BaseAttr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Autowired
    private AttrGroupService attrGroupService;
    
    @Autowired
    private AttrServiceImpl attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query<ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<ProductAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<BaseAttr> listAttrBySpuId(Long spuId) {
        // 获取当前产品下的所有属性
        QueryWrapper<ProductAttrValueEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("spu_id", spuId);
        List<ProductAttrValueEntity> productAttrValueEntities = baseMapper.selectList(wrapper);
        List<BaseAttr> baseAttrs = new ArrayList<>();
        productAttrValueEntities.forEach(item -> {
            Long attrId = item.getAttrId();
            // 查询基本属性
            AttrEntity attrEntity = attrService.listById(attrId);
            Long attrGroupId = attrAttrgroupRelationService.listAttrGroupIdByAttrId(attrId);
            AttrGroupEntity attrGroupEntity = attrGroupService.listById(attrGroupId);
            boolean exist = hasSameGroupEntity(baseAttrs, attrGroupEntity);
            // 属性对拷值实体类
            AttrEntities attrEntities = new AttrEntities();
            BeanUtils.copyProperties(attrEntity, attrEntities);
            attrEntities.setAttrValue(item.getAttrValue());
            attrEntities.setProductWithAttrId(item.getId());
            attrEntities.setShowDesc(item.getQuickShow());
            // 存在当前属性分组
            if (exist) {
                // 加入当前属性分组 -> 查询当前分组
                for (BaseAttr baseAttr : baseAttrs) {
                    if (baseAttr.getAttrGroupId().equals(attrGroupEntity.getAttrGroupId())) {
                        // 当前分组 -> 加入
                        baseAttr.getAttrEntities().add(attrEntities);
                    }
                }
            } else {
                // 新建一个baseAttr加入
                BaseAttr baseAttr = new BaseAttr();
                // 属性对拷
                BeanUtils.copyProperties(attrGroupEntity, baseAttr);
                baseAttr.getAttrEntities().add(attrEntities);
                baseAttrs.add(baseAttr);
            }
        });
        return baseAttrs;
    }

    // 判断当前是否有相同的基本属性分组
    private boolean hasSameGroupEntity(List<BaseAttr> baseAttrs, AttrGroupEntity attrGroupEntity) {
        Long attrGroupId = attrGroupEntity.getAttrGroupId();
        if (baseAttrs != null && baseAttrs.size() > 0) {
            for (BaseAttr baseAttr : baseAttrs) {
                if (baseAttr.getAttrGroupId().equals(attrGroupId)) {
                    return true;
                }
            }
        }
        return false;
    }

}