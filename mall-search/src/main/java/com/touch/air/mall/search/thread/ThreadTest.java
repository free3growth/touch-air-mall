package com.touch.air.mall.search.thread;

import java.util.concurrent.*;

/**
 * 线程初始化
 *
 * @author: bin.wang
 * @date: 2021/1/18 16:41
 */
public class ThreadTest {
    public static ExecutorService executorService = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main...start...");
        /**
         * 1、继承Thread
         * 2、实现Runnable接口
         * 3、实现Callable接口 + FutureTask （可以拿到返回结果，可以处理异常）
         * 4、线程池
         */
        //Thread
        Thread01 thread01 = new Thread01();
        //启动线程
        thread01.start();

        //Runnable
        Runnable01 runnable01 = new Runnable01();
        new Thread(runnable01).start();

        //Callable
        FutureTask<Integer> futureTask = new FutureTask<>(new Callable01());
        new Thread(futureTask).start();
        //阻塞等待整个线程执行完成，获取返回结果
        //Integer integer = futureTask.get();
        //System.out.println("main...end..." + integer);

        //线程池 给线程池直接提交任务
        //我们以后在业务代码里面，以上三种启动线程的方式都不用
        //【将所有的多线程异步任务交给线程池执行】
        //当前系统中池只有一两个，每个异步任务，提交给线程池，让它自己调度
        //可以控制资源，性能稳定
        //executorService.execute(new Runnable01());

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                5,
                200,
                10,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());

        System.out.println("main...end...");
    }

    public static class Thread01 extends Thread {
        @Override
        public void run() {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果："+i);
        }
    }

    public static class Runnable01 implements Runnable {

        @Override
        public void run() {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果："+i);
        }
    }

    public static class Callable01 implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果："+i);
            return i;
        }
    }


}
