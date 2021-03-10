package com.touch.air.mall.coupon;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

//@SpringBootTest
@Slf4j
class MallCouponApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    public void test() {
        LocalDate now = LocalDate.now();
        LocalDate plus = now.plusDays(2);
        LocalDateTime startTime = LocalDateTime.of(now, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(plus, LocalTime.MAX);
        String startDate = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endDate = endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info("开始时间：" + startDate + ",结束时间：" + endDate);
    }

}
