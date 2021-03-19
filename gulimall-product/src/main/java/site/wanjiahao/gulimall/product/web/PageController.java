package site.wanjiahao.gulimall.product.web;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.gulimall.product.entity.CategoryEntity;
import site.wanjiahao.gulimall.product.entity.SkuImagesEntity;
import site.wanjiahao.gulimall.product.entity.SkuInfoEntity;
import site.wanjiahao.gulimall.product.entity.SpuInfoDescEntity;
import site.wanjiahao.gulimall.product.feign.SeckillFeignService;
import site.wanjiahao.gulimall.product.service.*;
import site.wanjiahao.gulimall.product.vo.ItemResponseVo;
import site.wanjiahao.gulimall.product.vo.SaleAttrVos;
import site.wanjiahao.gulimall.product.vo.SeckillSkuRelationEntity;
import site.wanjiahao.gulimall.product.vo.SimpleAttrGroupWithAttrVo;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

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
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private SeckillFeignService seckillFeignService;

    @RequestMapping({"/", "/index.html"})
    public String home(Model model) {
        // 获取一级分类数据
        List<CategoryEntity> entities = categoryService.listCategoryByPcid(0L);
        model.addAttribute("categories", entities);
        return "index";
    }

    @GetMapping("/{skuId}.html")
    public String item(@PathVariable("skuId") Long skuId, Model model) throws ExecutionException, InterruptedException {
        // 异步编排优化
        ItemResponseVo itemResponseVo = new ItemResponseVo();
        CompletableFuture<SkuInfoEntity> skuInfoEntityCompletableFuture = CompletableFuture.supplyAsync(() -> {
            // 1.获取sku的基本信息 `pms_sku_info`
            SkuInfoEntity skuInfoEntity = skuInfoService.getById(skuId);
            itemResponseVo.setSkuInfoEntity(skuInfoEntity);
            return skuInfoEntity;
        }, threadPoolExecutor);


        CompletableFuture<Void> skuImageFuture = CompletableFuture.runAsync(() -> {
            // 2.获取sku的图片信息 `pms_sku_images`
            List<SkuImagesEntity> skuImagesEntities = skuImagesService.listBySkuId(skuId);
            itemResponseVo.setSkuImagesEntities(skuImagesEntities);
        }, threadPoolExecutor);


        CompletableFuture<Void> skuInfoFuture = skuInfoEntityCompletableFuture.thenAcceptAsync((res) -> {
            // 3.获取sku的销售属性 `pms_sku_sale_attr_value`
            // 3.1 查询当前spu下所有的sku ids信息
            List<Long> skuIds = skuInfoService.listIdsBySpuId(res.getSpuId());
            // 3.2 查询sku下所有的销售属性信息
            List<SaleAttrVos> saleAttrVos = skuSaleAttrValueService.listSaleAttrBySkuIds(skuIds);
            itemResponseVo.setSaleAttrVos(saleAttrVos);
        }, threadPoolExecutor);


        CompletableFuture<Void> skuInfoDescFuture = skuInfoEntityCompletableFuture.thenAcceptAsync((res) -> {
            // 4.获取spu的描述信息 `pms_spu_info_desc`
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(res.getSpuId());
            itemResponseVo.setSpuInfoDescEntity(spuInfoDescEntity);
        }, threadPoolExecutor);


        CompletableFuture<Void> attrFuture = skuInfoEntityCompletableFuture.thenAcceptAsync((res) -> {
            // 5.获取spu的基本属性信息 `pms_product_attr_value`
            List<SimpleAttrGroupWithAttrVo> simpleAttrGroupWithAttrVos = productAttrValueService.listSimpleGroupAndAttr(res.getSpuId());
            itemResponseVo.setSimpleAttrGroupWithAttrVos(simpleAttrGroupWithAttrVos);
        }, threadPoolExecutor);

        // 查询该商品的秒杀信息
        CompletableFuture<Void> seckillFuture = CompletableFuture.runAsync(() -> {
            R r = seckillFeignService.seckillInfo(skuId);
            if (r.getCode() == 0) {
                SeckillSkuRelationEntity entity = JSON.parseObject(JSON.toJSONString(r.get("entity")), SeckillSkuRelationEntity.class);
                if (entity != null) {
                    // 判断当前时间是否为秒杀区间
                    long start = entity.getStartTime().getTime();
                    long end = entity.getEndTime().getTime();
                    long current = new Date().getTime();
                    if (current < start || current > end) {
                        // 置空随机码
                        entity.setRandomCode(null);
                    }
                    itemResponseVo.setSeckillSkuRelationEntity(entity);
                }
            }
        }, threadPoolExecutor);


        CompletableFuture<Void> allFuture = CompletableFuture.allOf(skuImageFuture,
                skuInfoFuture,
                skuInfoDescFuture,
                attrFuture,
                seckillFuture);

        // 等待所有结果完成
        allFuture.get();
        model.addAttribute("item", itemResponseVo);
        return "item";
    }

}
