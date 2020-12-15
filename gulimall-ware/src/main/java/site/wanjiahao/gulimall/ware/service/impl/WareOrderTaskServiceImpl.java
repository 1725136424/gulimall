package site.wanjiahao.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.Query;
import site.wanjiahao.gulimall.ware.dao.WareOrderTaskDao;
import site.wanjiahao.gulimall.ware.entity.WareOrderTaskEntity;
import site.wanjiahao.gulimall.ware.service.WareOrderTaskService;

import java.util.Map;


@Service("wareOrderTaskService")
public class WareOrderTaskServiceImpl extends ServiceImpl<WareOrderTaskDao, WareOrderTaskEntity> implements WareOrderTaskService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareOrderTaskEntity> page = this.page(
                new Query<WareOrderTaskEntity>().getPage(params),
                new QueryWrapper<WareOrderTaskEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public WareOrderTaskEntity listById(Long taskId) {
        return baseMapper.selectById(taskId);
    }

    @Override
    public WareOrderTaskEntity getByOrderSn(String orderSn) {
        return baseMapper.selectOne(new QueryWrapper<WareOrderTaskEntity>().eq("order_sn", orderSn));
    }

}