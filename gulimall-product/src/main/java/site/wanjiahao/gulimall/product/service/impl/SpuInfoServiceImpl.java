package site.wanjiahao.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.wanjiahao.common.to.MemberPriceTo;
import site.wanjiahao.common.to.SkuFullReductionTo;
import site.wanjiahao.common.to.SkuLadderTo;
import site.wanjiahao.common.to.SpuBoundsTo;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.Query;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.gulimall.product.dao.SpuInfoDao;
import site.wanjiahao.gulimall.product.entity.*;
import site.wanjiahao.gulimall.product.feign.CouponFeignService;
import site.wanjiahao.gulimall.product.service.*;
import site.wanjiahao.gulimall.product.vo.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private SpuImagesService spuImagesService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        // 复杂查询
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        String brandId1 = (String) params.get("brandId");
        if (!StringUtils.isBlank(brandId1)) {
            wrapper.eq("brand_id", brandId1);
        }
        String catelogId1 = (String) params.get("catelogId");
        if (!StringUtils.isBlank(catelogId1)) {
            wrapper.eq("catelog_id", catelogId1);
        }
        String publishStatus = (String) params.get("publishStatus");
        if (!StringUtils.isBlank(publishStatus)) {
            wrapper.eq("publish_status", publishStatus);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isBlank(key)) {
            wrapper.and(item -> {
                item.eq("id", key)
                        .or()
                        .like("spu_name", key)
                        .or()
                        .like("spu_description", key);

            });
        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );
        List<SpuInfoEntity> records = page.getRecords();
        // 添加分类以及品牌信息
        List<SpuInfoWithBrandAndCategoryVo> collect = records.stream().map(item -> {
            SpuInfoWithBrandAndCategoryVo spuInfoWithBrandAndCategoryVo = new SpuInfoWithBrandAndCategoryVo();
            Long catelogId = item.getCatelogId();
            Long brandId = item.getBrandId();
            CategoryEntity categoryEntity = categoryService.listById(catelogId);
            BrandEntity brandEntity = brandService.listById(brandId);
            BeanUtils.copyProperties(item, spuInfoWithBrandAndCategoryVo);
            spuInfoWithBrandAndCategoryVo.setBrandEntity(brandEntity);
            spuInfoWithBrandAndCategoryVo.setCategoryEntity(categoryEntity);
            return spuInfoWithBrandAndCategoryVo;
        }).collect(Collectors.toList());
        PageUtils pageUtils = new PageUtils(page);
        pageUtils.setList(collect);
        return pageUtils;
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuInfoVo spuInfoVo) {
        // 1.保存spu基本信息 `pms_spu_info`
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuInfoVo, spuInfoEntity);
        spuInfoEntity.setPublishStatus(0);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        baseMapper.insert(spuInfoEntity);
        Long spuId = spuInfoEntity.getId();

        // 2.保存商品基本属性信息 `pms_product_attr_value`
        List<BaseAttr> baseAttr = spuInfoVo.getBaseAttr();
        ArrayList<ProductAttrValueEntity> productAttrValueEntities = new ArrayList<>();
        baseAttr.forEach(attr -> {
            List<AttrEntities> attrEntities = attr.getAttrEntities();
            attrEntities.forEach(item -> {
                ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
                productAttrValueEntity.setSpuId(spuId);
                BeanUtils.copyProperties(item, productAttrValueEntity);
                productAttrValueEntity.setQuickShow(item.getShowDesc());
                productAttrValueEntities.add(productAttrValueEntity);
            });
        });
        productAttrValueService.saveBatch(productAttrValueEntities);

        // 3.保存积分信息 `sms_spu_bounds`
        Bounds bounds = spuInfoVo.getBounds();
        if (bounds != null) {
            SpuBoundsTo spuBoundsTo = new SpuBoundsTo();
            BeanUtils.copyProperties(bounds, spuBoundsTo);
            spuBoundsTo.setSpuId(spuId);
            R couponResult = couponFeignService.save(spuBoundsTo);
            log.info(couponResult.toString());
        }

        // 4.保存描述图片信息 `pms_spu_info_desc` (json字符串保存)
        String descriptionStr = StringUtils.join(spuInfoVo.getDescription(), ",");
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuId);
        spuInfoDescEntity.setDecript(descriptionStr);
        spuInfoDescService.save(spuInfoDescEntity);

        // 5.保存介绍图片信息 `pms_spu_images` (一个一个保存图片)
        List<Images> images = spuInfoVo.getImages();
        List<SpuImagesEntity> spuImagesEntities = images.stream().map(image -> {
            SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
            spuImagesEntity.setSpuId(spuId);
            spuImagesEntity.setImgName(image.getName());
            spuImagesEntity.setImgUrl(image.getUrl());
            spuImagesEntity.setImgSort(image.getSort());
            spuImagesEntity.setDefaultImg(image.getDefaultImage());
            spuImagesEntity.setImgName(StringUtils.substringAfterLast(image.getUrl(), "_"));
            return spuImagesEntity;
        }).collect(Collectors.toList());
        spuImagesService.saveBatch(spuImagesEntities);

        // 6.保存sku信息
        List<SkuInfo> skuInfo = spuInfoVo.getSkuInfo();
        skuInfo.forEach(sku -> {
            // 6.1保存sku基本信息 `pms_sku_info`
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            BeanUtils.copyProperties(sku, skuInfoEntity);
            skuInfoEntity.setSpuId(spuId);
            skuInfoEntity.setSaleCount(0L);
            skuInfoEntity.setCatalogId(spuInfoVo.getCatelogId());
            skuInfoEntity.setBrandId(spuInfoVo.getBrandId());
            String imageUrl = "";
            List<SkuImages> skuImages = sku.getSkuImages();
            for (SkuImages skuImage: skuImages) {
                if (skuImage.getDefaultImage() == 1) {
                    imageUrl = skuImage.getUrl();
                    break;
                }
            }
            skuInfoEntity.setSkuDefaultImg(imageUrl);
            skuInfoService.save(skuInfoEntity);

            // 6.2保存满减折扣信息 `sms_sku_ladder`
            Lodder lodder = sku.getLodder();
            if (lodder != null) {
                SkuLadderTo skuLadderTo = new SkuLadderTo();
                BeanUtils.copyProperties(lodder, skuLadderTo);
                skuLadderTo.setSkuId(skuInfoEntity.getSkuId());
                R ladder = couponFeignService.save(skuLadderTo);
                log.info(ladder.toString());
            }

            // 6.3保存满减信息 `sms_sku_full_reduction`
            Reduction reduction = sku.getReduction();
            if (reduction != null) {
                SkuFullReductionTo skuFullReductionTo = new SkuFullReductionTo();
                BeanUtils.copyProperties(reduction, skuFullReductionTo);
                skuFullReductionTo.setSkuId(skuInfoEntity.getSkuId());
                R reductionResult = couponFeignService.save(skuFullReductionTo);
                log.info(reductionResult.toString());
            }

            // 6.4保存会员价格信息 `sms_member_price`
            List<MemberPrices> memberPrices = sku.getMemberPrices();
            if (memberPrices != null && memberPrices.size() > 0) {
                List<MemberPriceTo> memberPriceTos = memberPrices.stream().map(memberPrice -> {
                    MemberPriceTo memberPriceTo = new MemberPriceTo();
                    BeanUtils.copyProperties(memberPrice, memberPriceTo);
                    memberPriceTo.setSkuId(skuInfoEntity.getSkuId());
                    return memberPriceTo;
                }).collect(Collectors.toList());
                R memberResult = couponFeignService.save(memberPriceTos);
                log.info(memberResult.toString());
            }

            // 6.5保存sku图片信息 `pms_sku_images`
            List<SkuImages> skuImages1 = sku.getSkuImages();
            List<SkuImagesEntity> skuImagesEntities = skuImages1.stream().map(img -> {
                SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                skuImagesEntity.setSkuId(skuInfoEntity.getSkuId());
                skuImagesEntity.setDefaultImg(img.getDefaultImage());
                skuImagesEntity.setImgUrl(img.getUrl());
                skuImagesEntity.setImgSort(img.getSort());
                return skuImagesEntity;
            }).collect(Collectors.toList());
            skuImagesService.saveBatch(skuImagesEntities);

            // 6.6保存sku对应的销售属性信息 `pms_sku_sale_attr_value`
            Attr attr = sku.getAttr();
            SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
            skuSaleAttrValueEntity.setSkuId(skuInfoEntity.getSkuId());
            BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
            skuSaleAttrValueService.save(skuSaleAttrValueEntity);
        });

    }
}