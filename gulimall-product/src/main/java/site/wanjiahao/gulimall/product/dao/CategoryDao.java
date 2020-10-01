package site.wanjiahao.gulimall.product.dao;

import site.wanjiahao.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 16:18:27
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
