package com.touch.air.mall.member.exception;

/**
 * @author: bin.wang
 * @date: 2021/1/22 17:30
 */
public class PhoneExistException extends RuntimeException{

    public PhoneExistException() {
        super("手机号已存在");
    }
}
