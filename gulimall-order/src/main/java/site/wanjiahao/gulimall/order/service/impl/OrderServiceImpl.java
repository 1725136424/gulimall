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
        // ????????????????????????
        MemberEntityVo memberEntityVo = LoginInterceptor.threadLocal.get();
        Long id = memberEntityVo.getId();
        settleAccountsVo.setUserId(id);
        settleAccountsVo.setIntegration(memberEntityVo.getIntegration());

        // ??????????????????????????????????????????????????????????????????????????????????????????ThreadLocal??????????????????Request
        // ???????????????????????????????????????????????????????????????RequestAttributes
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            // ???????????????????????????
            List<ReceiveAddressVo> allAddress = memberFeignService.findAllAddress(id);
            settleAccountsVo.setReceiveAddressVos(allAddress);
        }, executor);

        CompletableFuture<Void> cartItemFuture = CompletableFuture.supplyAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // ????????????????????? ?????????????????? ?????????????????????????????????????????????cookie???????????????feign??????????????????????????????
            // ???????????????????????????????????????
            List<OrderItemVo> checkCartItem = cartFeignService.findCheckCartItem();
            settleAccountsVo.setOrderItemVos(checkCartItem);
            return checkCartItem;
        }, executor)
                .thenAcceptAsync((param) -> {

                    CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(() -> {
                        List<Long> spuIds = param.stream().map(OrderItemVo::getSpuId).collect(Collectors.toList());
                        // ??????spu?????????????????? ?????????????????????????????????
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
                            log.error("??????????????????????????????{}", e.getMessage());
                        }
                    }, executor);

                    CompletableFuture<Void> voidCompletableFuture1 = CompletableFuture.runAsync(() -> {
                        List<Long> skuIds = param.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());

                        // ?????????????????????????????????
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
                            log.error("????????????????????????????????????{}", e.getMessage());
                        }
                    }, executor);
                    try {
                        CompletableFuture.allOf(voidCompletableFuture, voidCompletableFuture1).get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }, executor);
        CompletableFuture.allOf(addressFuture, cartItemFuture).get();
        // ??????????????????
        String token = UUID.randomUUID().toString().replace("-", "");
        settleAccountsVo.setToken(token);
        stringRedisTemplate.opsForValue().set(OrderConstant.ORDER_TOKEN_PREFIX + ":" + memberEntityVo.getId(), token, 30, TimeUnit.MINUTES);
        return settleAccountsVo;
    }

    //    @GlobalTransactional // ??????????????????
    @Transactional
    @Override
    public OrderResponseVo buildOrderResponseVo(OrderSubmitVo orderSubmitVo) throws ExecutionException, InterruptedException {
        OrderResponseVo orderResponseVo = new OrderResponseVo();
        String token = orderSubmitVo.getToken();
        MemberEntityVo memberEntityVo = LoginInterceptor.threadLocal.get();
        Long id = memberEntityVo.getId();
        // ???????????????????????????
        String script = "if redis.call('GET', KEYS[1]) == ARGV[1] then return redis.call('DEL', KEYS[1]) else return 0 end";
        Long execute = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class) {
        }, Collections.singletonList(OrderConstant.ORDER_TOKEN_PREFIX + ":" + id), token);
        if (execute != null) {
            if (!execute.equals(0L)) {
                // ???????????? --> ?????????????????????????????????
                // 1.??????????????????
                OrderEntity orderEntity = buildOrderEntity(orderSubmitVo, memberEntityVo);
                // 2.?????????????????????
                List<OrderItemEntity> orderItemEntities = buildOrderItemEntity(orderEntity);
                // 3.??????
                BigDecimal totalAmount = orderEntity.getTotalAmount();
                BigDecimal payAmount = orderEntity.getPayAmount();
                if (Math.abs(totalAmount.subtract(payAmount).doubleValue()) < 0.01) {
                    // ???????????? ???????????????????????????
                    saveOrderAndOrderItem(orderEntity, orderItemEntities);
                    // ????????????
                    Map<Long, Integer> lockMap = orderItemEntities.stream().collect(Collectors.toMap(OrderItemEntity::getSkuId, OrderItemEntity::getSkuQuantity));
                    LockStockTo lockStockTo = new LockStockTo();
                    lockStockTo.setLockMap(lockMap);
                    lockStockTo.setOrderSn(orderEntity.getOrderSn());
                    R r = wareFeignService.lockStock(lockStockTo);
                    if (r.getCode() == 0) {
                        orderResponseVo.setOrderEntity(orderEntity);
                        // ????????????????????????
                        rabbitTemplate.convertAndSend(OrderRabbitConstant.ORDER_EXCHANGE,
                                OrderRabbitConstant.ORDER_DELAY_QUEUE,
                                orderEntity);
                        // TODO ????????????????????????
                        orderResponseVo.setCode(0);
                    } else {
                        throw new StockNotEnoughException("??????????????????");
                    }
                } else {
                    // ????????????
                    orderResponseVo.setCode(3);
                }
            } else {
                // ????????????
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
        // ????????????????????????
        if (orderEntity != null) {
            Integer status = orderEntity.getStatus();
            // ?????????????????????????????????????????????
            if (status.equals(OrderStatusEnum.CREATE_NEW.getCode())) {
                // ????????????
                OrderEntity order = new OrderEntity();
                order.setStatus(OrderStatusEnum.CANCLED.getCode());
                order.setId(orderEntity.getId());
                baseMapper.updateById(order);
                // ??????????????????  ?????????????????????????????????????????????????????????
                OrderEntityTo orderEntityTo = new OrderEntityTo();
                BeanUtils.copyProperties(orderEntity, orderEntityTo);
                String messageId = UUID.randomUUID().toString().replace("-", "");
                try {
                    // TODO ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
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
                    // ??????????????????id
                    CorrelationData correlationData = new CorrelationData();
                    correlationData.setId(messageId);
                    rabbitTemplate.convertAndSend(WareRabbitConstant.STOCK_EXCHANGE,
                            "stock.release.other", orderEntityTo, correlationData);
                    mqMessageEntity.setMessageStatus(MQConstant.MQStatus.SENT.getCode());
                    wareFeignService.updateById(mqMessageEntity);
                } catch (Exception e) {
                    e.printStackTrace();
                    // ??????????????????????????????????????????
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
        // ??????????????????
        OrderEntity orderEntity = baseMapper.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        aliPayVo.setOutTradeNo(orderSn);
        aliPayVo.setTotalMount(orderEntity.getPayAmount());
        // TODO ??????????????????
        aliPayVo.setReturnUrl("http://order.gulimall.com/orderList.html");
        // ????????????????????????????????????
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
        // ??????????????????????????????????????????
        List<OrderEntity> orderEntities = baseMapper.selectList(new QueryWrapper<OrderEntity>().eq("member_id", id));
        // ????????????5???
        return orderEntities.stream().limit(5).map(item -> {
            OrderListHtmlVo orderListHtmlVo = new OrderListHtmlVo();
            BeanUtils.copyProperties(item, orderListHtmlVo);
            String orderSn = item.getOrderSn();
            // ??????????????????????????????????????????
            List<OrderItemEntity> orderItemEntities = orderItemService.listByOrderSn(orderSn);
            orderListHtmlVo.setOrderItemEntities(orderItemEntities);
            return orderListHtmlVo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void handleOrderResult(AlipayAsyncNotifyVo alipayAsyncNotifyVo) {
        // ????????????????????????
        paymentInfoService.savePaymentByAlipayNotify(alipayAsyncNotifyVo);
        // ??????????????????
        String out_trade_no = alipayAsyncNotifyVo.getOut_trade_no();
        OrderEntity orderEntity = baseMapper.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", out_trade_no));
        orderEntity.setStatus(OrderStatusEnum.PAYED.getCode());
        baseMapper.updateById(orderEntity);
    }

    @Override
    public void saveSeckillOrder(SeckillSuccessVo seckillSuccessVo) throws ExecutionException, InterruptedException {
        // ????????????????????????
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
            throw new RuntimeException("????????????????????????");
        }
    }

    private void saveOrderAndOrderItem(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
        // ????????????
        baseMapper.insert(orderEntity);
        for (OrderItemEntity orderItemEntity : orderItemEntities) {
            orderItemEntity.setOrderId(orderEntity.getId());
        }
        // ???????????????
        orderItemService.saveBatch(orderItemEntities);
    }

    private List<OrderItemEntity> buildOrderItemEntity(OrderEntity orderEntity) throws ExecutionException, InterruptedException {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<List<OrderItemEntity>> listCompletableFuture = CompletableFuture.supplyAsync(() -> {
            // ?????????????????????
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // ???????????????????????????????????????
            return cartFeignService.findCheckCartItem();
        }, executor).thenApplyAsync((checkCartItem) -> {
            List<Long> spuIds = checkCartItem.stream().map(OrderItemVo::getSpuId).collect(Collectors.toList());
            // ?????????????????????spu??????
            Map<Long, SpuInfoEntityTo> spuInfoMap = productFeignService.listSpuInfoMapByIds(spuIds);
            return checkCartItem.stream().map((item) -> {
                // order_id
                Long spuId = item.getSpuId();
                OrderItemEntity orderItemEntity = new OrderItemEntity();
                // ?????????
                orderItemEntity.setOrderSn(orderEntity.getOrderSn());
                // spu_id
                orderItemEntity.setSpuId(spuId);
                // spu_name
                orderItemEntity.setSpuName(spuInfoMap.get(spuId).getSpuName());
                // spu_pic --> ????????????sku?????????
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
                // sku_quantity ??????
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
     * @param orderSubmitVo  ??????????????????
     * @param memberEntityVo ??????id
     * @return
     */
    private OrderEntity buildOrderEntity(OrderSubmitVo orderSubmitVo, MemberEntityVo memberEntityVo) throws ExecutionException, InterruptedException {

        OrderEntity orderEntity = new OrderEntity();

        // ??????????????????
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        // ????????????????????????
        CompletableFuture<Void> normalFuture = CompletableFuture.runAsync(() -> {
            orderEntity.setMemberId(memberEntityVo.getId());
            // mybatis ???????????????
            String orderId = IdWorker.getTimeId();
            // ?????????
            orderEntity.setOrderSn(orderId);
            // ????????????
            orderEntity.setCreateTime(new Date());
            // ?????????
            orderEntity.setMemberUsername(memberEntityVo.getUsername());
            // ????????????
            orderEntity.setPayAmount(orderSubmitVo.getPayPrice());
            // ????????????
            orderEntity.setPayType(orderSubmitVo.getPayType());
            // ????????????
            orderEntity.setSourceType(OrderConstant.OrderSource.PC.getCode());
            // ????????????
            orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
            // ????????????????????????
            orderEntity.setAutoConfirmDay(30);
            // ??????????????????
            orderEntity.setConfirmStatus(ConfirmOrderStatusEnum.UN_CONFIRM.getCode());
            // ??????????????????
            orderEntity.setDeleteStatus(DeleteOrderStatusEnum.UN_DELETE.getCode());
            // ??????????????????
            orderEntity.setIntegrationAmount(new BigDecimal(memberEntityVo.getIntegration() * OrderConstant.PRICE_INTEGRATION_RATE));
            // ????????????
            orderEntity.setModifyTime(new Date());
        }, executor);

        // ????????????????????????
        CompletableFuture<Void> postageFuture = CompletableFuture.supplyAsync(() -> {
            // ????????????
            R postageRes = memberFeignService.getPostage(orderSubmitVo.getAddressId());
            BigDecimal postage = new BigDecimal(postageRes.get("postage") + "");
            orderEntity.setFreightAmount(postage);
            return postage;
        }, executor).thenAcceptAsync((postage) -> {
            // ????????????????????????
            // ????????????
            RequestContextHolder.setRequestAttributes(requestAttributes);
            BigDecimal totalPrice = new BigDecimal(cartFeignService.getTotalPrice().get("price") + "");
            BigDecimal subtract = totalPrice.subtract(orderSubmitVo.getIntegration().multiply(new BigDecimal("0.01")));
            orderEntity.setTotalAmount(subtract.add(postage));
            // ??????????????? ??????????????????
            orderEntity.setIntegration((int) (totalPrice.intValue() * OrderConstant.PRICE_INTEGRATION_RATE));
            // ?????????????????????
            orderEntity.setGrowth((int) (totalPrice.intValue() * OrderConstant.PRICE_INTEGRATION_RATE));
        }, executor);


        // ??????????????????????????????
        CompletableFuture<Void> defaultAddressFuture = CompletableFuture.runAsync(() -> {
            // ????????????
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

        // ????????????
        CompletableFuture.allOf(normalFuture, postageFuture, defaultAddressFuture).get();
        return orderEntity;
    }

    // ???????????????????????????
    private synchronized int verifyToken(String token, int userId) {
        String key = OrderConstant.ORDER_TOKEN_PREFIX + ":" + userId;
        // ??????redis???token
        String redisToken = stringRedisTemplate.opsForValue().get(key);
        if (token.equals(redisToken)) {
            // ????????????
            // ??????
            return stringRedisTemplate.delete(key) ? 1 : 0;
        } else {
            return 0;
        }
    }

}