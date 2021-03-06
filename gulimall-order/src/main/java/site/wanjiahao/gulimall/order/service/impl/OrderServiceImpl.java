package site.wanjiahao.gulimall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import site.wanjiahao.common.constant.*;
import site.wanjiahao.common.exception.StockNotEnoughException;
import site.wanjiahao.common.to.LockStockTo;
import site.wanjiahao.common.to.OrderEntityTo;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.Query;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.common.vo.MemberEntityVo;
import site.wanjiahao.common.vo.SeckillSuccessVo;
import site.wanjiahao.gulimall.order.dao.OrderDao;
import site.wanjiahao.gulimall.order.entity.MQMessageEntity;
import site.wanjiahao.gulimall.order.entity.OrderEntity;
import site.wanjiahao.gulimall.order.entity.OrderItemEntity;
import site.wanjiahao.gulimall.order.feign.CartFeignService;
import site.wanjiahao.gulimall.order.feign.MemberFeignService;
import site.wanjiahao.gulimall.order.feign.ProductFeignService;
import site.wanjiahao.gulimall.order.feign.WareFeignService;
import site.wanjiahao.gulimall.order.interceptor.LoginInterceptor;
import site.wanjiahao.gulimall.order.service.OrderItemService;
import site.wanjiahao.gulimall.order.service.OrderService;
import site.wanjiahao.gulimall.order.service.PaymentInfoService;
import site.wanjiahao.gulimall.order.to.SpuInfoEntityTo;
import site.wanjiahao.gulimall.order.vo.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private ThreadPoolExecutor executor;

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private CartFeignService cartFeignService;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PaymentInfoService paymentInfoService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public SettleAccountsVo structureSettleAccountsVo() throws ExecutionException, InterruptedException {
        SettleAccountsVo settleAccountsVo = new SettleAccountsVo();
        // 获取当前用户信息
        MemberEntityVo memberEntityVo = LoginInterceptor.threadLocal.get();
        Long id = memberEntityVo.getId();
        settleAccountsVo.setUserId(id);
        settleAccountsVo.setIntegration(memberEntityVo.getIntegration());

        // 开启异步任务的话，就会开启新的线程，如果开启新的线程的话，重ThreadLocal中就获取不到Request
        // 所以在开启每一个异步任务的时候，都需要设置RequestAttributes
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            // 获取收货人地址集合
            List<ReceiveAddressVo> allAddress = memberFeignService.findAllAddress(id);
            settleAccountsVo.setReceiveAddressVos(allAddress);
        }, executor);

        CompletableFuture<Void> cartItemFuture = CompletableFuture.supplyAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 获取当前购物车 需要注意的是 当前已经登录，是按照请求头中的cookie来知道的，feign远程调用会丢失请求头
            // 我们需要加上一个请求拦截器
            List<OrderItemVo> checkCartItem = cartFeignService.findCheckCartItem();
            settleAccountsVo.setOrderItemVos(checkCartItem);
            return checkCartItem;
        }, executor)
                .thenAcceptAsync((param) -> {

                    CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(() -> {
                        List<Long> spuIds = param.stream().map(OrderItemVo::getSpuId).collect(Collectors.toList());
                        // 根据spu获取重量信息 获取当前商品的重量信息
                        try {
                            Map<Long, BigDecimal> weightBySpuIds = productFeignService.getWeightBySpuIds(spuIds);
                            if (weightBySpuIds != null && weightBySpuIds.size() > 0) {
                                param.forEach((item) -> {
                                    Long spuId = item.getSpuId();
                                    BigDecimal weight = weightBySpuIds.get(spuId);
                                    item.setWeight(weight);
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            log.error("远程调用商品服务异常{}", e.getMessage());
                        }
                    }, executor);

                    CompletableFuture<Void> voidCompletableFuture1 = CompletableFuture.runAsync(() -> {
                        List<Long> skuIds = param.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());

                        // 获取当前商品的库存信息
                        try {
                            Map<Long, Boolean> stockMap = wareFeignService.hasStocks(skuIds);
                            if (stockMap != null && stockMap.size() > 0) {
                                param.forEach((item) -> {
                                    Long skuId = item.getSkuId();
                                    Boolean hasStock = stockMap.get(skuId);
                                    item.setHasStock(hasStock == null ? false : hasStock);
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            log.error("远程调用库存服务异常异常{}", e.getMessage());
                        }
                    }, executor);
                    try {
                        CompletableFuture.allOf(voidCompletableFuture, voidCompletableFuture1).get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }, executor);
        CompletableFuture.allOf(addressFuture, cartItemFuture).get();
        // 设置防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        settleAccountsVo.setToken(token);
        stringRedisTemplate.opsForValue().set(OrderConstant.ORDER_TOKEN_PREFIX + ":" + memberEntityVo.getId(), token, 30, TimeUnit.MINUTES);
        return settleAccountsVo;
    }

    //    @GlobalTransactional // 全局事务注解
    @Transactional
    @Override
    public OrderResponseVo buildOrderResponseVo(OrderSubmitVo orderSubmitVo) throws ExecutionException, InterruptedException {
        OrderResponseVo orderResponseVo = new OrderResponseVo();
        String token = orderSubmitVo.getToken();
        MemberEntityVo memberEntityVo = LoginInterceptor.threadLocal.get();
        Long id = memberEntityVo.getId();
        // 原子验证令牌正确性
        String script = "if redis.call('GET', KEYS[1]) == ARGV[1] then return redis.call('DEL', KEYS[1]) else return 0 end";
        Long execute = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class) {
        }, Collections.singletonList(OrderConstant.ORDER_TOKEN_PREFIX + ":" + id), token);
        if (execute != null) {
            if (!execute.equals(0L)) {
                // 验证成功 --> 构建订单以及订单项数据
                // 1.构建订单实体
                OrderEntity orderEntity = buildOrderEntity(orderSubmitVo, memberEntityVo);
                // 2.构建订单项实体
                List<OrderItemEntity> orderItemEntities = buildOrderItemEntity(orderEntity);
                // 3.验价
                BigDecimal totalAmount = orderEntity.getTotalAmount();
                BigDecimal payAmount = orderEntity.getPayAmount();
                if (Math.abs(totalAmount.subtract(payAmount).doubleValue()) < 0.01) {
                    // 验价成功 保存订单以及订单项
                    saveOrderAndOrderItem(orderEntity, orderItemEntities);
                    // 锁定库存
                    Map<Long, Integer> lockMap = orderItemEntities.stream().collect(Collectors.toMap(OrderItemEntity::getSkuId, OrderItemEntity::getSkuQuantity));
                    LockStockTo lockStockTo = new LockStockTo();
                    lockStockTo.setLockMap(lockMap);
                    lockStockTo.setOrderSn(orderEntity.getOrderSn());
                    R r = wareFeignService.lockStock(lockStockTo);
                    if (r.getCode() == 0) {
                        orderResponseVo.setOrderEntity(orderEntity);
                        // 延迟关闭订单功能
                        rabbitTemplate.convertAndSend(OrderRabbitConstant.ORDER_EXCHANGE,
                                OrderRabbitConstant.ORDER_DELAY_QUEUE,
                                orderEntity);
                        // TODO 远程保存积分信息
                        orderResponseVo.setCode(0);
                    } else {
                        throw new StockNotEnoughException("库存不足异常");
                    }
                } else {
                    // 验价失败
                    orderResponseVo.setCode(3);
                }
            } else {
                // 验证失败
                orderResponseVo.setCode(2);
            }
        } else {
            orderResponseVo.setCode(1);
        }

        return orderResponseVo;
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        return baseMapper.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
    }

    @Override
    public void closeOrder(OrderEntity orderEntity) {
        // 获取当前订单状态
        if (orderEntity != null) {
            Integer status = orderEntity.getStatus();
            // 如果是代付款状态，需要关闭订单
            if (status.equals(OrderStatusEnum.CREATE_NEW.getCode())) {
                // 关闭订单
                OrderEntity order = new OrderEntity();
                order.setStatus(OrderStatusEnum.CANCLED.getCode());
                order.setId(orderEntity.getId());
                baseMapper.updateById(order);
                // 立即解锁库存  发送给解锁库存的消息队列，直接解锁库存
                OrderEntityTo orderEntityTo = new OrderEntityTo();
                BeanUtils.copyProperties(orderEntity, orderEntityTo);
                String messageId = UUID.randomUUID().toString().replace("-", "");
                try {
                    // TODO 保证消息的可靠性，才是保证分布式事务的最关键因素，做好日志记录，保证所有的消息的可以发出
                    MQMessageEntity mqMessageEntity = new MQMessageEntity();
                    mqMessageEntity.setMessageId(messageId);
                    mqMessageEntity.setContent(JSON.toJSONString(orderEntityTo));
                    mqMessageEntity.setToChange(WareRabbitConstant.STOCK_EXCHANGE);
                    mqMessageEntity.setRoutingKey("stock.release.other");
                    mqMessageEntity.setClassType(OrderEntityTo.class.getTypeName());
                    mqMessageEntity.setMessageStatus(MQConstant.MQStatus.NEW.getCode());
                    mqMessageEntity.setCreateTime(new Date());
                    mqMessageEntity.setUpdateTime(new Date());
                    wareFeignService.saveMessage(mqMessageEntity);
                    // 设置消息唯一id
                    CorrelationData correlationData = new CorrelationData();
                    correlationData.setId(messageId);
                    rabbitTemplate.convertAndSend(WareRabbitConstant.STOCK_EXCHANGE,
                            "stock.release.other", orderEntityTo, correlationData);
                    mqMessageEntity.setMessageStatus(MQConstant.MQStatus.SENT.getCode());
                    wareFeignService.updateById(mqMessageEntity);
                } catch (Exception e) {
                    e.printStackTrace();
                    // 查出当前异常的信息，修改状态
                    R r = wareFeignService.selectById(messageId);
                    MQMessageEntity mqMessageEntity = JSON.parseObject(JSON.toJSONString(r.get("mqMessage")), MQMessageEntity.class);
                    if (mqMessageEntity != null &&
                            (mqMessageEntity.getMessageStatus() == MQConstant.MQStatus.NEW.getCode() ||
                                    mqMessageEntity.getMessageStatus() == MQConstant.MQStatus.SENT.getCode())) {
                        mqMessageEntity.setMessageStatus(MQConstant.MQStatus.ERROR_DELIVERED.getCode());
                        wareFeignService.updateById(mqMessageEntity);
                    }
                }
            }
        }
    }

    @Override
    public AliPayVo pay(String orderSn) {
        AliPayVo aliPayVo = new AliPayVo();
        // 查询当前订单
        OrderEntity orderEntity = baseMapper.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        aliPayVo.setOutTradeNo(orderSn);
        aliPayVo.setTotalMount(orderEntity.getPayAmount());
        // TODO 设置回调地址
        aliPayVo.setReturnUrl("http://order.gulimall.com/orderList.html");
        // 查询当前订单对应的订单项
        List<OrderItemEntity> orderItemEntities = orderItemService.listByOrderSn(orderSn);
        if (orderItemEntities != null && orderItemEntities.size() > 0) {
            OrderItemEntity orderItemEntity = orderItemEntities.get(0);
            aliPayVo.setSubject(orderItemEntity.getSkuName());
        }
        return aliPayVo;
    }

    @Override
    public List<OrderListHtmlVo> listOrderWithOrderItem() {
        MemberEntityVo memberEntityVo = LoginInterceptor.threadLocal.get();
        Long id = memberEntityVo.getId();
        // 获取当前用户所有的的购物列表
        List<OrderEntity> orderEntities = baseMapper.selectList(new QueryWrapper<OrderEntity>().eq("member_id", id));
        // 最多显示5个
        return orderEntities.stream().limit(5).map(item -> {
            OrderListHtmlVo orderListHtmlVo = new OrderListHtmlVo();
            BeanUtils.copyProperties(item, orderListHtmlVo);
            String orderSn = item.getOrderSn();
            // 查询当前订单对应的订单项信息
            List<OrderItemEntity> orderItemEntities = orderItemService.listByOrderSn(orderSn);
            orderListHtmlVo.setOrderItemEntities(orderItemEntities);
            return orderListHtmlVo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void handleOrderResult(AlipayAsyncNotifyVo alipayAsyncNotifyVo) {
        // 保存订单付款信息
        paymentInfoService.savePaymentByAlipayNotify(alipayAsyncNotifyVo);
        // 更新订单状态
        String out_trade_no = alipayAsyncNotifyVo.getOut_trade_no();
        OrderEntity orderEntity = baseMapper.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", out_trade_no));
        orderEntity.setStatus(OrderStatusEnum.PAYED.getCode());
        baseMapper.updateById(orderEntity);
    }

    @Override
    public void saveSeckillOrder(SeckillSuccessVo seckillSuccessVo) throws ExecutionException, InterruptedException {
        // 查询当前用户信息
        Long memberId = seckillSuccessVo.getMemberId();
        R info = memberFeignService.info(memberId);
        ReceiveAddressVo defaultAddress = memberFeignService.findDefaultAddress(memberId);
        if (info.getCode() == 0 && defaultAddress != null) {
            MemberEntityVo member = JSON.parseObject(JSON.toJSONString(info.get("member")), MemberEntityVo.class);
            OrderSubmitVo orderSubmitVo = new OrderSubmitVo();
            orderSubmitVo.setAddressId(defaultAddress.getId());
            orderSubmitVo.setIntegration(BigDecimal.ZERO);
            orderSubmitVo.setPayPrice(seckillSuccessVo.getSkuPrice());
            orderSubmitVo.setPayType(1);
            OrderEntity orderEntity = buildOrderEntity(orderSubmitVo, member);
            orderEntity.setOrderSn(seckillSuccessVo.getOrderSn());
            OrderItemEntity orderItemEntity = new OrderItemEntity();
            orderItemEntity.setSkuPic(seckillSuccessVo.getSkuImage());
            orderItemEntity.setSkuPrice(seckillSuccessVo.getSkuPrice());
            orderItemEntity.setSkuId(seckillSuccessVo.getSkuId());
            orderItemEntity.setOrderSn(seckillSuccessVo.getOrderSn());
            orderItemEntity.setSkuName(seckillSuccessVo.getSkuTitle());
            orderItemEntity.setSkuQuantity(seckillSuccessVo.getTotalMount());
            saveOrderAndOrderItem(orderEntity,Collections.singletonList(orderItemEntity));
        } else {
            throw new RuntimeException("远程调用发生异常");
        }
    }

    private void saveOrderAndOrderItem(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
        // 保存订单
        baseMapper.insert(orderEntity);
        for (OrderItemEntity orderItemEntity : orderItemEntities) {
            orderItemEntity.setOrderId(orderEntity.getId());
        }
        // 保存订单项
        orderItemService.saveBatch(orderItemEntities);
    }

    private List<OrderItemEntity> buildOrderItemEntity(OrderEntity orderEntity) throws ExecutionException, InterruptedException {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<List<OrderItemEntity>> listCompletableFuture = CompletableFuture.supplyAsync(() -> {
            // 保存线程一致性
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 远程调用获取购物车商品信息
            return cartFeignService.findCheckCartItem();
        }, executor).thenApplyAsync((checkCartItem) -> {
            List<Long> spuIds = checkCartItem.stream().map(OrderItemVo::getSpuId).collect(Collectors.toList());
            // 远程查询素有的spu信息
            Map<Long, SpuInfoEntityTo> spuInfoMap = productFeignService.listSpuInfoMapByIds(spuIds);
            return checkCartItem.stream().map((item) -> {
                // order_id
                Long spuId = item.getSpuId();
                OrderItemEntity orderItemEntity = new OrderItemEntity();
                // 订单号
                orderItemEntity.setOrderSn(orderEntity.getOrderSn());
                // spu_id
                orderItemEntity.setSpuId(spuId);
                // spu_name
                orderItemEntity.setSpuName(spuInfoMap.get(spuId).getSpuName());
                // spu_pic --> 直接设置sku图片，
                orderItemEntity.setSpuPic(item.getSkuImg());
                orderItemEntity.setSkuPic(item.getSkuImg());
                // spu_brand
                orderItemEntity.setSpuBrand(spuInfoMap.get(spuId).getBrandId() + "");
                // category_id
                orderItemEntity.setCategoryId(spuInfoMap.get(spuId).getCatelogId());
                // sku_id
                orderItemEntity.setSkuId(item.getSkuId());
                // sku_name
                orderItemEntity.setSkuName(item.getSkuTitle());
                // sku_pic
                orderItemEntity.setSkuPrice(item.getPrice());
                // sku_quantity 数量
                orderItemEntity.setSkuQuantity(item.getNum());
                // sku_attr_values json
                List<String> strAttrs = item.getAttrs().stream().map(JSON::toJSONString).collect(Collectors.toList());
                String jsonAttr = StringUtils.collectionToDelimitedString(strAttrs, ",");
                orderItemEntity.setSkuAttrsVals(jsonAttr);
                return orderItemEntity;
            }).collect(Collectors.toList());
        });
        return listCompletableFuture.get();
    }

    /**
     * @param orderSubmitVo  订单提交数据
     * @param memberEntityVo 用户id
     * @return
     */
    private OrderEntity buildOrderEntity(OrderSubmitVo orderSubmitVo, MemberEntityVo memberEntityVo) throws ExecutionException, InterruptedException {

        OrderEntity orderEntity = new OrderEntity();

        // 获取当前请求
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        // 普通数据异步设置
        CompletableFuture<Void> normalFuture = CompletableFuture.runAsync(() -> {
            orderEntity.setMemberId(memberEntityVo.getId());
            // mybatis 构造订单号
            String orderId = IdWorker.getTimeId();
            // 订单号
            orderEntity.setOrderSn(orderId);
            // 创建时间
            orderEntity.setCreateTime(new Date());
            // 用户名
            orderEntity.setMemberUsername(memberEntityVo.getUsername());
            // 应付总额
            orderEntity.setPayAmount(orderSubmitVo.getPayPrice());
            // 支付类型
            orderEntity.setPayType(orderSubmitVo.getPayType());
            // 订单来源
            orderEntity.setSourceType(OrderConstant.OrderSource.PC.getCode());
            // 订单状态
            orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
            // 订单自动确认时间
            orderEntity.setAutoConfirmDay(30);
            // 订单确认状态
            orderEntity.setConfirmStatus(ConfirmOrderStatusEnum.UN_CONFIRM.getCode());
            // 订单删除状态
            orderEntity.setDeleteStatus(DeleteOrderStatusEnum.UN_DELETE.getCode());
            // 积分抵扣金额
            orderEntity.setIntegrationAmount(new BigDecimal(memberEntityVo.getIntegration() * OrderConstant.PRICE_INTEGRATION_RATE));
            // 修改时间
            orderEntity.setModifyTime(new Date());
        }, executor);

        // 远程查询运费总额
        CompletableFuture<Void> postageFuture = CompletableFuture.supplyAsync(() -> {
            // 运费总额
            R postageRes = memberFeignService.getPostage(orderSubmitVo.getAddressId());
            BigDecimal postage = new BigDecimal(postageRes.get("postage") + "");
            orderEntity.setFreightAmount(postage);
            return postage;
        }, executor).thenAcceptAsync((postage) -> {
            // 远程查询订单总额
            // 订单总额
            RequestContextHolder.setRequestAttributes(requestAttributes);
            BigDecimal totalPrice = new BigDecimal(cartFeignService.getTotalPrice().get("price") + "");
            BigDecimal subtract = totalPrice.subtract(orderSubmitVo.getIntegration().multiply(new BigDecimal("0.01")));
            orderEntity.setTotalAmount(subtract.add(postage));
            // 可获得积分 根据价钱比例
            orderEntity.setIntegration((int) (totalPrice.intValue() * OrderConstant.PRICE_INTEGRATION_RATE));
            // 可以获得成长值
            orderEntity.setGrowth((int) (totalPrice.intValue() * OrderConstant.PRICE_INTEGRATION_RATE));
        }, executor);


        // 远程查询默认地址信息
        CompletableFuture<Void> defaultAddressFuture = CompletableFuture.runAsync(() -> {
            // 地址设置
            ReceiveAddressVo defaultAddress = memberFeignService.findDefaultAddress(memberEntityVo.getId());
            orderEntity.setReceiverProvince(defaultAddress.getProvince());
            orderEntity.setReceiverCity(defaultAddress.getCity());
            orderEntity.setReceiverRegion(defaultAddress.getRegion());
            orderEntity.setReceiverDetailAddress(defaultAddress.getDetailAddress());
            orderEntity.setReceiverName(defaultAddress.getName());
            orderEntity.setReceiverPhone(defaultAddress.getPhone());
            orderEntity.setReceiverPostCode(defaultAddress.getPostCode());
            orderEntity.setReceiverName(defaultAddress.getName());
        }, executor);

        // 阻塞等待
        CompletableFuture.allOf(normalFuture, postageFuture, defaultAddressFuture).get();
        return orderEntity;
    }

    // 锁方式实现同步校验
    private synchronized int verifyToken(String token, int userId) {
        String key = OrderConstant.ORDER_TOKEN_PREFIX + ":" + userId;
        // 获取redis中token
        String redisToken = stringRedisTemplate.opsForValue().get(key);
        if (token.equals(redisToken)) {
            // 校验成功
            // 删除
            return stringRedisTemplate.delete(key) ? 1 : 0;
        } else {
            return 0;
        }
    }

}