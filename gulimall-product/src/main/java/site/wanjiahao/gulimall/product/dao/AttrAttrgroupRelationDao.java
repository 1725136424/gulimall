package site.wanjiahao.gulimall.product.dao;

import org.apache.ibatis.annotations.Param;
import site.wanjiahao.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 属性&属性分组关联
 * 
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 16:18:27
 */
@Mapper
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {

    List<Long> listAttrIdsByAttrGroupId(@Param("attrGroupId") Long attrGroupId);

    List<Long> selectUniqueGroupId();
}
