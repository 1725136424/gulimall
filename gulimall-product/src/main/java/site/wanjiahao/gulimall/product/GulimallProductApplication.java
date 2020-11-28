package site.wanjiahao.gulimall.product;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.util.ArrayList;

@EnableFeignClients
@SpringBootApplication
@EnableDiscoveryClient
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}

class Test1 {

    @Test
    void test1() {
        ArrayList<Boolean> objects = new ArrayList<>();
        objects.add(true);
        objects.add(false);
        objects.add(true);
        objects.add(true);
        objects.add(true);
    /*    objects.stream().filter(Boolean::booleanValue).forEach(item -> {
            item = false;
        });*/
        for (Boolean object : objects) {
            System.out.println(object.toString());
        }
    }
}
