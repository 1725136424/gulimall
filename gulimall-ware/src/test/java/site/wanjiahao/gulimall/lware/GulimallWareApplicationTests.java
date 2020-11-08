package site.wanjiahao.gulimall.lware;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.gulimall.ware.GulimallWareApplication;
import site.wanjiahao.gulimall.ware.entity.PurchaseEntity;
import site.wanjiahao.gulimall.ware.service.PurchaseService;

@SpringBootTest(classes = GulimallWareApplication.class)
class GulimallWareApplicationTests {

    @Autowired
    private PurchaseService purchaseService;

    @Test
    void contextLoads() {
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(17L);
        purchaseEntity.setPhone("1234");
        purchaseService.updateById(purchaseEntity);
    }

    @Test
    void test() {
        R r = R.ok().put("data", 111);
        System.out.println(r);

    }

}
