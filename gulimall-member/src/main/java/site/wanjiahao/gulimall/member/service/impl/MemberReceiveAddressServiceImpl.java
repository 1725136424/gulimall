package site.wanjiahao.gulimall.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.Query;
import site.wanjiahao.gulimall.member.dao.MemberReceiveAddressDao;
import site.wanjiahao.gulimall.member.entity.MemberReceiveAddressEntity;
import site.wanjiahao.gulimall.member.service.MemberReceiveAddressService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("memberReceiveAddressService")
public class MemberReceiveAddressServiceImpl extends ServiceImpl<MemberReceiveAddressDao, MemberReceiveAddressEntity> implements MemberReceiveAddressService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberReceiveAddressEntity> page = this.page(
                new Query<MemberReceiveAddressEntity>().getPage(params),
                new QueryWrapper<MemberReceiveAddressEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<MemberReceiveAddressEntity> findAllAddress(Long memberId) {
        return baseMapper.selectList(new QueryWrapper<MemberReceiveAddressEntity>().eq("member_id", memberId));
    }

    @Override
    public BigDecimal getPostage(Long addressId) {
        MemberReceiveAddressEntity memberReceiveAddressEntity = baseMapper.selectById(addressId);
        String phone = memberReceiveAddressEntity.getPhone();
        return new BigDecimal(phone.substring(phone.length() - 1));
    }

    @Override
    public MemberReceiveAddressEntity findDefaultAddress(Long memberId) {
        List<MemberReceiveAddressEntity> defaultAddress = findAllAddress(memberId)
                .stream()
                .filter(item -> item.getDefaultStatus() == 1)
                .collect(Collectors.toList());
        if (defaultAddress.size() > 0) {
            return defaultAddress.get(0);
        } else {
            return null;
        }
    }

}