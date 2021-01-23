package com.touch.air.mall.member.controller;

import cn.hutool.core.util.ObjectUtil;
import com.touch.air.common.exception.BizCodeEnum;
import com.touch.air.common.utils.PageUtils;
import com.touch.air.common.utils.R;
import com.touch.air.mall.member.entity.MemberEntity;
import com.touch.air.mall.member.exception.PhoneExistException;
import com.touch.air.mall.member.exception.UsernameExistException;
import com.touch.air.mall.member.feign.CouponsFeignService;
import com.touch.air.mall.member.service.MemberService;
import com.touch.air.mall.member.vo.MemberLoginVo;
import com.touch.air.mall.member.vo.MemberRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;



/**
 * 会员
 *
 * @author bin.wang
 * @email 1178321785@qq.com
 * @date 2020-12-04 14:18:41
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;
    @Autowired
    private CouponsFeignService couponsFeignService;

    @RequestMapping("/coupons")
    public R test(){
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("用户z3");
        R coupons = couponsFeignService.memberCoupons();
        return R.ok().put("member", memberEntity).put("coupons", coupons.get("coupons"));

    }

    @PostMapping("/register")
    public R register(@RequestBody MemberRegisterVo memberRegisterVo) {
        try {
            memberService.register(memberRegisterVo);
        } catch (PhoneExistException e) {
            return R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnum.PHONE_EXIST_EXCEPTION.getMsg());
        } catch (UsernameExistException e) {
            return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(), BizCodeEnum.USER_EXIST_EXCEPTION.getMsg());
        }
        return R.ok();
    }

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo memberRegisterVo) {

        MemberEntity memberEntity = memberService.login(memberRegisterVo);
        if (ObjectUtil.isNotNull(memberEntity)) {
            return R.ok();
        }else{
            return R.error(BizCodeEnum.ACCOUNT_PASSWORD_EXCEPTION.getCode(), BizCodeEnum.ACCOUNT_PASSWORD_EXCEPTION.getMsg());
        }

    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
