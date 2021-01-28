package com.touch.air.mall.auth.feign;

import com.touch.air.common.utils.R;
import com.touch.air.mall.auth.vo.SocialUser;
import com.touch.air.mall.auth.vo.UserLoginVo;
import com.touch.air.mall.auth.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author: bin.wang
 * @date: 2021/1/22 18:13
 */
@FeignClient("touch-air-mall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/register")
    R register(@RequestBody UserRegisterVo userRegisterVo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo userLoginVo);

    @PostMapping("/member/member/oauth/weibo/login")
    R oauthWeiboLogin(@RequestBody SocialUser socialUser);
}
