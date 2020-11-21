package site.wanjiahao.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.Query;
import site.wanjiahao.gulimall.product.dao.SkuSaleAttrValueDao;
import site.wanjiahao.gulimall.product.entity.SkuSaleAttrValueEntity;
import site.wanjiahao.gulimall.product.service.SkuSaleAttrValueService;
import site.wanjiahao.gulimall.product.vo.Attr;
import site.wanjiahao.gulimall.product.vo.SaleAttrVos;

import java.util.List;
import java.util.Map;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SaleAttrVos> listSaleAttrBySkuIds(List<Long> skuIds) {
        return baseMapper.listSaleAttrBySkuIds(skuIds);
    }

    @Override
    public List<Attr> infoAttr(Long skuId) {
        return baseMapper.infoAttr(skuId);
    }
}