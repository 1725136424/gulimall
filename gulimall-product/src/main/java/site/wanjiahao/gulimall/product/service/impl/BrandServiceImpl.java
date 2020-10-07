package site.wanjiahao.gulimall.product.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import site.wanjiahao.common.utils.Constant;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.Query;

import site.wanjiahao.gulimall.product.dao.BrandDao;
import site.wanjiahao.gulimall.product.entity.BrandEntity;
import site.wanjiahao.gulimall.product.service.BrandService;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<BrandEntity> wrapper = new QueryWrapper<>();
        String keywords = (String) params.get("key");
        if (!StringUtils.isBlank(keywords)) {
            // 不是空白字符串 id 品牌名称 首字母
            wrapper.like("brand_id", keywords)
                    .or()
                    .like("name", keywords)
                    .or()
                    .like("first_letter", keywords);
        }
        IPage<BrandEntity> page = this.page(new Query<BrandEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

}