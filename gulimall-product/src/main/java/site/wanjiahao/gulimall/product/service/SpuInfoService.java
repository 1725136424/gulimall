package site.wanjiahao.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.gulimall.product.entity.SpuInfoEntity;
import site.wanjiahao.gulimall.product.vo.SpuInfoUpdateStatusVo;
import site.wanjiahao.gulimall.product.vo.SpuInfoVo;

import java.util.Map;

/**
 * spu信息
 *
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-15 21:07:35
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuInfoVo spuInfoVo);

    void updatePublishStatus(SpuInfoUpdateStatusVo spuInfoUpdateStatusVo);

    SpuInfoEntity listById(Long spuId);
}

