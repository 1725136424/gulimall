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
import site.wanjiahao.common.to.*;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.Query;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.gulimall.product.dao.SpuInfoDao;
import site.wanjiahao.gulimall.product.entity.*;
import site.wanjiahao.gulimall.product.feign.CouponFeignService;
import site.wanjiahao.gulimall.product.feign.ESFeignService;
import site.wanjiahao.gulimall.product.feign.WareFeignService;
import site.wanjiahao.gulimall.product.service.*;
import site.wanjiahao.gulimall.product.vo.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
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

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private ESFeignService esFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        // ????????????
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
        // ??????????????????????????????
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
        // 1.??????spu???????????? `pms_spu_info`
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuInfoVo, spuInfoEntity);
        spuInfoEntity.setPublishStatus(0);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        baseMapper.insert(spuInfoEntity);
        Long spuId = spuInfoEntity.getId();

        // 2.?????????????????????????????? `pms_product_attr_value`
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

        // 3.?????????????????? `sms_spu_bounds`
        Bounds bounds = spuInfoVo.getBounds();
        if (bounds != null) {
            SpuBoundsTo spuBoundsTo = new SpuBoundsTo();
            BeanUtils.copyProperties(bounds, spuBoundsTo);
            spuBoundsTo.setSpuId(spuId);
            R couponResult = couponFeignService.save(spuBoundsTo);
            log.info(couponResult.toString());
        }

        // 4.???????????????????????? `pms_spu_info_desc` (json???????????????)
        List<String> imgStr = spuInfoVo.getDescription().stream().map(Description::getUrl).collect(Collectors.toList());
        String descriptionStr = StringUtils.join(imgStr, ",");
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuId);
        spuInfoDescEntity.setDecript(descriptionStr);
        spuInfoDescService.save(spuInfoDescEntity);

        // 5.???????????????????????? `pms_spu_images` (????????????????????????)
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

        // 6.??????sku??????
        List<SkuInfo> skuInfo = spuInfoVo.getSkuInfo();
        skuInfo.forEach(sku -> {
            // 6.1??????sku???????????? `pms_sku_info`
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            BeanUtils.copyProperties(sku, skuInfoEntity);
            skuInfoEntity.setSpuId(spuId);
            skuInfoEntity.setSaleCount(0L);
            skuInfoEntity.setCatelogId(spuInfoVo.getCatelogId());
            skuInfoEntity.setBrandId(spuInfoVo.getBrandId());
            String imageUrl = "";
            List<SkuImages> skuImages = sku.getSkuImages();
            for (SkuImages skuImage: skuImages) {
                if (skuImage.getDefaultImage().equals(1)) {
                    imageUrl = skuImage.getUrl();
                    break;
                }
            }
            skuInfoEntity.setSkuDefaultImg(imageUrl);
            skuInfoService.save(skuInfoEntity);

            // 6.2???????????????????????? `sms_sku_ladder`
            Lodder lodder = sku.getLodder();
            if (lodder != null) {
                SkuLadderTo skuLadderTo = new SkuLadderTo();
                BeanUtils.copyProperties(lodder, skuLadderTo);
                skuLadderTo.setSkuId(skuInfoEntity.getSkuId());
                R ladder = couponFeignService.save(skuLadderTo);
                log.info(ladder.toString());
            }

            // 6.3?????????????????? `sms_sku_full_reduction`
            Reduction reduction = sku.getReduction();
            if (reduction != null) {
                SkuFullReductionTo skuFullReductionTo = new SkuFullReductionTo();
                BeanUtils.copyProperties(reduction, skuFullReductionTo);
                skuFullReductionTo.setSkuId(skuInfoEntity.getSkuId());
                R reductionResult = couponFeignService.save(skuFullReductionTo);
                log.info(reductionResult.toString());
            }

            // 6.4???????????????????????? `sms_member_price`
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

            // 6.5??????sku???????????? `pms_sku_images`
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

            // 6.6??????sku??????????????????????????? `pms_sku_sale_attr_value`
            List<Attr> attrs = sku.getAttrs();
            List<SkuSaleAttrValueEntity> attrEntities = attrs.stream().map(item -> {
                SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                skuSaleAttrValueEntity.setSkuId(skuInfoEntity.getSkuId());
                BeanUtils.copyProperties(item, skuSaleAttrValueEntity);
                return skuSaleAttrValueEntity;
            }).collect(Collectors.toList());
            skuSaleAttrValueService.saveBatch(attrEntities);
        });

    }

    @Transactional
    @Override
    public void updatePublishStatus(SpuInfoUpdateStatusVo spuInfoUpdateStatusVo) {
        // ???????????????????????????????????????????????????
        Integer publishStatus = spuInfoUpdateStatusVo.getPublishStatus();
        Long spuId = spuInfoUpdateStatusVo.getId();
        SpuInfoEntity spuInfoEntity = listById(spuId);
        // ????????????spu??????sku???id
        List<Long> allSkuIds = skuInfoService.listIdsBySpuId(spuId);
        // 1.????????????spu?????????sku??????
        // ???????????????????????????????????????id
        List<Long> colorPid = productAttrValueService.listPidByColor();
        List<Long> skuIds = new ArrayList<>();
        // ?????????????????????id
        for (Long aLong : colorPid) {
            for (Long allSkuId : allSkuIds) {
                if (aLong.equals(allSkuId)) {
                    skuIds.add(aLong);
                }
            }
        }
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.listByIds(skuIds);
        if (publishStatus.equals(0)) {
            // ?????? --> ???ES??????????????????
            R res = esFeignService.delete(spuId);
            try {
                boolean data = (boolean) res.get("data");
                if (!data) {
                    log.error("??????????????????????????????");
                }
            } catch (Exception e) {
                log.error(e.toString());
            }
        } else {
            // ????????????sku???????????????
            Map<String, Boolean> data = null;
            boolean spuHasStock = false;
            try {
                // ??????sku?????????????????????
                R resStock = wareFeignService.listAllStock();
                data = (Map<String, Boolean>) resStock.get("data");
                Collection<Boolean> values = data.values();
                for (Boolean value : values) {
                    if (value) {
                        spuHasStock = true;
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("??????????????????????????????{}", e.toString());
            }
            // ?????? --> ??????ES????????????
            Map<String, Boolean> finalData = data;
            ESProductMappingTo esProductMappingTo = new ESProductMappingTo();
            ESProductMappingTo.Brand brand = new ESProductMappingTo.Brand();
            ESProductMappingTo.Category category = new ESProductMappingTo.Category();
            List<ESProductMappingTo.Product> resProduct = skuInfoEntities.stream().map(sku -> {
                ESProductMappingTo.Product product = new ESProductMappingTo.Product();
                product.setSkuId(sku.getSkuId());
                product.setSaleCount(sku.getSaleCount());
                product.setSkuImg(sku.getSkuDefaultImg());
                product.setSkuTitle(sku.getSkuTitle());
                product.setSkuPrice(sku.getPrice());
                product.setHotScore(0F);
                // ????????????????????????????????????
                Boolean aBoolean = finalData.get(sku.getSkuId().toString());
                product.setHasStock(aBoolean == null ? false : aBoolean);
                return product;
            }).collect(Collectors.toList());
            esProductMappingTo.setProducts(resProduct);
            esProductMappingTo.setSpuId(spuId);
            // ??????
            BrandEntity brandEntity = brandService.listById(spuInfoEntity.getBrandId());
            brand.setBrandId(brandEntity.getBrandId());
            brand.setBrandImg(brandEntity.getLogo());
            brand.setBrandName(brandEntity.getName());
            esProductMappingTo.setBrand(brand);
            // ??????
            CategoryEntity categoryEntity = categoryService.listById(spuInfoEntity.getCatelogId());
            category.setCategoryId(categoryEntity.getCatId());
            category.setCategoryName(categoryEntity.getName());
            esProductMappingTo.setCategory(category);
            // ??????
            List<ProductAttrValueEntity> baseAttrs = productAttrValueService.listBaseAttrBySpuId(spuId);
            List<ESProductMappingTo.Attr> attrs = baseAttrs.stream().map(item -> {
                ESProductMappingTo.Attr attr = new ESProductMappingTo.Attr();
                attr.setAttrId(item.getAttrId());
                attr.setAttrName(item.getAttrName());
                String[] splitValue = item.getAttrValue().split(",");
                attr.setAttrValue(Arrays.asList(splitValue));
                return attr;
            }).collect(Collectors.toList());
            esProductMappingTo.setAttrs(attrs);
            // ??????????????????
            List<ESProductMappingTo.Product> products = esProductMappingTo.getProducts();
            ESProductMappingTo.Product product = products.get(0);
            esProductMappingTo.setSkuTitle(product.getSkuTitle());
            esProductMappingTo.setSkuPrice(product.getSkuPrice());
            esProductMappingTo.setSaleCount(product.getSaleCount());
            esProductMappingTo.setHotScore(product.getHotScore());
            esProductMappingTo.setSkuImg(product.getSkuImg());
            esProductMappingTo.setHasStock(spuHasStock);
            // ????????????ES????????????
            R saveRes = esFeignService.save(esProductMappingTo);
            boolean res = (boolean) saveRes.get("data");
            if (res) {
                log.info("????????????");
            } else {
                throw new RuntimeException("ES????????????");
            }
        }
        // ????????????
        spuInfoEntity.setPublishStatus(spuInfoUpdateStatusVo.getPublishStatus());
        baseMapper.updateById(spuInfoEntity);
    }

    @Override
    public SpuInfoEntity listById(Long spuId) {
        return baseMapper.selectById(spuId);
    }

    @Override
    public Map<Long, BigDecimal> getWeightBySpuIds(List<Long> spuIds) {
        return listByIds(spuIds).stream().collect(Collectors.toMap(SpuInfoEntity::getId, SpuInfoEntity::getWeight));
    }

    @Override
    public Map<Long, SpuInfoEntity> listSpuInfoMapByIds(List<Long> spuIds) {
        if (spuIds != null && spuIds.size() > 0) {
            return listByIds(spuIds).stream().collect(Collectors.toMap(SpuInfoEntity::getId, Function.identity()));
        } else {
            return null;
        }
    }
}