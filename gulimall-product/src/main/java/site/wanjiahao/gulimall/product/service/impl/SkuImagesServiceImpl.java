package site.wanjiahao.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.Query;
import site.wanjiahao.gulimall.product.dao.SkuImagesDao;
import site.wanjiahao.gulimall.product.entity.SkuImagesEntity;
import site.wanjiahao.gulimall.product.service.SkuImagesService;

import java.util.List;
import java.util.Map;


@Service("skuImagesService")
public class SkuImagesServiceImpl extends ServiceImpl<SkuImagesDao, SkuImagesEntity> implements SkuImagesService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuImagesEntity> page = this.page(
                new Query<SkuImagesEntity>().getPage(params),
                new QueryWrapper<SkuImagesEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuImagesEntity> listBySkuId(Long skuId) {
        return baseMapper.selectList(new QueryWrapper<SkuImagesEntity>().eq("sku_id", skuId));
    }

}