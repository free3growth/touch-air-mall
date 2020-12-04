package com.touch.air.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.touch.air.common.utils.PageUtils;
import com.touch.air.mall.member.entity.MemberLevelEntity;

import java.util.Map;

/**
 * 会员等级
 *
 * @author bin.wang
 * @email 1178321785@qq.com
 * @date 2020-12-04 14:18:41
 */
public interface MemberLevelService extends IService<MemberLevelEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

