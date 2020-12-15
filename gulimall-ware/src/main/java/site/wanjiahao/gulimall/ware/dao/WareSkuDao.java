package site.wanjiahao.gulimall.ware.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import site.wanjiahao.gulimall.ware.entity.WareSkuEntity;

import java.util.List;

/**
 * 商品库存
 * 
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 16:04:38
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    Long listHasStockBySkuId(Long skuId);

    List<WareSkuEntity> listHasAllStock();

    List<Long> listWareBySkuId(@Param("skuId") Long skuId);

    int updateStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("num") Integer num);

    void unlock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);
}
