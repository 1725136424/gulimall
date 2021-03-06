package site.wanjiahao.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import site.wanjiahao.common.utils.Constant;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.Query;
import site.wanjiahao.gulimall.product.dao.CategoryDao;
import site.wanjiahao.gulimall.product.entity.CategoryEntity;
import site.wanjiahao.gulimall.product.service.CategoryService;
import site.wanjiahao.gulimall.product.vo.CategoryLevel3Vo;
import site.wanjiahao.gulimall.product.vo.IndexCategoryLevel2RespVo;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationServiceImpl categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redisson;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );
        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 1. ????????????????????????
        List<CategoryEntity> entities = baseMapper.selectList(null);
        // 2. ????????????
        return entities.stream()
                .filter((item) -> item.getParentCid() == 0)
                .peek((item) -> item.setChildren(getChildren(item, entities)))
                .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
                .collect(Collectors.toList());
    }

    @Cacheable(value = "category", key = "'category-level1'")
    @Override
    public List<CategoryEntity> listCategoryByPcid(Long pcid) {
        QueryWrapper<CategoryEntity> query = new QueryWrapper<>();
        query.eq("parent_cid", pcid);
        return baseMapper.selectList(query);
    }

    @Caching(evict = {
            @CacheEvict(value = "category", key = "'category-level1'"),
            @CacheEvict(value = "category", key = "'category-level2'"),
    })
    @Override
    public void updateBatch(List<CategoryEntity> categoryEntities) {
        updateBatchById(categoryEntities);
        for (CategoryEntity categoryEntity : categoryEntities) {
            if (categoryEntity.getChildren() != null) {
                updateBatch(categoryEntity.getChildren());
            }
        }
    }

    @Override
    public List<Long> listCategoryPath(Long catelogId) {
        List<Long> longs = new ArrayList<>();
        collectCIds(catelogId, longs);
        Collections.reverse(longs);
        return longs;
    }

    @Override
    public CategoryEntity listById(Long catelogId) {
        return baseMapper.selectById(catelogId);
    }

    private void collectCIds(Long catelogId, List<Long> longs) {
        CategoryEntity categoryEntity = baseMapper.selectById(catelogId);
        if (categoryEntity != null) {
            longs.add(catelogId);
            collectCIds(categoryEntity.getParentCid(), longs);
        }
    }

    @Override
    public PageUtils listWithPageByBranId(Map<String, Object> params, Long brandId) {
        // ??????????????????????????????
        List<Long> catIds = categoryBrandRelationService.listCatIdsByBrandId(brandId);
        QueryWrapper<CategoryEntity> wrapper = new QueryWrapper<>();
        if (catIds != null && catIds.size() > 0) {
            wrapper.in("cat_id", catIds);
            IPage<CategoryEntity> page = this.page(new Query<CategoryEntity>().getPage(params), wrapper);
            return new PageUtils(page);
        } else {
            return null;
        }
    }

    @Cacheable(value = "category", key = "'category-level2'")
    @Override
    public Map<String, List<IndexCategoryLevel2RespVo>> listCateLevel2() {
        // ????????????????????????
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        // ????????????????????????
        // 1.???????????????????????????
        QueryWrapper<CategoryEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("cat_level", Constant.CategoryLevel.TWO.getLevel());
        List<CategoryEntity> categoryLevel2 = baseMapper.selectList(wrapper);
        List<IndexCategoryLevel2RespVo> collect = categoryLevel2.stream().map(item -> {
            // ???????????? --> ????????????
            IndexCategoryLevel2RespVo indexCategoryLevel2RespVo = new IndexCategoryLevel2RespVo();
            indexCategoryLevel2RespVo.setCatalog1Id(item.getParentCid());
            indexCategoryLevel2RespVo.setId(item.getCatId());
            indexCategoryLevel2RespVo.setName(item.getName());
            // ????????????????????????
            Long id = indexCategoryLevel2RespVo.getId();
            List<CategoryEntity> categoryLevel3 = categoryEntities.stream().filter(item1 -> item1.getParentCid().equals(id)).collect(Collectors.toList());
            List<CategoryLevel3Vo> resLevel3 = categoryLevel3.stream().map(level3 -> {
                CategoryLevel3Vo categoryLevel3Vo = new CategoryLevel3Vo();
                categoryLevel3Vo.setCatalog2Id(level3.getParentCid());
                categoryLevel3Vo.setId(level3.getCatId());
                categoryLevel3Vo.setName(level3.getName());
                return categoryLevel3Vo;
            }).collect(Collectors.toList());
            indexCategoryLevel2RespVo.setCatalog3List(resLevel3);
            return indexCategoryLevel2RespVo;
        }).collect(Collectors.toList());
        // ?????????map??????
        HashMap<String, List<IndexCategoryLevel2RespVo>> map = new HashMap<>();
        collect.forEach(item -> {
            List<IndexCategoryLevel2RespVo> indexCategoryLevel2RespVos = map.get(item.getCatalog1Id() + "");
            if (indexCategoryLevel2RespVos == null) {
                indexCategoryLevel2RespVos = new ArrayList<IndexCategoryLevel2RespVo>();
            }
            indexCategoryLevel2RespVos.add(item);
            map.put(item.getCatalog1Id() + "", indexCategoryLevel2RespVos);
        });
        // ????????????????????????
        String resJson = JSON.toJSONString(map);
        stringRedisTemplate.opsForValue().set("catelogJson", resJson);
        return map;
    }

    // redisson??????????????????
    private Map<String, List<IndexCategoryLevel2RespVo>> getCatelogJsonFormDBWitRedissonLock() {
        RLock lock = redisson.getLock("category-lock");
        lock.lock();
        try {
            String catelogJson = stringRedisTemplate.opsForValue().get("catelogJson");
            if (!StringUtils.isBlank(catelogJson)) {
                return JSON.parseObject(catelogJson, new TypeReference<Map<String, List<IndexCategoryLevel2RespVo>>>() {
                });
            }
            List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
            System.out.println("???????????????");
            // ????????????????????????
            // 1.???????????????????????????
            QueryWrapper<CategoryEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("cat_level", Constant.CategoryLevel.TWO.getLevel());
            List<CategoryEntity> categoryLevel2 = baseMapper.selectList(wrapper);
            List<IndexCategoryLevel2RespVo> collect = categoryLevel2.stream().map(item -> {
                // ???????????? --> ????????????
                IndexCategoryLevel2RespVo indexCategoryLevel2RespVo = new IndexCategoryLevel2RespVo();
                indexCategoryLevel2RespVo.setCatalog1Id(item.getParentCid());
                indexCategoryLevel2RespVo.setId(item.getCatId());
                indexCategoryLevel2RespVo.setName(item.getName());
                // ????????????????????????
                Long id = indexCategoryLevel2RespVo.getId();
                List<CategoryEntity> categoryLevel3 = categoryEntities.stream().filter(item1 -> item1.getParentCid().equals(id)).collect(Collectors.toList());
                List<CategoryLevel3Vo> resLevel3 = categoryLevel3.stream().map(level3 -> {
                    CategoryLevel3Vo categoryLevel3Vo = new CategoryLevel3Vo();
                    categoryLevel3Vo.setCatalog2Id(level3.getParentCid());
                    categoryLevel3Vo.setId(level3.getCatId());
                    categoryLevel3Vo.setName(level3.getName());
                    return categoryLevel3Vo;
                }).collect(Collectors.toList());
                indexCategoryLevel2RespVo.setCatalog3List(resLevel3);
                return indexCategoryLevel2RespVo;
            }).collect(Collectors.toList());
            // ?????????map??????
            HashMap<String, List<IndexCategoryLevel2RespVo>> map = new HashMap<>();
            collect.forEach(item -> {
                List<IndexCategoryLevel2RespVo> indexCategoryLevel2RespVos = map.get(item.getCatalog1Id() + "");
                if (indexCategoryLevel2RespVos == null) {
                    indexCategoryLevel2RespVos = new ArrayList<IndexCategoryLevel2RespVo>();
                }
                indexCategoryLevel2RespVos.add(item);
                map.put(item.getCatalog1Id() + "", indexCategoryLevel2RespVos);
            });
            // ????????????????????????
            String resJson = JSON.toJSONString(map);
            stringRedisTemplate.opsForValue().set("catelogJson", resJson);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return null;
    }

    // ????????????????????????
    private Map<String, List<IndexCategoryLevel2RespVo>> getCatelogJsonFormDBWitRedisLock() {
        // ????????????????????? ->> ???????????????????????????????????????????????????????????? ->> ?????????????????????UUID ?????????????????????????????????
        String token = UUID.randomUUID().toString();
        // ?????????30s?????????
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", token, 30, TimeUnit.SECONDS);
        if (lock) {
            Map<String, List<IndexCategoryLevel2RespVo>> stringListMap = null;
            try {
                // ????????? ???????????????
                stringListMap = getStringListMap();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // ???????????????????????? --> ????????????lua???????????????
                String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
                        "then\n" +
                        "    return redis.call(\"del\",KEYS[1])\n" +
                        "else\n" +
                        "    return 0\n" +
                        "end";
                Long lock1 = stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Collections.singletonList("lock"), token);
            }
            // ??????????????????1 ??????0
            return stringListMap;
        } else {
            // ?????? --> ?????????????????????
            return getCatelogJsonFormDBWitRedisLock();
        }

    }


    private Map<String, List<IndexCategoryLevel2RespVo>> getStringListMap() {
        // ???????????????????????????????????????
        String catelogJson = stringRedisTemplate.opsForValue().get("catelogJson");
        if (!StringUtils.isBlank(catelogJson)) {
            return JSON.parseObject(catelogJson, new TypeReference<Map<String, List<IndexCategoryLevel2RespVo>>>() {
            });
        }
        // ????????????????????????
        /**
         * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
         */
        // ????????????????????????
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        System.out.println("??????????????????");
        // ????????????????????????
        // 1.???????????????????????????
        QueryWrapper<CategoryEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("cat_level", Constant.CategoryLevel.TWO.getLevel());
        List<CategoryEntity> categoryLevel2 = baseMapper.selectList(wrapper);
        List<IndexCategoryLevel2RespVo> collect = categoryLevel2.stream().map(item -> {
            // ???????????? --> ????????????
            IndexCategoryLevel2RespVo indexCategoryLevel2RespVo = new IndexCategoryLevel2RespVo();
            indexCategoryLevel2RespVo.setCatalog1Id(item.getParentCid());
            indexCategoryLevel2RespVo.setId(item.getCatId());
            indexCategoryLevel2RespVo.setName(item.getName());
            // ????????????????????????
            Long id = indexCategoryLevel2RespVo.getId();
            List<CategoryEntity> categoryLevel3 = categoryEntities.stream().filter(item1 -> item1.getParentCid().equals(id)).collect(Collectors.toList());
            List<CategoryLevel3Vo> resLevel3 = categoryLevel3.stream().map(level3 -> {
                CategoryLevel3Vo categoryLevel3Vo = new CategoryLevel3Vo();
                categoryLevel3Vo.setCatalog2Id(level3.getParentCid());
                categoryLevel3Vo.setId(level3.getCatId());
                categoryLevel3Vo.setName(level3.getName());
                return categoryLevel3Vo;
            }).collect(Collectors.toList());
            indexCategoryLevel2RespVo.setCatalog3List(resLevel3);
            return indexCategoryLevel2RespVo;
        }).collect(Collectors.toList());
        // ?????????map??????
        HashMap<String, List<IndexCategoryLevel2RespVo>> map = new HashMap<>();
        collect.forEach(item -> {
            List<IndexCategoryLevel2RespVo> indexCategoryLevel2RespVos = map.get(item.getCatalog1Id() + "");
            if (indexCategoryLevel2RespVos == null) {
                indexCategoryLevel2RespVos = new ArrayList<IndexCategoryLevel2RespVo>();
            }
            indexCategoryLevel2RespVos.add(item);
            map.put(item.getCatalog1Id() + "", indexCategoryLevel2RespVos);
        });
        // ????????????????????????
        String resJson = JSON.toJSONString(map);
        stringRedisTemplate.opsForValue().set("catelogJson", resJson);
        return map;
    }

    // ???????????????
    private Map<String, List<IndexCategoryLevel2RespVo>> getCatelogJsonFormDBWithLocalLock() {
        // ?????? ?????????????????????????????????????????????????????????????????????????????????????????????
        synchronized (this) {
            System.out.println("?????????????????????");
            // ???????????????????????????????????????
            String catelogJson = stringRedisTemplate.opsForValue().get("catelogJson");
            if (!StringUtils.isBlank(catelogJson)) {
                return JSON.parseObject(catelogJson, new TypeReference<Map<String, List<IndexCategoryLevel2RespVo>>>() {
                });
            }
            // ????????????????????????
            /**
             * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
             */
            // ????????????????????????
            List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
            System.out.println("???????????????");
            // ????????????????????????
            // 1.???????????????????????????
            QueryWrapper<CategoryEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("cat_level", Constant.CategoryLevel.TWO.getLevel());
            List<CategoryEntity> categoryLevel2 = baseMapper.selectList(wrapper);
            List<IndexCategoryLevel2RespVo> collect = categoryLevel2.stream().map(item -> {
                // ???????????? --> ????????????
                IndexCategoryLevel2RespVo indexCategoryLevel2RespVo = new IndexCategoryLevel2RespVo();
                indexCategoryLevel2RespVo.setCatalog1Id(item.getParentCid());
                indexCategoryLevel2RespVo.setId(item.getCatId());
                indexCategoryLevel2RespVo.setName(item.getName());
                // ????????????????????????
                Long id = indexCategoryLevel2RespVo.getId();
                List<CategoryEntity> categoryLevel3 = categoryEntities.stream().filter(item1 -> item1.getParentCid().equals(id)).collect(Collectors.toList());
                List<CategoryLevel3Vo> resLevel3 = categoryLevel3.stream().map(level3 -> {
                    CategoryLevel3Vo categoryLevel3Vo = new CategoryLevel3Vo();
                    categoryLevel3Vo.setCatalog2Id(level3.getParentCid());
                    categoryLevel3Vo.setId(level3.getCatId());
                    categoryLevel3Vo.setName(level3.getName());
                    return categoryLevel3Vo;
                }).collect(Collectors.toList());
                indexCategoryLevel2RespVo.setCatalog3List(resLevel3);
                return indexCategoryLevel2RespVo;
            }).collect(Collectors.toList());
            // ?????????map??????
            HashMap<String, List<IndexCategoryLevel2RespVo>> map = new HashMap<>();
            collect.forEach(item -> {
                List<IndexCategoryLevel2RespVo> indexCategoryLevel2RespVos = map.get(item.getCatalog1Id() + "");
                if (indexCategoryLevel2RespVos == null) {
                    indexCategoryLevel2RespVos = new ArrayList<IndexCategoryLevel2RespVo>();
                }
                indexCategoryLevel2RespVos.add(item);
                map.put(item.getCatalog1Id() + "", indexCategoryLevel2RespVos);
            });
            // ????????????????????????
            String resJson = JSON.toJSONString(map);
            stringRedisTemplate.opsForValue().set("catelogJson", resJson);
            return map;
        }
    }


    private List<CategoryEntity> getChildren(CategoryEntity currentItem, List<CategoryEntity> allItem) {
        // ??????????????????????????????
        return allItem.stream()
                .filter((item) -> item.getParentCid().equals(currentItem.getCatId()))
                .peek((item) -> item.setChildren(getChildren(item, allItem)))
                .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
                .collect(Collectors.toList());
    }
}