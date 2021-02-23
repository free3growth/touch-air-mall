package com.touch.air.mall.ware.exception;

/**
 * @author: bin.wang
 * @date: 2021/2/23 14:57
 */
public class NoStockException extends RuntimeException {
    private Long skuId;

    public NoStockException(Long skuId) {
        super("商品id:"+skuId + ",没有足够的库存了");
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}
