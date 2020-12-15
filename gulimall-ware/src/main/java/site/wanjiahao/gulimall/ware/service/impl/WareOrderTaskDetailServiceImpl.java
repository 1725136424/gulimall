package site.wanjiahao.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.Query;
import site.wanjiahao.gulimall.ware.dao.WareOrderTaskDetailDao;
import site.wanjiahao.gulimall.ware.entity.WareOrderTaskDetailEntity;
import site.wanjiahao.gulimall.ware.service.WareOrderTaskDetailService;

import java.util.List;
import java.util.Map;


@Service("wareOrderTaskDetailService")
public class WareOrderTaskDetailServiceImpl extends ServiceImpl<WareOrderTaskDetailDao, WareOrderTaskDetailEntity> implements WareOrderTaskDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareOrderTaskDetailEntity> page = this.page(
                new Query<WareOrderTaskDetailEntity>().getPage(params),
                new QueryWrapper<WareOrderTaskDetailEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public WareOrderTaskDetailEntity listById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public List<WareOrderTaskDetailEntity> listByTaskId(Long id) {
        return baseMapper.selectList(new QueryWrapper<WareOrderTaskDetailEntity>().eq("task_id", id));
    }

}