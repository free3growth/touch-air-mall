package com.touch.air.common.exception;

/**
 * 异常code 枚举
 * @author: bin.wang
 * @date: 2020/12/17 16:59
 *
 * 1. 错误码定义规则为5位数字
 * 2. 前两位表示业务场景，最后三位表示错误码
 *    例如：10001
 *    10：通用
 *      001：系统未知异常
 *      002: 短信验证码频率太高
 * 3. 维护错误码后需要维护错误描述，将他们定义为枚举形式
 *    错误码列表：
 *    10：通用
 *    11：商品
 *    12：订单
 *    13：购物车
 *    14：物流
 *    15: 用户
 */
public enum BizCodeEnum {
    UNKNOWN_EXCEPTION(10000, "系统未知异常"),
    VALID_EXCEPTION(10001, "参数格式校验失败"),
    SMS_CODE_EXCEPTION(10002, "验证码获取频率太高，请稍后再试"),
    USER_EXIST_EXCEPTION(15001, "用户已经存在"),
    PHONE_EXIST_EXCEPTION(15002, "手机号已经存在"),
    ACCOUNT_PASSWORD_EXCEPTION(15003, "账号或密码错误"),
    PRODUCT_UP_EXCEPTION(11000,"商品上架异常");

    private int code;
    private String msg;

    BizCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode(){
        return code;
    }
    public String getMsg(){
        return msg;
    }
}
