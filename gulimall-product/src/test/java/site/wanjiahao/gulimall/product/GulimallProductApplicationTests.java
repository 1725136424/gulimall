package site.wanjiahao.gulimall.product;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import site.wanjiahao.gulimall.product.entity.BrandEntity;
import site.wanjiahao.gulimall.product.service.BrandService;
import java.util.List;
import java.util.UUID;

@SpringBootTest
class GulimallProductApplicationTests {

    @Autowired
    private BrandService brandService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redisson;

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

    @Test
    void test1() {
        stringRedisTemplate.opsForValue().set("name", "万佳豪" + UUID.randomUUID());
        String name = stringRedisTemplate.opsForValue().get("name");
        System.out.println("之前保存的值为" + name);
    }

    @Test
    void test2() {
        System.out.println(redisson);
    }


}

class Test1 {
    @Test
    void test1() {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode("1725136424");
        System.out.println(encode);
    }

    @Test
    void test2() {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        boolean matches = bCryptPasswordEncoder.matches("13684819080",
                "$2a$10$T2NxQiBXVikCpjwLRUM09./wGE.FQGUAQsTtsydKoKMGsGnbXH69.");
        System.out.println(matches);
    }
}
