package com.touch.air.mall.member.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.touch.air.common.utils.PageUtils;
import com.touch.air.common.utils.Query;
import com.touch.air.mall.member.dao.MemberDao;
import com.touch.air.mall.member.dao.MemberLevelDao;
import com.touch.air.mall.member.entity.MemberEntity;
import com.touch.air.mall.member.entity.MemberLevelEntity;
import com.touch.air.mall.member.exception.PhoneExistException;
import com.touch.air.mall.member.exception.UsernameExistException;
import com.touch.air.mall.member.service.MemberService;
import com.touch.air.mall.member.vo.MemberLoginVo;
import com.touch.air.mall.member.vo.MemberRegisterVo;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Resource
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(MemberRegisterVo memberRegisterVo) {
        MemberEntity memberEntity = new MemberEntity();
        //检查用户名、手机号是否唯一
        //为了让controller感知异常，使用异常机制
        checkPhoneUnique(memberRegisterVo.getPhone());
        checkUsernameUnique(memberRegisterVo.getUsername());

        memberEntity.setUsername(memberRegisterVo.getUsername());
        memberEntity.setMobile(memberRegisterVo.getPhone());

        //密码加密
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode(memberRegisterVo.getPassword());
        memberEntity.setPassword(encode);

        //设置默认等级
        MemberLevelEntity memberLevelEntity = memberLevelDao.selectOne(new QueryWrapper<MemberLevelEntity>().eq("default_status", 1));
        memberEntity.setLevelId(memberLevelEntity.getId());


        baseMapper.insert(memberEntity);
    }


    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        Integer count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (count > 0) {
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkUsernameUnique(String username) throws UsernameExistException {
        Integer count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if (count > 0) {
            throw new UsernameExistException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo memberRegisterVo) {

        String loginAccount = memberRegisterVo.getLoginAccount();
        MemberEntity memberEntity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginAccount).or().eq("mobile", loginAccount));
        if (ObjectUtil.isNotNull(memberEntity)) {
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            boolean matches = bCryptPasswordEncoder.matches(memberRegisterVo.getPassword(), memberEntity.getPassword());
            if (matches) {
                //匹配成功
                return memberEntity;
            }else{
                return null;
            }
        }else{
            return null;
        }
    }

}