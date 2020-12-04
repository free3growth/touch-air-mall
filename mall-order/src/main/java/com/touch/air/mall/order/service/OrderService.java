package com.touch.air.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.touch.air.common.utils.PageUtils;
import com.touch.air.mall.order.entity.OrderEntity;

import java.util.Map;

/**
 * 订单
 *
 * @author bin.wang
 * @email 1178321785@qq.com
 * @date 2020-12-04 14:28:33
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

