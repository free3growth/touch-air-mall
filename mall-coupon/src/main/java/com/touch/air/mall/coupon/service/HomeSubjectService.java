package com.touch.air.mall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.touch.air.common.utils.PageUtils;
import com.touch.air.mall.coupon.entity.HomeSubjectEntity;

import java.util.Map;

/**
 * 首页专题表【jd首页下面很多专题，每个专题链接新的页面，展示专题商品信息】
 *
 * @author bin.wang
 * @email 1178321785@qq.com
 * @date 2020-12-04 13:47:36
 */
public interface HomeSubjectService extends IService<HomeSubjectEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

