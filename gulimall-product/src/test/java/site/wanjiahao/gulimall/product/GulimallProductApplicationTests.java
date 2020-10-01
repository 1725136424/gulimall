package site.wanjiahao.gulimall.product;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import site.wanjiahao.gulimall.product.entity.BrandEntity;
import site.wanjiahao.gulimall.product.service.BrandService;
import site.wanjiahao.gulimall.product.service.ProductAttrValueService;

import java.util.List;

@SpringBootTest
class GulimallProductApplicationTests {

    @Autowired
    private BrandService brandService;

    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setDescript("测试");
        brandEntity.setName("111");
        brandService.save(brandEntity);
    }

    @Test
    void contextLoads1() {
        List<BrandEntity> name = brandService.list(new QueryWrapper<BrandEntity>().eq("name", "111"));
        name.forEach(System.out::println);
    }

}
