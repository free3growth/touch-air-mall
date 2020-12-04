package com.touch.air.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.touch.air.common.utils.PageUtils;
import com.touch.air.mall.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author bin.wang
 * @email 1178321785@qq.com
 * @date 2020-12-04 14:18:41
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

