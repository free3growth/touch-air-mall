package com.touch.air.mall.member.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.touch.air.common.utils.HttpUtils;
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
import com.touch.air.mall.member.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
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

        memberEntity.setNickname(memberRegisterVo.getUsername());

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

    @Override
    public MemberEntity oauthWeiboLogin(SocialUser socialUser) {
        //登录和注册合并逻辑
        String socialUserUid = socialUser.getUid();
        //1、判断当前社交用户是否已经登陆过系统
        MemberEntity selectOne = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", socialUserUid));
        if (ObjectUtil.isNotNull(selectOne)) {
            //用户已经注册过
            //更新令牌和令牌过期时间
            selectOne.setAccessToken(socialUser.getAccess_token());
            selectOne.setExpiresIn(socialUser.getExpires_in());
            baseMapper.updateById(selectOne);
            return selectOne;
        } else {
            //未注册
            MemberEntity memberEntity = new MemberEntity();
            //2、获取社交用户的基本信息
            Map<String, String> query = new HashMap<>();
            query.put("access_token", socialUser.getAccess_token());
            query.put("uid", socialUser.getUid());
            try {
                HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<String, String>(), query);
                if (response.getStatusLine().getStatusCode() == 200) {
                    //获取社交信息成功
                    //即使因为网络原因 不成功，也不应该影响本次登录
                    String resJson= EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = JSON.parseObject(resJson);
                    //昵称
                    String name = jsonObject.getString("name");
                    String gender = jsonObject.getString("gender");
                    memberEntity.setNickname(name);
                    memberEntity.setGender("m".equals(gender) ? 1 : 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            memberEntity.setSocialUid(socialUser.getUid());
            memberEntity.setAccessToken(socialUser.getAccess_token());
            memberEntity.setExpiresIn(socialUser.getExpires_in());
            baseMapper.insert(memberEntity);
            return memberEntity;
        }
    }
}