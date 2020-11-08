package site.wanjiahao.gulimall.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import site.wanjiahao.gulimall.product.entity.SkuInfoEntity;

import java.util.List;

/**
 * sku信息
 * 
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 16:18:27
 */
@Mapper
public interface SkuInfoDao extends BaseMapper<SkuInfoEntity> {

    List<Long> listIdsBySpuId(Long spuId);
}
