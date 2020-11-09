package site.wanjiahao.gulimall.product.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import site.wanjiahao.gulimall.product.entity.CategoryEntity;
import site.wanjiahao.gulimall.product.entity.SkuImagesEntity;
import site.wanjiahao.gulimall.product.entity.SkuInfoEntity;
import site.wanjiahao.gulimall.product.entity.SpuInfoDescEntity;
import site.wanjiahao.gulimall.product.service.*;
import site.wanjiahao.gulimall.product.vo.ItemResponseVo;
import site.wanjiahao.gulimall.product.vo.SaleAttrVos;
import site.wanjiahao.gulimall.product.vo.SimpleAttrGroupWithAttrVo;

import java.util.List;

/**
 * 页面跳转controller
 */
@Controller
public class PageController {

    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private SkuInfoService skuInfoService;
    
    @Autowired
    private SkuImagesService skuImagesService;
    
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    
    @Autowired
    private SpuInfoDescService spuInfoDescService;
    
    @Autowired
    private ProductAttrValueService productAttrValueService;
    
    @Autowired
    private SpuInfoService spuInfoService;

    @RequestMapping({"/", "/index.html"})
    public String home(Model model) {
        // 获取一级分类数据
        List<CategoryEntity> entities = categoryService.listCategoryByPcid(0L);
        model.addAttribute("categories", entities);
        return "index";
    }

    @GetMapping("/{skuId}.html")
    public String item(@PathVariable("skuId") Long skuId, Model model) {
        ItemResponseVo itemResponseVo = new ItemResponseVo();
        // 1.获取sku的基本信息 `pms_sku_info`
        SkuInfoEntity skuInfoEntity = skuInfoService.getById(skuId);
        
        // 2.获取sku的图片信息 `pms_sku_images`
        List<SkuImagesEntity> skuImagesEntities = skuImagesService.listBySkuId(skuId);

        // 3.获取sku的销售属性 `pms_sku_sale_attr_value`
            // 3.1 查询当前spu下所有的sku ids信息
        List<Long> skuIds = skuInfoService.listIdsBySpuId(skuInfoEntity.getSpuId());
            // 3.2 查询sku下所有的销售属性信息
        List<SaleAttrVos> saleAttrVos = skuSaleAttrValueService.listSaleAttrBySkuIds(skuIds);

        // 4.获取spu的描述信息 `pms_spu_info_desc`
        SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(skuInfoEntity.getSpuId());

        // 5.获取spu的基本属性信息 `pms_product_attr_value`
        List<SimpleAttrGroupWithAttrVo> simpleAttrGroupWithAttrVos = productAttrValueService.listSimpleGroupAndAttr(skuInfoEntity.getSpuId());
        itemResponseVo.setSkuInfoEntity(skuInfoEntity);
        itemResponseVo.setSkuImagesEntities(skuImagesEntities);
        itemResponseVo.setSaleAttrVos(saleAttrVos);
        itemResponseVo.setSpuInfoDescEntity(spuInfoDescEntity);
        itemResponseVo.setSimpleAttrGroupWithAttrVos(simpleAttrGroupWithAttrVos);
        model.addAttribute("item", itemResponseVo);
        return "item";
    }

}
