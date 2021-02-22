package com.touch.air.mall.ware.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.touch.air.common.utils.PageUtils;
import com.touch.air.common.utils.Query;
import com.touch.air.common.utils.R;
import com.touch.air.mall.ware.dao.WareInfoDao;
import com.touch.air.mall.ware.entity.WareInfoEntity;
import com.touch.air.mall.ware.feign.MemberFeignService;
import com.touch.air.mall.ware.service.WareInfoService;
import com.touch.air.mall.ware.vo.FareResVo;
import com.touch.air.mall.ware.vo.MemberAddressVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Resource
    private MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String) params.get("key");
        QueryWrapper<WareInfoEntity> wrapper = new QueryWrapper<>();
        if (StrUtil.isNotEmpty(key)) {
            wrapper.and(queryWrapper -> {
                queryWrapper.eq("id", key).or().like("name", key).or().like("address", key).or().like("areacode", key);
            });
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public FareResVo getFare(Long addrId) {
        FareResVo fareResVo = new FareResVo();
        R info = memberFeignService.info(addrId);
        MemberAddressVo memberAddressVo = info.getData("memberReceiveAddress",new TypeReference<MemberAddressVo>() {
        });
        if (ObjectUtil.isNotNull(memberAddressVo)) {
            fareResVo.setAddress(memberAddressVo);
            //运费计算，第三方物流公司提供的接口，这里就简单计算一下
            String phone = memberAddressVo.getPhone();
            String substring = phone.substring(phone.length() - 2, phone.length());
            fareResVo.setFare(new BigDecimal(substring));
            return fareResVo;
        }
        return fareResVo;
    }
}