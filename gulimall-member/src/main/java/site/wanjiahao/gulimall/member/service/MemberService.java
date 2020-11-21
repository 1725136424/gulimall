package site.wanjiahao.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.gulimall.member.entity.MemberEntity;
import site.wanjiahao.gulimall.member.vo.Auth2WeiboResponseVo;
import site.wanjiahao.gulimall.member.vo.RegisterUserVo;

import java.util.Map;

/**
 * 会员
 *
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 15:58:22
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(RegisterUserVo registerUserVo);

    void login(RegisterUserVo registerUserVo);

    MemberEntity auth2Login(Auth2WeiboResponseVo auth2WeiboResponseVo);
}

