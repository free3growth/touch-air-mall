package com.touch.air.mall.order.dao;

import com.touch.air.mall.order.entity.OrderItemEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项信息
 * 
 * @author bin.wang
 * @email 1178321785@qq.com
 * @date 2020-12-04 14:28:33
 */
@Mapper
public interface OrderItemDao extends BaseMapper<OrderItemEntity> {
	
}
