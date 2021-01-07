package com.touch.air.mall.product;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileNotFoundException;

@RunWith(SpringRunner.class)
@SpringBootTest
class MallProductApplicationTests {
//    @Resource
//    private OSSClient ossClient;

    @Test
    void contextLoads() {
    }

    @Test
    public void ossTest() throws FileNotFoundException {

        String bucketName = "touch-air-mall";
        // <yourObjectName>上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg。
        String objectName = "华为mate40 pro.jpg";

        // 上传文件到指定的存储空间（bucketName）并将其保存为指定的文件名称（objectName）。
//        FileInputStream fileInputStream = new FileInputStream("D:\\谷粒商城\\docs\\pics\\28f296629cca865e.jpg");
//        ossClient.putObject(bucketName, objectName, fileInputStream);

        // 关闭OSSClient。
//        ossClient.shutdown();
        System.out.println("上传完成");
    }

}
