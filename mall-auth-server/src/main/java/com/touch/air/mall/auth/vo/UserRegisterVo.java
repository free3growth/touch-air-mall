package com.touch.air.mall.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @author: bin.wang
 * @date: 2021/1/22 15:31
 */
@Data
public class UserRegisterVo {
    @NotEmpty(message = "用户名必须提交")
    @Length(min = 6, max = 18, message = "用户名必须是6-18位")
    private String username;

    @NotEmpty(message = "密码必须填写")
    @Length(min = 6, max = 18, message = "密码必须是6-18位")
    private String password;

    @NotEmpty(message = "手机号必须填写")
    @Pattern(regexp = "^[1]([3-9])[0-9]{9}$", message = "手机号格式不正确")
    private String phone;

    @NotEmpty(message = "验证码必须填写")
    private String code;
}
