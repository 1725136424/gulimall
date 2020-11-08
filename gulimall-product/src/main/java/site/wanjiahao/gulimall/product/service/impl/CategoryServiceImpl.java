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
        // 1. 查询所有分类数据
        List<CategoryEntity> entities = baseMapper.selectList(null);
        // 2. 处理数据
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
        // 查询品牌下的所有分类
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
        // 查询所有分类数据
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        // 获取二级菜单数据
        // 1.获取所有的二级菜单
        QueryWrapper<CategoryEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("cat_level", Constant.CategoryLevel.TWO.getLevel());
        List<CategoryEntity> categoryLevel2 = baseMapper.selectList(wrapper);
        List<IndexCategoryLevel2RespVo> collect = categoryLevel2.stream().map(item -> {
            // 拷贝数据 --> 二级分类
            IndexCategoryLevel2RespVo indexCategoryLevel2RespVo = new IndexCategoryLevel2RespVo();
            indexCategoryLevel2RespVo.setCatalog1Id(item.getParentCid());
            indexCategoryLevel2RespVo.setId(item.getCatId());
            indexCategoryLevel2RespVo.setName(item.getName());
            // 查询三级分类数据
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
        // 转换为map数据
        HashMap<String, List<IndexCategoryLevel2RespVo>> map = new HashMap<>();
        collect.forEach(item -> {
            List<IndexCategoryLevel2RespVo> indexCategoryLevel2RespVos = map.get(item.getCatalog1Id() + "");
            if (indexCategoryLevel2RespVos == null) {
                indexCategoryLevel2RespVos = new ArrayList<IndexCategoryLevel2RespVo>();
            }
            indexCategoryLevel2RespVos.add(item);
            map.put(item.getCatalog1Id() + "", indexCategoryLevel2RespVos);
        });
        // 保存缓存数据库中
        String resJson = JSON.toJSONString(map);
        stringRedisTemplate.opsForValue().set("catelogJson", resJson);
        return map;
    }

    // redisson实现分布式锁
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
            System.out.println("查询数据了");
            // 获取二级菜单数据
            // 1.获取所有的二级菜单
            QueryWrapper<CategoryEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("cat_level", Constant.CategoryLevel.TWO.getLevel());
            List<CategoryEntity> categoryLevel2 = baseMapper.selectList(wrapper);
            List<IndexCategoryLevel2RespVo> collect = categoryLevel2.stream().map(item -> {
                // 拷贝数据 --> 二级分类
                IndexCategoryLevel2RespVo indexCategoryLevel2RespVo = new IndexCategoryLevel2RespVo();
                indexCategoryLevel2RespVo.setCatalog1Id(item.getParentCid());
                indexCategoryLevel2RespVo.setId(item.getCatId());
                indexCategoryLevel2RespVo.setName(item.getName());
                // 查询三级分类数据
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
            // 转换为map数据
            HashMap<String, List<IndexCategoryLevel2RespVo>> map = new HashMap<>();
            collect.forEach(item -> {
                List<IndexCategoryLevel2RespVo> indexCategoryLevel2RespVos = map.get(item.getCatalog1Id() + "");
                if (indexCategoryLevel2RespVos == null) {
                    indexCategoryLevel2RespVos = new ArrayList<IndexCategoryLevel2RespVo>();
                }
                indexCategoryLevel2RespVos.add(item);
                map.put(item.getCatalog1Id() + "", indexCategoryLevel2RespVos);
            });
            // 保存缓存数据库中
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

    // 模拟分布式锁获取
    private Map<String, List<IndexCategoryLevel2RespVo>> getCatelogJsonFormDBWitRedisLock() {
        // 加锁保证原子性 ->> 并且设置锁的过期时间，这样就可以避免死锁 ->> 并且指定加锁的UUID 这样锁过期就不会误删除
        String token = UUID.randomUUID().toString();
        // 当前锁30s后过期
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", token, 30, TimeUnit.SECONDS);
        if (lock) {
            Map<String, List<IndexCategoryLevel2RespVo>> stringListMap = null;
            try {
                // 不存在 查询数据库
                stringListMap = getStringListMap();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 删除锁保证原子性 --> 可以使用lua脚本来编写
                String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
                        "then\n" +
                        "    return redis.call(\"del\",KEYS[1])\n" +
                        "else\n" +
                        "    return 0\n" +
                        "end";
                Long lock1 = stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Collections.singletonList("lock"), token);
            }
            // 刪除成功放回1 失败0
            return stringListMap;
        } else {
            // 存在 --> 重新调用该方法
            return getCatelogJsonFormDBWitRedisLock();
        }

    }


    private Map<String, List<IndexCategoryLevel2RespVo>> getStringListMap() {
        // 判断当前缓存中是否存在数据
        String catelogJson = stringRedisTemplate.opsForValue().get("catelogJson");
        if (!StringUtils.isBlank(catelogJson)) {
            return JSON.parseObject(catelogJson, new TypeReference<Map<String, List<IndexCategoryLevel2RespVo>>>() {
            });
        }
        // 否则才查询数据库
        /**
         * 尽量减少和数据库的交互，对数据库的交互最好不要放在循环中，可以查出所有，在来过滤出想要的数据
         */
        // 查询所有分类数据
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        System.out.println("查询数据库了");
        // 获取二级菜单数据
        // 1.获取所有的二级菜单
        QueryWrapper<CategoryEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("cat_level", Constant.CategoryLevel.TWO.getLevel());
        List<CategoryEntity> categoryLevel2 = baseMapper.selectList(wrapper);
        List<IndexCategoryLevel2RespVo> collect = categoryLevel2.stream().map(item -> {
            // 拷贝数据 --> 二级分类
            IndexCategoryLevel2RespVo indexCategoryLevel2RespVo = new IndexCategoryLevel2RespVo();
            indexCategoryLevel2RespVo.setCatalog1Id(item.getParentCid());
            indexCategoryLevel2RespVo.setId(item.getCatId());
            indexCategoryLevel2RespVo.setName(item.getName());
            // 查询三级分类数据
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
        // 转换为map数据
        HashMap<String, List<IndexCategoryLevel2RespVo>> map = new HashMap<>();
        collect.forEach(item -> {
            List<IndexCategoryLevel2RespVo> indexCategoryLevel2RespVos = map.get(item.getCatalog1Id() + "");
            if (indexCategoryLevel2RespVos == null) {
                indexCategoryLevel2RespVos = new ArrayList<IndexCategoryLevel2RespVo>();
            }
            indexCategoryLevel2RespVos.add(item);
            map.put(item.getCatalog1Id() + "", indexCategoryLevel2RespVos);
        });
        // 保存缓存数据库中
        String resJson = JSON.toJSONString(map);
        stringRedisTemplate.opsForValue().set("catelogJson", resJson);
        return map;
    }

    // 本地锁获取
    private Map<String, List<IndexCategoryLevel2RespVo>> getCatelogJsonFormDBWithLocalLock() {
        // 加锁 防止缓存击穿问题，大面积请求同时访问数据库，导致数据库压力过大
        synchronized (this) {
            System.out.println("预备查询数据库");
            // 判断当前缓存中是否存在数据
            String catelogJson = stringRedisTemplate.opsForValue().get("catelogJson");
            if (!StringUtils.isBlank(catelogJson)) {
                return JSON.parseObject(catelogJson, new TypeReference<Map<String, List<IndexCategoryLevel2RespVo>>>() {
                });
            }
            // 否则才查询数据库
            /**
             * 尽量减少和数据库的交互，对数据库的交互最好不要放在循环中，可以查出所有，在来过滤出想要的数据
             */
            // 查询所有分类数据
            List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
            System.out.println("查询数据了");
            // 获取二级菜单数据
            // 1.获取所有的二级菜单
            QueryWrapper<CategoryEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("cat_level", Constant.CategoryLevel.TWO.getLevel());
            List<CategoryEntity> categoryLevel2 = baseMapper.selectList(wrapper);
            List<IndexCategoryLevel2RespVo> collect = categoryLevel2.stream().map(item -> {
                // 拷贝数据 --> 二级分类
                IndexCategoryLevel2RespVo indexCategoryLevel2RespVo = new IndexCategoryLevel2RespVo();
                indexCategoryLevel2RespVo.setCatalog1Id(item.getParentCid());
                indexCategoryLevel2RespVo.setId(item.getCatId());
                indexCategoryLevel2RespVo.setName(item.getName());
                // 查询三级分类数据
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
            // 转换为map数据
            HashMap<String, List<IndexCategoryLevel2RespVo>> map = new HashMap<>();
            collect.forEach(item -> {
                List<IndexCategoryLevel2RespVo> indexCategoryLevel2RespVos = map.get(item.getCatalog1Id() + "");
                if (indexCategoryLevel2RespVos == null) {
                    indexCategoryLevel2RespVos = new ArrayList<IndexCategoryLevel2RespVo>();
                }
                indexCategoryLevel2RespVos.add(item);
                map.put(item.getCatalog1Id() + "", indexCategoryLevel2RespVos);
            });
            // 保存缓存数据库中
            String resJson = JSON.toJSONString(map);
            stringRedisTemplate.opsForValue().set("catelogJson", resJson);
            return map;
        }
    }


    private List<CategoryEntity> getChildren(CategoryEntity currentItem, List<CategoryEntity> allItem) {
        // 设置当前分类的子分类
        return allItem.stream()
                .filter((item) -> item.getParentCid().equals(currentItem.getCatId()))
                .peek((item) -> item.setChildren(getChildren(item, allItem)))
                .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
                .collect(Collectors.toList());
    }
}