package site.wanjiahao.gulimall.product.web;

import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.wanjiahao.gulimall.product.service.CategoryService;
import site.wanjiahao.gulimall.product.vo.IndexCategoryLevel2RespVo;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/index")
public class HomeController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedissonClient redisson;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 获取2级菜单和三级菜单数据
     */
    @RequestMapping("/json/catelog.json")
    public Map<String, List<IndexCategoryLevel2RespVo>> sendJson() {
        return categoryService.listCateLevel2();
    }

    // 可重入锁
    @GetMapping("/hello")
    public String hello() {
        RLock lock = redisson.getLock("lock");
        lock.lock(); // 加锁 阻塞等待
        try {
            Thread.sleep(30000);
            System.out.println("业务正在执行" + Thread.currentThread().getName());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return "hello";
    }

    // 读写锁
    @GetMapping("/write")
    public String write() {
        RReadWriteLock readWriteLock = redisson.getReadWriteLock("rw-lock");
        String str = UUID.randomUUID().toString();
        RLock rLock = readWriteLock.writeLock();
        try {
            rLock.lock();
            stringRedisTemplate.opsForValue().set("value", str);
            System.out.println("写入数据成功");
            Thread.sleep(15000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
        }
        return "success";
    }

    @GetMapping("/read")
    public String read() {
        RReadWriteLock readWriteLock = redisson.getReadWriteLock("rw-lock");
        RLock rLock = readWriteLock.readLock();
        String str = "";
        try {
            rLock.lock();
            str = stringRedisTemplate.opsForValue().get("value");
            System.out.println("读取数据成功");
            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
        }
        return str;
    }
    // 读写锁

    // 信号量
    @GetMapping("/park")
    public String park() throws InterruptedException {
        RSemaphore park = redisson.getSemaphore("park");
        // 获取一个信号量
        boolean b = park.tryAcquire();
        if (b) {
            return "park";
        } else {
            return "当前流量过大";
        }
    }

    @GetMapping("/go")
    public String go() throws InterruptedException {
        RSemaphore park = redisson.getSemaphore("park");
        // 获取一个信号量
        park.release();
        return "go";
    }
    // 信号量

    // 闭锁
    @GetMapping("/door")
    public String door() throws InterruptedException {
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.trySetCount(3);
        door.await();
        return "door";
    }

    @GetMapping("/goto")
    public String goto1(){
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.countDown();
        return "放假";
    }

}
