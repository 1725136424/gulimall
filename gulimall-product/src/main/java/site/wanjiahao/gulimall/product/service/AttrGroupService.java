package site.wanjiahao.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.gulimall.product.entity.AttrGroupEntity;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 16:18:27
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catId);

    PageUtils queryPageWithAttr(Map<String, Object> params, Long groupId);

    void deleteRelWithAttr(Long groupId, Long attrId);

    void deleteRelWithAttr(Long groupId, Long[] attrIds);

    PageUtils listOtherAttrWithPage(Long catId, Map<String, Object> params);

    void saveRel(Long attrGroupId, Long[] attrId);

    AttrGroupEntity listById(Long attrGroupId);

    void removeRelation(List<Long> asList);
}

