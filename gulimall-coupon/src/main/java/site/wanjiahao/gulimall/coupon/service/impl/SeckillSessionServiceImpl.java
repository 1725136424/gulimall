package site.wanjiahao.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.wanjiahao.common.constant.SeckillSessionStatusEnum;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.Query;
import site.wanjiahao.gulimall.coupon.dao.SeckillSessionDao;
import site.wanjiahao.gulimall.coupon.entity.SeckillSessionEntity;
import site.wanjiahao.gulimall.coupon.entity.SeckillSkuRelationEntity;
import site.wanjiahao.gulimall.coupon.service.SeckillSessionService;
import site.wanjiahao.gulimall.coupon.service.SeckillSkuRelationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    private SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getThreeSession() {
        QueryWrapper<SeckillSessionEntity> wrapper = new QueryWrapper<>();
        // 上架还未上架的商品
        wrapper.between("end_time", getStartTime(), getEndTime())
                .ne("publish_status", SeckillSessionStatusEnum.PUBLISHED.getCode());
        List<SeckillSessionEntity> seckillSessionEntities = list(wrapper);
        return seckillSessionEntities.stream().peek((item) -> {
            Long id = item.getId();
            // 查询当前关联商品信息
            List<SeckillSkuRelationEntity> seckillSkuRelationEntities = seckillSkuRelationService.listBySessionId(id);
            item.setSeckillSkuRelationEntities(seckillSkuRelationEntities);
        }).collect(Collectors.toList());
    }

    @Override
    public void publish(List<Long> sessionIds) {
        sessionIds.forEach(item -> {
            baseMapper.publish(item);
        });
    }

    private LocalDateTime getStartTime() {
        // 当前日期
        LocalDate startTime = LocalDate.now();
        // 当前日期三天之内
        // min
        LocalTime min = LocalTime.MIN;
        // max
        return LocalDateTime.of(startTime, min);
    }

    private LocalDateTime getEndTime() {
        LocalDate startTime = LocalDate.now();
        LocalDate endTime = startTime.plusDays(3);
        // max
        LocalTime max = LocalTime.MAX;
        return LocalDateTime.of(endTime, max);
    }


}