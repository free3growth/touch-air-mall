package com.touch.air.mall.search.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newFixedThreadPool;


/**
 * 测试CompletableFuture
 *
 * @author: bin.wang
 * @date: 2021/1/20 08:40
 */
public class CompletableFutureTest {

    public static ExecutorService executorService = newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main...start...");
        /**
         * runAsync
         */
//        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("运行结果：" + i);
//        }, executorService);


        /**
         * supplyAsync
         * whenComplete
         * exceptionally
         * 方法成功完成后的处理
         */
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            //int i = 10 / 0; //模拟出现异常
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
            return i;
        }, executorService).whenComplete((res, exception)->{
            //whenComplete 虽然可以得到异常信息，但是无法处理异常，修改返回结果
            System.out.println("异步任务成功完成..结果是："+res+",...异常是："+exception);
            //R apply(T t);
        }).exceptionally(throwable -> {
            //exceptionally 可以感知异常 同时返回默认值
            return 0;
        });
        //completableFuture.get()
        System.out.println("completableFuture...end..." + completableFuture.get());

        /**
         * handle
         * 执行完成后的处理，无论成功还是失败
         */
        System.out.println("handle...start...");
        CompletableFuture<Integer> handle = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            //int i = 10 / 0; //模拟出现异常
            int i = 10 / 4;
            System.out.println("运行结果：" + i);
            return i;
            // R apply(T t, U u);
        }, executorService).handle((res, exception) -> {
            if (res != null) {
                return res * 2;
            }
            if (exception != null) {
                return 0;
            }
            return -1;
        });
        System.out.println("handle...end..." + handle.get());


        /**
         * 串行化
         * 1. thenRun/thenRunAsync: 不能获取到上一步的执行结果
         *   thenRunAsync(() -> {
         *             //任务1 有返回值，但任务2不带返回值
         *             System.out.println("任务2启动了....");
         *         },executorService);
         * 2. thenAccept/thenAcceptAsync：能接收上一步的结果，但无返回值
         *  thenAcceptAsync((res) -> {
         *             //任务1 有返回值，但任务2不带返回值
         *             System.out.println("任务2启动了...."+res);
         *         });
         * 3. thenApply/thenApplyAsync：能接收上一步的结果，有返回值
         */
        System.out.println("thenApplyAsync...start...");
        CompletableFuture<String> thenApplyAsync = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            //int i = 10 / 0; //模拟出现异常
            int i = 10 / 3;
            System.out.println("运行结果：" + i);
            return i;
        }, executorService).thenApplyAsync((res -> {
            System.out.println("任务2启动了..." + "res:" + res);
            return "Hello,World" + res;
        }), executorService);
        //thenApplyAsync.get() 是个阻塞方法
        System.out.println("thenApplyAsync...end..." + thenApplyAsync.get());

        /**
         * 两个都完成
         * thenCombine
         * henAcceptBoth
         * runAfterBoth
         */
        CompletableFuture<Integer> future01 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1启动了...：" + Thread.currentThread().getId());
            //int i = 10 / 0; //模拟出现异常
            int i = 10 / 3;
            System.out.println("任务1结束：");
            return i;
        }, executorService);

        CompletableFuture<Integer> future02 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2启动了...：" + Thread.currentThread().getId());
            try {
                //模拟测试 只完成其中一个
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("任务2结束");
            return 0;
            //return "Hello";
        }, executorService);

        //1和2 结束之后，才会进行3
        //runAfterBothAsync 感知不到1和2的返回值
        //future01.runAfterBothAsync(future02, () -> {
        //    System.out.println("任务3开始...");
        //}, executorService);

        //thenAcceptBothAsync 可以获取任务1和2的返回值,无返回值
        //future01.thenAcceptBothAsync(future02, (res1,res2) -> {
        //   System.out.println("任务3开始..." + res1+"-->" + res2);
        //}, executorService);

        //thenCombineAsync 可以获取任务1和2的返回值,有返回值
        //CompletableFuture<String> thenCombineAsync = future01.thenCombineAsync(future02, (res1, res2) -> {
        //    System.out.println("任务3开始..." + res1+"-->" + res2);
        //    return res1 * 10 + res2;
        //}, executorService);
        //System.out.println("thenCombineAsync end..." + thenCombineAsync.get());

        /**
         * 两个任务只要有一个完成，就执行任务三
         * 1、runAfterEither 不感知任务1或2的结果，也没用返回值
         * 2、acceptEither    感知任务结果，自己没有返回值
         * 3、applyToEitherAsync 感知任务结果，并且有返回值
         */

        //runAfterEither
        //future01.runAfterEitherAsync(future02, ()->{
        //    System.out.println("任务3开始...");
        //}, executorService);

        //acceptEither
        //future01.acceptEitherAsync(future02, (res)->{
        //    System.out.println("任务3开始..."+res);
        //}, executorService);

        //CompletableFuture<Integer> applyToEitherAsync = future01.applyToEitherAsync(future02, (res) -> {
        //    System.out.println("任务3开始..." + res);
        //    return res * 10;
        //}, executorService);
        //System.out.println("applyToEitherAsync End..." + applyToEitherAsync.get());

        /**
         * 多任务组合
         * 1、
         * 2、
         */
        CompletableFuture<String> futureApple = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的图片信息");
            return "apple.png";
        }, executorService);

        CompletableFuture<String> futureAttr = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的属性");
            return "玫红+256GB";
        }, executorService);

        CompletableFuture<String> futureDesc = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的介绍");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "2020最新款";
        }, executorService);

        //futureApple.get();futureAttr.get();futureDesc.get(); //阻塞式等待
        //allOf 全部完成，才算结束
        //CompletableFuture<Void> allOf = CompletableFuture.allOf(futureApple, futureAttr, futureDesc);
        //等待所有结果完成
        //allOf.get();
        //System.out.println("main...end..."+ futureApple.get() + futureAttr.get() + futureDesc.get());

        //anyOf  只要有一个完成，就算结束
        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(futureApple, futureAttr, futureDesc);
        System.out.println("main...end..." + anyOf.get());

    }


}
