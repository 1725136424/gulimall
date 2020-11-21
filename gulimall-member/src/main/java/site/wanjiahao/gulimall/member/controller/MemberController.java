package site.wanjiahao.gulimall.member.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.wanjiahao.common.code.BizCodeEnum;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.gulimall.member.entity.MemberEntity;
import site.wanjiahao.gulimall.member.exception.PasswordErrorException;
import site.wanjiahao.gulimall.member.exception.UsernameExistException;
import site.wanjiahao.gulimall.member.exception.UsernameUnknownException;
import site.wanjiahao.gulimall.member.feign.CouponFeignService;
import site.wanjiahao.gulimall.member.service.MemberService;
import site.wanjiahao.gulimall.member.vo.Auth2WeiboResponseVo;
import site.wanjiahao.gulimall.member.vo.RegisterUserVo;

import java.util.Arrays;
import java.util.Map;

// import org.apache.shiro.authz.annotation.RequiresPermissions;


/**
 * 会员
 *
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 15:58:22
 */
@RestController
@RequestMapping("member/member")
@Slf4j
public class MemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private CouponFeignService couponFeignService;

    @RequestMapping("/coupons")
    public R coupons() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("wjh");
        R test = couponFeignService.test();
        return R.ok().put("member", memberEntity).put("coupons", test.get("coupons"));
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public R register(@RequestBody RegisterUserVo registerUserVo) {
        // 查询当前用户名是否存在
        try {
            memberService.register(registerUserVo);
        } catch (UsernameExistException e) {
            log.error(e.toString());
            return R.error(BizCodeEnum.USERNAME_EXIST_EXCEPTION.getBizCode(),
                    BizCodeEnum.USERNAME_EXIST_EXCEPTION.getMessage());
        }
        return R.ok("保存成功");
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public R login(@RequestBody RegisterUserVo registerUserVo) {
        try {
            memberService.login(registerUserVo);
            return R.ok();
        } catch (UsernameUnknownException e) {
            return R.error(BizCodeEnum.USERNAME_UNKNOWN_EXCEPTION.getBizCode(),
                    BizCodeEnum.USERNAME_UNKNOWN_EXCEPTION.getMessage());
        } catch (PasswordErrorException e) {
            return R.error(BizCodeEnum.PASSWORD_ERROR_EXCEPTION.getBizCode(),
                    BizCodeEnum.PASSWORD_ERROR_EXCEPTION.getMessage());
        } catch (Exception e) {
            return R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getBizCode(),
                    BizCodeEnum.UNKNOWN_EXCEPTION.getMessage());
        }
    }

    /**
     * auth2登录
     */
    @PostMapping("/auth2Login")
    public R auth2Login(@RequestBody Auth2WeiboResponseVo auth2WeiboResponseVo) {
        try {
            MemberEntity memberEntity = memberService.auth2Login(auth2WeiboResponseVo);
            return R.ok().put("user", memberEntity);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return R.error();
        }
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
