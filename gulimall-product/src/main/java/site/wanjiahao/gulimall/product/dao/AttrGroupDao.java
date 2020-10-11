package site.wanjiahao.gulimall.product.dao;

import org.apache.ibatis.annotations.Param;
import site.wanjiahao.gulimall.product.entity.AttrGroupEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 属性分组
 * 
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 16:18:27
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    void deleteRelWithAttr(@Param("groupId") Long groupId, @Param("attrId") Long attrId);
}
