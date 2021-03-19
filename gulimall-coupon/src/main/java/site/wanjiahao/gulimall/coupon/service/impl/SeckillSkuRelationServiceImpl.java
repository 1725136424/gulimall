package site.wanjiahao.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.Query;
import site.wanjiahao.gulimall.coupon.dao.SeckillSkuRelationDao;
import site.wanjiahao.gulimall.coupon.entity.SeckillSkuRelationEntity;
import site.wanjiahao.gulimall.coupon.service.SeckillSkuRelationService;

import java.util.List;
import java.util.Map;


@Service("seckillSkuRelationService")
public class SeckillSkuRelationServiceImpl extends ServiceImpl<SeckillSkuRelationDao, SeckillSkuRelationEntity> implements SeckillSkuRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        // 获取场次id，获取关联的场次商品信息
        Long promotionSessionId = Long.parseLong(params.get("promotionSessionId") + "");
        QueryWrapper<SeckillSkuRelationEntity> wrapper = new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", promotionSessionId);
        IPage<SeckillSkuRelationEntity> page = this.page(
                new Query<SeckillSkuRelationEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSkuRelationEntity> listBySessionId(Long id) {
        return list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", id));
    }

}