package com.touch.air.mall.order.constant;

/**
 * @author: bin.wang
 * @date: 2021/2/23 13:48
 */
public enum OrderStatusEnum {
    CREATE_NEW(0, "待付款"),
    PAYED(1, "已付款"),
    SENDED(2, "已发货"),
    RECEIVED(3, "已完成"),
    CANCLED(4, "已取消"),
    SERVICING(5, "售后中"),
    SERVICED(6, "售后完成");

    OrderStatusEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;

    }

    private int code;
    private String msg;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
