package site.wanjiahao.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.gulimall.member.entity.MemberLevelEntity;

import java.util.List;
import java.util.Map;

/**
 * 会员等级
 *
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 15:58:22
 */
public interface MemberLevelService extends IService<MemberLevelEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<MemberLevelEntity> listPriviledge();
}

