package com.touch.air.mall.search.service;

import com.touch.air.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @author: bin.wang
 * @date: 2021/1/6 14:21
 */
public interface ProductSaveService {
    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
