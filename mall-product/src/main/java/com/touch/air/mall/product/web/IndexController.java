package com.touch.air.mall.product.web;

import com.touch.air.mall.product.entity.CategoryEntity;
import com.touch.air.mall.product.service.CategoryService;
import com.touch.air.mall.product.vo.Catalog2Vo;
import org.redisson.api.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author: bin.wang
 * @date: 2021/1/7 16:51
 */
@Controller
public class IndexController {
    @Resource
    private CategoryService categoryService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {
        //TODO 查出所有的一级分类
        List<CategoryEntity> categoryEntityList = categoryService.getFirstLevelCategroys();
        //视图解析器进行拼串
        // classpath:/templates/  +返回值+  .html
        model.addAttribute("categorys", categoryEntityList);
        return "index";
    }

    //index/catalog.json
    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        Map<String, List<Catalog2Vo>> map = categoryService.getCatalogJson();
        return map;
    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        //1、获取一把锁，只要锁的名字一样，就是同一把锁
        RLock lock = redissonClient.getLock("my-lock");
        //2、加锁
        //阻塞式等待
        lock.lock();
        /**
         * Redisson解决了两大难点
         *
         * 1. 锁的自动续期，如果业务超长，运行期间会自动给锁续上新的30s，不用担心业务实际长，锁自动过期而被删除
         * 2. 加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，锁默认在30s以后自动删除
         */

        //自动解锁时间 一定要大于业务执行时间
        //问题：lock.lock(10, TimeUnit.SECONDS); 在锁时间到了以后，不会自动续期
        //1.1、如果我们传递了锁的超时时间，就发送给redis执行脚本，进行占锁，默认超时就是我们指定的时间
        //1.2、如果我们未指定超时时间，就使用30*1000【LockWatchdogTimeOut看门狗的默认时间】
        /**
         *  总结：只要占锁成功，就会启动一个定时任务【重新给锁设置过期时间，新的过期时间就是看门狗的时间】
         *  定时任务执行：【看门狗时间】/3
         */
//        lock.lock(10, TimeUnit.SECONDS);
        try {
            System.out.println("加锁成功，执行业务" + Thread.currentThread().getId());
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //3、解锁   假设解锁代码没有运行，redis会不会出现死锁
            System.out.println("释放锁...." + Thread.currentThread().getId());
            lock.unlock();
        }
        return "hello";
    }

    @ResponseBody
    @GetMapping("/write")
    public String writeValue() {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("rw-lock");
        String uuid = "";
        RLock writeLock = readWriteLock.writeLock();
        try {
            //1、改数据加写锁，读数据加读锁
            writeLock.lock();
            uuid = UUID.randomUUID().toString();
            Thread.sleep(30000);
            stringRedisTemplate.opsForValue().set("writeValue", uuid);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            writeLock.unlock();
        }
        return uuid;
    }

    @ResponseBody
    @GetMapping("/read")
    public String readValue() {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("rw-lock");
        // 加读锁
        RLock readLock = readWriteLock.readLock();
        readLock.lock();
        String s = "";
        try {
            s = stringRedisTemplate.opsForValue().get("writeValue");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            readLock.unlock();
        }
        return s;
    }

    /**
     * 车位场景
     * 停车
     *
     * @return
     */

    @GetMapping("/park")
    @ResponseBody
    public String park() {
        RSemaphore semaphore = redissonClient.getSemaphore("park");
        //获取一个信号，获取一个值
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "ok";
    }

    /**
     * 车位场景
     * 离开
     *
     * @return
     */
    @GetMapping("/go")
    @ResponseBody
    public String go() {
        RSemaphore semaphore = redissonClient.getSemaphore("park");
        //释放一个信号，释放一个值
        semaphore.release();
        return "ok";
    }

    /**
     * 放假锁门
     * 1班没人了，2班没人了...
     * 5个班全部走完，我们才可以锁校门
     */
    @GetMapping("/lockDoor")
    @ResponseBody
    public String lockDoor() throws InterruptedException {

        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.trySetCount(5);
        //等待闭锁都完成
        door.await();

        return "放假了";
    }

    @GetMapping("/goHome/{id}")
    @ResponseBody
    public String goHome(@PathVariable("id") Long id){
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        //计数减一
        door.countDown();
        return id + "班的人都走完了";
    }


}

