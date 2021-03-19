package site.wanjiahao.gulimall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import site.wanjiahao.common.constant.OrderRabbitConstant;
import site.wanjiahao.common.constant.SeckillConstant;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.common.vo.MemberEntityVo;
import site.wanjiahao.common.vo.SeckillSuccessVo;
import site.wanjiahao.gulimall.seckill.feign.CouponFeignService;
import site.wanjiahao.gulimall.seckill.feign.ProductFeignService;
import site.wanjiahao.gulimall.seckill.interceptor.LoginInterceptor;
import site.wanjiahao.gulimall.seckill.service.SeckillService;
import site.wanjiahao.gulimall.seckill.to.SeckillSessionEntity;
import site.wanjiahao.gulimall.seckill.to.SeckillSkuRelationEntity;
import site.wanjiahao.gulimall.seckill.to.SkuInfoEntity;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedissonClient redissonClient;

    private static final String SECKILL_SESSION_PREFIX = "seckill:session:";

    private static final String SECKILL_SKUINFO_PREFIX = "seckill:skuinfo:";

    private static final String SECKILL_STOCK_PREFIX = "seckill:STOCK:";

    private static final String SECKILL_BROUGHT_PREFIX = "seckill:brought:";

    @Override
    public void uploadThreeProduct() {
        R result = couponFeignService.getThreeSession();
        if (result.getCode() == 0) {
            // 成功查询
            List<SeckillSessionEntity> seckillSessionEntities = JSON.parseObject(JSON.toJSONString(result.get("sessions")), new TypeReference<List<SeckillSessionEntity>>() {
            });
            if (seckillSessionEntities != null && seckillSessionEntities.size() > 0) {
                // 保存至redis中
                // 1.缓存活动对应的商品id信息
                saveSessionSkuIds(seckillSessionEntities);
                // 2.缓存场次对应的秒杀信息，和商品详情信息
                saveSkuDetail(seckillSessionEntities);
                // 3.更新秒杀上架状态信息
                updatePublishStatus(seckillSessionEntities);
            }
        }
    }

    @Override
    public List<String> currentSeckillProducts() {
        List<String> result = new ArrayList<>();
        long currentTime = new Date().getTime();
        // 获取redis中所有的秒杀场次信息，筛选出当前时间段的秒杀商品
        Set<String> sessionKeys = stringRedisTemplate.keys(SECKILL_SESSION_PREFIX + "*");
        if (sessionKeys != null && sessionKeys.size() > 0) {
            for (String sessionKey : sessionKeys) {
                // 判断当前场次是否在当前时间段
                String timeStr = sessionKey.replace(SECKILL_SESSION_PREFIX, "");
                String[] timeAry = timeStr.split("_");
                long startTime = Long.parseLong(timeAry[0]);
                long endTime = Long.parseLong(timeAry[1]);
                if (currentTime >= startTime && currentTime <= endTime) {
                    // 当前场次筛选成功
                    List<String> combineStr = stringRedisTemplate.opsForList().range(sessionKey, -100, 100);
                    // 获取当前秒杀详情信息
                    if (combineStr != null && combineStr.size() > 0) {
                        List<String> stringList = combineStr.stream().map(item -> {
                            String[] split = item.split("_");
                            String sessionId = split[0];
                            String skuId = split[1];
                            String seckillDetailKey = SECKILL_SKUINFO_PREFIX + sessionId;
                            BoundHashOperations<String, String, String> hash = stringRedisTemplate.boundHashOps(seckillDetailKey);
                            return hash.get(skuId);
                        }).collect(Collectors.toList());
                        result.addAll(stringList);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public SeckillSkuRelationEntity seckillInfo(Long skuId) {
        Set<String> keys = stringRedisTemplate.keys(SECKILL_SKUINFO_PREFIX + "*");
        if (keys != null && keys.size() > 0) {
            for (String key : keys) {
                // 获取哈希
                BoundHashOperations<String, String, String> hash = stringRedisTemplate.boundHashOps(key);
                Boolean isExist = hash.hasKey(skuId.toString());
                if (isExist != null && isExist) {
                    return JSON.parseObject(hash.get(skuId.toString()), SeckillSkuRelationEntity.class);
                }
            }
        }
        return null;
    }

    /*
     *  秒杀商品服务
     *   1.验证秒杀请求的各种合法性
     *   2.发送消息至mq中，使用队列消锋的技术，来提高该接口的并发能力
     * */
    @Override
    public SeckillSuccessVo seckill(Long sessionId, Long skuId, Integer num, String token) {
        MemberEntityVo memberEntityVo = LoginInterceptor.threadLocal.get();
        // 1.获取秒杀商品
        BoundHashOperations<String, String, String> hash = stringRedisTemplate.boundHashOps(SECKILL_SKUINFO_PREFIX + sessionId);
        SeckillSkuRelationEntity seckillSkuRelationEntity = JSON.parseObject(hash.get(skuId.toString()), SeckillSkuRelationEntity.class);
        if (seckillSkuRelationEntity != null) {
            long current = new Date().getTime();
            long startTime = seckillSkuRelationEntity.getStartTime().getTime();
            long endTime = seckillSkuRelationEntity.getEndTime().getTime();
            Long redisSkuId = seckillSkuRelationEntity.getSkuId();
            String randomCode = seckillSkuRelationEntity.getRandomCode();
            int seckillLimit = seckillSkuRelationEntity.getSeckillLimit().intValue();
            if (isLegalTime(current, startTime, endTime) &&
                    isLegalToken(redisSkuId, randomCode, skuId, token) &&
                    isLegalNum(seckillLimit, num)) {
                // 判断当前用户是否已经买过该商品
                Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(SECKILL_BROUGHT_PREFIX + memberEntityVo.getId() + ":" + sessionId + ":" + redisSkuId,
                        "true",
                        endTime - startTime, TimeUnit.MILLISECONDS);
                if (success != null) {
                    if (success) {
                        // 成功设置 设置库存信号量减少
                        RSemaphore semaphore = redissonClient.getSemaphore(SECKILL_STOCK_PREFIX + sessionId + ":" + skuId);
                        // 尝试获取信号量值
                        boolean isSuccess = semaphore.tryAcquire(num);
                        if (isSuccess) {
                            /*
                             *  秒杀成功 创建秒杀订单实体
                             *  private String orderSn;
                             *
                             *  private String skuTitle;
                             *
                             *  private String skuImage;
                             *
                             *  private BigDecimal skuPrice;
                             *
                             *  private Integer totalMount;
                             */
                            SeckillSuccessVo seckillSuccessVo = new SeckillSuccessVo();
                            String orderSn = IdWorker.getTimeId();
                            SkuInfoEntity skuInfoEntity = seckillSkuRelationEntity.getSkuInfoEntity();
                            String skuTitle = skuInfoEntity.getSkuTitle();
                            String skuImage = skuInfoEntity.getSkuDefaultImg();
                            seckillSuccessVo.setOrderSn(orderSn);
                            seckillSuccessVo.setSkuTitle(skuTitle);
                            seckillSuccessVo.setSkuImage(skuImage);
                            seckillSuccessVo.setSkuPrice(seckillSkuRelationEntity.getSeckillPrice());
                            seckillSuccessVo.setTotalMount(num);
                            seckillSuccessVo.setSkuId(skuId);
                            seckillSuccessVo.setMemberId(memberEntityVo.getId());
                            // 发送消息至mq中 是的订单服务消费
                            rabbitTemplate.convertAndSend(OrderRabbitConstant.ORDER_EXCHANGE,
                                    SeckillConstant.SECKILL_QUEUE,
                                    seckillSuccessVo);
                            System.out.println("发送消息成功");
                            return seckillSuccessVo;
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean isLegalTime(long current, long startTime, long endTime) {
        return current >= startTime && current <= endTime;
    }

    private boolean isLegalToken(Long skuId, String token, Long verifySkuId, String verifyToken) {
        return verifySkuId.equals(skuId) && verifyToken.equals(token);
    }

    private boolean isLegalNum(int num, int verifyNum) {
        return verifyNum <= num;
    }

    private void updatePublishStatus(List<SeckillSessionEntity> seckillSessionEntities) {
        List<Long> ids = seckillSessionEntities.stream().map(SeckillSessionEntity::getId).collect(Collectors.toList());
        couponFeignService.publish(ids);
    }

    private void saveSkuDetail(List<SeckillSessionEntity> seckillSessionEntities) {
        // 构造map类型的数据 缓存至redis中
        seckillSessionEntities.forEach(item -> {
            BoundHashOperations<String, String, String> sessionHash = stringRedisTemplate.boundHashOps(SECKILL_SKUINFO_PREFIX + item.getId());
            List<SeckillSkuRelationEntity> seckillSkuRelationEntities = item.getSeckillSkuRelationEntities();
            seckillSkuRelationEntities.forEach(item1 -> {
                Long skuId = item1.getSkuId();
                // 查询远程商品详细信息
                R result = productFeignService.skuInfo(skuId);
                if (result.getCode() == 0) {
                    SkuInfoEntity skuInfo = JSON.parseObject(JSON.toJSONString(result.get("skuInfo")), SkuInfoEntity.class);
                    // 设置商品详细内容
                    item1.setSkuInfoEntity(skuInfo);
                }
                // 秒杀开始时间 结束时间
                item1.setStartTime(item.getStartTime());
                item1.setEndTime(item.getEndTime());
                // 设置商品随机码
                String randomCode = UUID.randomUUID().toString().replace(("-"), "");
                item1.setRandomCode(randomCode);
                // 保存库存信息至redis中
                RSemaphore semaphore = redissonClient.getSemaphore(SECKILL_STOCK_PREFIX + item.getId() + ":" + item1.getSkuId());
                semaphore.expire(item.getEndTime().getTime() - item.getStartTime().getTime(), TimeUnit.MILLISECONDS);
                semaphore.trySetPermits(item1.getSeckillCount().intValue());
                // 保存秒杀信息至redis中
                sessionHash.put(item1.getSkuId().toString(), JSON.toJSONString(item1));
            });
        });
    }

    private void saveSessionSkuIds(List<SeckillSessionEntity> seckillSessionEntities) {
        seckillSessionEntities.forEach(item -> {
            List<String> skuIds = item.getSeckillSkuRelationEntities().stream().map(item1 -> item.getId() + "_" + item1.getSkuId().toString()).collect(Collectors.toList());
            // 保存redis中 加入当前场次的时间信息 方便日后好查询出数据
            stringRedisTemplate
                    .opsForList()
                    .leftPushAll(SECKILL_SESSION_PREFIX + item.getStartTime().getTime() + "_" + item.getEndTime().getTime(), skuIds);
            // 设置过期时间
            stringRedisTemplate.expire(SECKILL_SESSION_PREFIX + item.getStartTime().getTime() + "_" + item.getEndTime().getTime(),
                    (item.getEndTime().getTime() - item.getStartTime().getTime()), TimeUnit.MILLISECONDS);
        });
    }
}
