package com.touch.air.mall.third;

import com.aliyun.oss.OSSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

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


}
