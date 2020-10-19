package site.wanjiahao.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.Query;
import site.wanjiahao.gulimall.product.dao.SkuInfoDao;
import site.wanjiahao.gulimall.product.entity.SkuInfoEntity;
import site.wanjiahao.gulimall.product.service.SkuInfoService;

import java.util.Map;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        // 复杂查询
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isBlank(brandId)) {
            wrapper.eq("brand_id", brandId);
        }
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isBlank(catelogId)) {
            wrapper.eq("catelog_id", catelogId);
        }
        String minPrice = (String) params.get("minPrice");
        String maxPrice = (String) params.get("maxPrice");
        if (!StringUtils.isBlank(minPrice) && !StringUtils.isBlank(maxPrice)) {
            if (!"0".equals(minPrice) && "0".equals(maxPrice)) {
                wrapper.ge("price", minPrice);
            }
            if (!"0".equals(maxPrice)) {
                wrapper.ge("price", minPrice)
                        .le("price", maxPrice);
            }
        }
        String key = (String) params.get("key");
        if (!StringUtils.isBlank(key)) {
            wrapper.and(item -> {
                item.eq("sku_id", key)
                        .or()
                        .like("sku_name", key)
                        .or()
                        .like("sku_title", key);

            });
        }
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

}