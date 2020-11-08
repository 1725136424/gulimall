package site.wanjiahao.gulimall.search.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadTest {

    // 创建一个固定数量的线程池
    public static final ExecutorService service = Executors.newFixedThreadPool(10);

    public static Integer result = null;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main方法执行");
      /*  // 继承Thread
        Thread thread = new Thread01();
        thread.start();*/
      /*  // 实现Runnable
        Runnable01 runnable01 = new Runnable01();
        Thread thread1 = new Thread(runnable01);
        thread1.start();*/
     /*   // 实现Callable
        FutureTask<Integer> integerFutureTask = new FutureTask<>(new Callable01());
        Thread thread = new Thread(integerFutureTask);
        thread.start();
        // get方法是一个阻塞是等待方法
        Integer integer = integerFutureTask.get();
        System.out.println(integer);*/
        // 使用runnable得到返回结果
    /*    Runnable01 runnable01 = new Runnable01();
        FutureTask<Integer> integerFutureTask = new FutureTask<>(runnable01, result);
        Thread thread = new Thread(integerFutureTask);
        thread.start();
        Integer integer = integerFutureTask.get();
        System.out.println("获取Runnable中返回的结果:" + integer);*/
        /**
         * 线程池7大参数
         *     corePoolSize 核心线程数大小(一直存在)
         *     maximumPoolSize 最大线程数
         *     keepAliveTime 存活时间
         *     TimeUnit 时间单位
         *     BlockingQueue 阻塞队列（当前进入的线程 - 最大线程数）
         *     ThreadFactory 线程的创建工厂
         *     RejectedExecutionHandler 拒绝策略
         * 工作顺序
         *      1.线程池创建好后。准备好核心数量的线程池，准备接受任务
         *      2.如果核心线程数满了，就把进来的线程放入阻塞队列中，如果核心线程数有空闲，就去阻塞队列中获取
         *      3.如果阻塞队列满了，就直接开启新的线程执行，最大只能开到max指定的数量
         *      4.如果max满了，就会使用拒绝策略，拒绝接受任务
         *      5.max都执行完成，会在指定keepAliveTime 释放(max -core些线程)
         */
       /* ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10,
                200, 10,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(20000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());*/

     /*   Executors.newCachedThreadPool(); // 指定带有缓存的线程池 core是0 都可以回收
        Executors.newFixedThreadPool(10); // 指定固定数量的线程池  core=max 固定大小 不可以回收
        Executors.newScheduledThreadPool(5); // 定时任务线程池
        Executors.newSingleThreadExecutor(); // 一个一个执行线程*/
        System.out.println("main方法结束");
    }

    public static class Thread01 extends Thread {
        @Override
        public void run() {
            System.out.println("当前线程" + Thread.currentThread().getId());
            int i = 10 / 4;
            System.out.println("运行结果为：" + i);
        }
    }

    public static class Runnable01 implements Runnable {
        @Override
        public void run() {
            System.out.println("当前线程" + Thread.currentThread().getId());
            int i = 10 / 4;
            result = 18;
            System.out.println("运行结果为：" + i);
        }
    }

    public static class Callable01 implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            System.out.println("当前线程" + Thread.currentThread().getId());
            int i = 10 / 4;
            System.out.println("运行结果为：" + i);
            return i;
        }
    }

}

