package com.touch.air.mall.product;

import com.touch.air.mall.product.entity.BrandEntity;
import com.touch.air.mall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class MallProductApplicationTests {

    @Resource
    private BrandService brandService;

    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setDescript("cpb");
        brandEntity.setName("jifuzhishi");
        brandService.save(brandEntity);
        System.out.println(brandService.list());
    }

}
