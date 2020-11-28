package site.wanjiahao.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.gulimall.member.entity.MemberReceiveAddressEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 会员收货地址
 *
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 15:58:22
 */
public interface MemberReceiveAddressService extends IService<MemberReceiveAddressEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<MemberReceiveAddressEntity> findAllAddress(Long memberId);

    BigDecimal getPostage(Long addressId);

    MemberReceiveAddressEntity findDefaultAddress(Long memberId);
}

