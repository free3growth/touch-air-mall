package com.touch.air.common.constant;

/**
 * 商品常量
 * @author: bin.wang
 * @date: 2020/12/28 16:53
 */
public class ProductConstant {
    public enum AttrEnum{
        ATTR_TYPE_BASE(1,"基本属性"),
        ATTR_TYPE_SALE(0,"销售属性");

        AttrEnum(int code, String msg) {
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
}
