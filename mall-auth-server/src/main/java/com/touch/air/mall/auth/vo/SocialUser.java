package com.touch.air.mall.auth.vo;

import lombok.Data;

/**
 * @author: bin.wang
 * @date: 2021/1/23 15:18
 */
@Data
public class SocialUser {

    private String access_token;
    private Long expires_in;
    private String remind_in;
    private String uid;
    private String isRealName;
}
