package site.wanjiahao.gulimall.search.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompletableFutureTest {

    public static final ExecutorService service = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("方法的开始");
        // 没有返回结果
      /*  CompletableFuture.runAsync(() -> {
            System.out.println("当前线程Id为" + Thread.currentThread().getId());
            int i = 10 / 4;
            System.out.println("线程执行结果为:" + i);
        }, service);*/
        // 带有返回结果
       /* CompletableFuture<Integer> integerCompletableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程Id为" + Thread.currentThread().getId());
            int i = 10 / 0;
            System.out.println("线程执行结果为:" + i);
            return i;
        }, service).whenComplete((res, e) -> {
            System.out.println("获取的结果是" + res);
            System.out.println("感知到的异常是" + e);
        }).exceptionally(e -> {
            System.out.println(e);
            return 50;
        });
        System.out.println("获取到supply的返回结果11" + integerCompletableFuture.get());*/
       /* CompletableFuture<Integer> integerCompletableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程Id为" + Thread.currentThread().getId());
            int i = 10 / 4;
            System.out.println("线程执行结果为:" + i);
            return i;
        }, service).handle((res, e) -> {
            System.out.println("感知到方法结束了");
            if (e == null) {
                return res;
            } else {
                return 0;
            }
        });
        System.out.println(integerCompletableFuture.get());*/
      /*  CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程Id为" + Thread.currentThread().getId());
            int i = 10 / 4;
            System.out.println("线程执行结果为:" + i);
            return i;
        }, service).thenApply((res) -> {
            System.out.println("获取到的一个方法的返回值" + res);
            System.out.println("第二个方法执行了");
            return 100;
        });
        System.out.println(future.get());*/

       /* CompletableFuture<Integer> future01 = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程Id为" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("第一个线程执行结果为:" + i);

            return 20;
        }, service);

        CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程Id为" + Thread.currentThread().getId());
            int i = 10 / 4;
            System.out.println("第二个线程执行结果为:" + i);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return i;
        }, service).acceptEither(future01, (res) -> {
            System.out.println(res);
            System.out.println("一个方法执行了");
        });*/

        CompletableFuture<String> future01 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("查询商品图片");
            return "test.jpg";
        }, service);

        CompletableFuture<String> future02 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("查询商品属性");
            return "黑色256G";
        }, service);

        CompletableFuture<String> future03 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("查询商品");
            return "华为++";
        }, service);

        CompletableFuture<Object> objectCompletableFuture = CompletableFuture.anyOf(future01, future02, future03);
        objectCompletableFuture.get();

        System.out.println("方法的结束");
    }
}
