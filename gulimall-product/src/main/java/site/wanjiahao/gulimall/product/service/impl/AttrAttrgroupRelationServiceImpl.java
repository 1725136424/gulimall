package site.wanjiahao.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.wanjiahao.common.utils.Constant;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.Query;
import site.wanjiahao.gulimall.product.dao.AttrAttrgroupRelationDao;
import site.wanjiahao.gulimall.product.entity.AttrAttrgroupRelationEntity;
import site.wanjiahao.gulimall.product.entity.AttrEntity;
import site.wanjiahao.gulimall.product.entity.AttrGroupEntity;
import site.wanjiahao.gulimall.product.service.AttrAttrgroupRelationService;
import site.wanjiahao.gulimall.product.service.AttrGroupService;
import site.wanjiahao.gulimall.product.service.AttrService;
import site.wanjiahao.gulimall.product.vo.AttrGroupWithAttrVo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query<AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<AttrAttrgroupRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public List<AttrGroupWithAttrVo> listAttrGroupWithAttr(Integer type) {
        // 查询所有属性分组对应的属性
        List<Long> attrGroupId = baseMapper.selectUniqueGroupId();
        return attrGroupId.stream().map(item -> {
            AttrGroupWithAttrVo attrGroupWithAttrVo = new AttrGroupWithAttrVo();
            // 查询属性分组信息
            AttrGroupEntity attrGroupEntity = attrGroupService.listById(item);
            BeanUtils.copyProperties(attrGroupEntity, attrGroupWithAttrVo);
            // 查询属性分组对应的属性
            List<Long> attrIds = listAttrIdsByAttrGroupId(item);
            if (attrIds != null && attrIds.size() > 0) {
                QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
                wrapper.in("attr_id", attrIds);
                List<AttrEntity> attrEntities = attrService.list(wrapper);
                if (type.equals(0)) {
                    attrEntities.removeIf(attr -> !Constant.AttrType.BASE_ATTR.getCode().equals(attr.getAttrType()) &&
                            !Constant.AttrType.OTHER_ATTR.getCode().equals(attr.getAttrType()));
                } else {
                    attrEntities.removeIf(attr -> !Constant.AttrType.SALE_ATTR.getCode().equals(attr.getAttrType()) &&
                            !Constant.AttrType.OTHER_ATTR.getCode().equals(attr.getAttrType()));
                }
                attrGroupWithAttrVo.setAttrEntities(attrEntities);
            }
            return attrGroupWithAttrVo;
        }).filter(item -> item.getAttrEntities() != null && item.getAttrEntities().size() > 0)
                .collect(Collectors.toList());
    }

    private List<Long> listAttrIdsByAttrGroupId(Long attrGroupId) {
        return baseMapper.listAttrIdsByAttrGroupId(attrGroupId);
    }
}