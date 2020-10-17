package site.wanjiahao.gulimall.member.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.gulimall.member.entity.MemberLevelEntity;
import site.wanjiahao.gulimall.member.service.MemberLevelService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

// import org.apache.shiro.authz.annotation.RequiresPermissions;



/**
 * 会员等级
 *
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 15:58:22
 */
@RestController
@RequestMapping("member/memberlevel")
public class MemberLevelController {
    @Autowired
    private MemberLevelService memberLevelService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("member:memberlevel:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberLevelService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 获取有会员价格特权的会员
     */
    @RequestMapping("/listPriviledge")
    public R listPriviledge(){
        List<MemberLevelEntity> memberLevelEntities = memberLevelService.listPriviledge();
        return R.ok().put("data", memberLevelEntities);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("member:memberlevel:info")
    public R info(@PathVariable("id") Long id){
		MemberLevelEntity memberLevel = memberLevelService.getById(id);

        return R.ok().put("memberLevel", memberLevel);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("member:memberlevel:save")
    public R save(@RequestBody MemberLevelEntity memberLevel){
		memberLevelService.save(memberLevel);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("member:memberlevel:update")
    public R update(@RequestBody MemberLevelEntity memberLevel){
		memberLevelService.updateById(memberLevel);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("member:memberlevel:delete")
    public R delete(@RequestBody Long[] ids){
		memberLevelService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
