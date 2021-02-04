package com.touch.air.mall.cart.vo;

import lombok.Data;

/**
 * @author: bin.wang
 * @date: 2021/2/2 13:58
 */
@Data
public class UserInfoTo {
    private Long userId;
    private String userKey;
    /**
     * 是否临时用户
     */
    private Boolean tempUser = false;
}
