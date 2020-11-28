package site.wanjiahao.gulimall.member.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.gulimall.member.entity.MemberReceiveAddressEntity;
import site.wanjiahao.gulimall.member.service.MemberReceiveAddressService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

// import org.apache.shiro.authz.annotation.RequiresPermissions;



/**
 * 会员收货地址
 *
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 15:58:22
 */
@RestController
@RequestMapping("member/memberreceiveaddress")
public class MemberReceiveAddressController {
    @Autowired
    private MemberReceiveAddressService memberReceiveAddressService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("member:memberreceiveaddress:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberReceiveAddressService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 获取当前用户的所有收获地址
     */
    @GetMapping("/addresses/{memberId}")
    public List<MemberReceiveAddressEntity> findAllAddress(@PathVariable("memberId") Long memberId) {
       return memberReceiveAddressService.findAllAddress(memberId);
    }

    /**
     * 获取当前用户的默认地址
     */
    @GetMapping("/defaultAddress/{memberId}")
    public MemberReceiveAddressEntity findDefaultAddress(@PathVariable("memberId") Long memberId) {
        return memberReceiveAddressService.findDefaultAddress(memberId);
    }

    /**
     * 获取当前用户的邮费信息
     */
    @GetMapping("/getPostage/{addressId}")
    public R getPostage(@PathVariable("addressId") Long addressId) {
        BigDecimal postage  = memberReceiveAddressService.getPostage(addressId);
        return R.ok().put("postage", postage);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("member:memberreceiveaddress:info")
    public R info(@PathVariable("id") Long id){
		MemberReceiveAddressEntity memberReceiveAddress = memberReceiveAddressService.getById(id);

        return R.ok().put("memberReceiveAddress", memberReceiveAddress);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("member:memberreceiveaddress:save")
    public R save(@RequestBody MemberReceiveAddressEntity memberReceiveAddress){
		memberReceiveAddressService.save(memberReceiveAddress);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("member:memberreceiveaddress:update")
    public R update(@RequestBody MemberReceiveAddressEntity memberReceiveAddress){
		memberReceiveAddressService.updateById(memberReceiveAddress);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("member:memberreceiveaddress:delete")
    public R delete(@RequestBody Long[] ids){
		memberReceiveAddressService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
