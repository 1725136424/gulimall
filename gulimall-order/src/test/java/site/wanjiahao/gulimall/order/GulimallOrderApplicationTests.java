package site.wanjiahao.gulimall.order;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import site.wanjiahao.gulimall.order.utils.AliPayTemplate;

@SpringBootTest
@Slf4j
class GulimallOrderApplicationTests {

    @Autowired
    private AliPayTemplate aliPayTemplate;

    @Test
    void contextLoads() throws Exception {
        String out = aliPayTemplate.pagePay("华为Mate20", "23561456622211", "5678.00", "http://www.baidu.com");
        System.out.println(out);
    }

}

class Test1 {

    @Test
    public void test1() throws ClassNotFoundException {
        int[] ints = new int[]{1};
        System.out.println(ints.toString());
    }
}
