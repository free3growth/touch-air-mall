package com.touch.air.mall.order.vo;

import com.touch.air.mall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @author: bin.wang
 * @date: 2021/2/22 14:51
 */
@Data
public class SubmitOrderResVo {

    private OrderEntity orderEntity;

    /**
     * 错误状态码
     *  0 成功
     */
    private Integer code;
}
