package com.touch.air.mall.member.exception;

/**
 * @author: bin.wang
 * @date: 2021/1/22 17:30
 */
public class UsernameExistException extends RuntimeException {

    public UsernameExistException() {
        super("用户名已存在");
    }
}
