package site.wanjiahao.gulimall.product.vo;

import lombok.Data;
import site.wanjiahao.gulimall.product.entity.SkuImagesEntity;
import site.wanjiahao.gulimall.product.entity.SkuInfoEntity;
import site.wanjiahao.gulimall.product.entity.SpuInfoDescEntity;

import java.util.List;

@Data
public class ItemResponseVo {

    private SkuInfoEntity skuInfoEntity;

    private List<SkuImagesEntity> skuImagesEntities;

    private List<SaleAttrVos> saleAttrVos;

    private SpuInfoDescEntity spuInfoDescEntity;

    private List<SimpleAttrGroupWithAttrVo> simpleAttrGroupWithAttrVos;

    // 秒杀信息
    private SeckillSkuRelationEntity seckillSkuRelationEntity;
}
