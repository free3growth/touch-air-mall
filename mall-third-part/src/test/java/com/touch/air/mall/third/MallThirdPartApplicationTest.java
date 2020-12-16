package com.touch.air.mall.third;

import com.aliyun.oss.OSSClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * @author: bin.wang
 * @date: 2020/12/16 16:33
 */
@SpringBootTest
public class MallThirdPartApplicationTest {
    @Resource
    private OSSClient ossClient;
    @Value("${spring.cloud.alicloud.oss.bucket}")
    private String bucketName;
    @Test
    public void oss() throws FileNotFoundException {
//        String bucketName = "touch-air-mall";
        // <yourObjectName>上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg。
        String objectName = "华为mate40 pro.jpg";

        // 上传文件到指定的存储空间（bucketName）并将其保存为指定的文件名称（objectName）。
        FileInputStream fileInputStream = new FileInputStream("D:\\谷粒商城\\docs\\pics\\23d9fbb256ea5d4a.jpg");
//        ossClient.putObject(bucketName, objectName, fileInputStream);
        System.out.println(bucketName);
        // 关闭OSSClient。
        ossClient.shutdown();
        System.out.println("上传完成");
    }

}
