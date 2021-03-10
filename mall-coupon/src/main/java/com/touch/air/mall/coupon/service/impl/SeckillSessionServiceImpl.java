package com.touch.air.mall.coupon.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.touch.air.common.utils.PageUtils;
import com.touch.air.common.utils.Query;
import com.touch.air.mall.coupon.dao.SeckillSessionDao;
import com.touch.air.mall.coupon.entity.SeckillSessionEntity;
import com.touch.air.mall.coupon.entity.SeckillSkuRelationEntity;
import com.touch.air.mall.coupon.service.SeckillSessionService;
import com.touch.air.mall.coupon.service.SeckillSkuRelationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Resource
    private SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLatest3DaySession() {
        //计算最近三天
        LocalDate now = LocalDate.now();
        LocalDate plus = now.plusDays(2);
        LocalDateTime startTime = LocalDateTime.of(now, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(plus, LocalTime.MAX);
        String startDate = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endDate = endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info("开始时间：" + startDate + ",结束时间：" + endDate);
        List<SeckillSessionEntity> seckillSessionEntities = this.list(new QueryWrapper<SeckillSessionEntity>().between("start_time", startDate, endDate));
        if (CollUtil.isNotEmpty(seckillSessionEntities)) {
            seckillSessionEntities = seckillSessionEntities.stream().map(seckillSessionEntity -> {
                Long sessionEntityId = seckillSessionEntity.getId();
                List<SeckillSkuRelationEntity> relationEntities = seckillSkuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", sessionEntityId));
                seckillSessionEntity.setRelationEntities(relationEntities);
                return seckillSessionEntity;
            }).collect(Collectors.toList());
        }
        return seckillSessionEntities;
    }

}