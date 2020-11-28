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
        List<String> imgStr = spuInfoVo.getDescription().stream().map(Description::getUrl).collect(Collectors.toList());
        String descriptionStr = StringUtils.join(imgStr, ",");
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
        // 判断当前是否是上架商品还是下架商品
        Integer publishStatus = spuInfoUpdateStatusVo.getPublishStatus();
        Long spuId = spuInfoUpdateStatusVo.getId();
        SpuInfoEntity spuInfoEntity = listById(spuId);
        // 查询当前spu所有sku的id
        List<Long> allSkuIds = skuInfoService.listIdsBySpuId(spuId);
        // 1.查询当前spu对应的sku信息
        // 查询当前不同颜色的基本产品id
        List<Long> colorPid = productAttrValueService.listPidByColor();
        List<Long> skuIds = new ArrayList<>();
        // 过滤出当前产品id
        for (Long aLong : colorPid) {
            for (Long allSkuId : allSkuIds) {
                if (aLong.equals(allSkuId)) {
                    skuIds.add(aLong);
                }
            }
        }
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.listByIds(skuIds);
        if (publishStatus.equals(0)) {
            // 下架 --> 从ES服务器中删除
            R res = esFeignService.delete(spuId);
            try {
                boolean data = (boolean) res.get("data");
                if (!data) {
                    log.error("检索服务远程调用异常");
                }
            } catch (Exception e) {
                log.error(e.toString());
            }
        } else {
            // 查询所有sku的库存信息
            Map<String, Boolean> data = null;
            boolean spuHasStock = false;
            try {
                // 当前sku对应是否有库存
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
                log.error("库存服务远程调用异常{}", e.toString());
            }
            // 上架 --> 保存ES服务器中
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
                // 查询当前商品是否存在库存
                Boolean aBoolean = finalData.get(sku.getSkuId().toString());
                product.setHasStock(aBoolean == null ? false : aBoolean);
                return product;
            }).collect(Collectors.toList());
            esProductMappingTo.setProducts(resProduct);
            esProductMappingTo.setSpuId(spuId);
            // 品牌
            BrandEntity brandEntity = brandService.listById(spuInfoEntity.getBrandId());
            brand.setBrandId(brandEntity.getBrandId());
            brand.setBrandImg(brandEntity.getLogo());
            brand.setBrandName(brandEntity.getName());
            esProductMappingTo.setBrand(brand);
            // 分类
            CategoryEntity categoryEntity = categoryService.listById(spuInfoEntity.getCatelogId());
            category.setCategoryId(categoryEntity.getCatId());
            category.setCategoryName(categoryEntity.getName());
            esProductMappingTo.setCategory(category);
            // 属性
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
            // 添加检索字段
            List<ESProductMappingTo.Product> products = esProductMappingTo.getProducts();
            ESProductMappingTo.Product product = products.get(0);
            esProductMappingTo.setSkuTitle(product.getSkuTitle());
            esProductMappingTo.setSkuPrice(product.getSkuPrice());
            esProductMappingTo.setSaleCount(product.getSaleCount());
            esProductMappingTo.setHotScore(product.getHotScore());
            esProductMappingTo.setSkuImg(product.getSkuImg());
            esProductMappingTo.setHasStock(spuHasStock);
            // 批量保存ES服务器中
            R saveRes = esFeignService.save(esProductMappingTo);
            boolean res = (boolean) saveRes.get("data");
            if (res) {
                log.info("保存成功");
            } else {
                throw new RuntimeException("ES保存异常");
            }
        }
        // 更新状态
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
            return listByIds(spuIds).stream().collect(Collectors.toMap(SpuInfoEntity::getId, (item) -> item));
        } else {
            return null;
        }
    }
}