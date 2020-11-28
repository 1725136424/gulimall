package site.wanjiahao.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import site.wanjiahao.common.constant.CartConstant;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.gulimall.cart.feign.ProductFeignService;
import site.wanjiahao.gulimall.cart.interceptor.ThreadLocalInterceptor;
import site.wanjiahao.gulimall.cart.pojo.SkuInfo;
import site.wanjiahao.gulimall.cart.pojo.UserInfo;
import site.wanjiahao.gulimall.cart.service.CartService;
import site.wanjiahao.gulimall.cart.vo.Attr;
import site.wanjiahao.gulimall.cart.vo.Cart;
import site.wanjiahao.gulimall.cart.vo.CartItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ThreadPoolExecutor executor;

    @Override
    public void mappingCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        CartItem cartItem = mappingCartItem(skuId, num);
        // 判断当前用户是否已经登录
        UserInfo userInfo = ThreadLocalInterceptor.threadLocal.get();
        String userId = userInfo.getUserId();
        if (userId != null) {
            // 已经登录
            saveCartItemToRedisCart(userId, cartItem);
            // 临时购物车和并
            mergeTempCartToLoginCart();
        } else {
            // 未登录 临时购物车
            String userKey = userInfo.getUserKey();
            saveCartItemToRedisCart(userKey, cartItem);
        }
    }

    @Override
    public void mergeTempCartToLoginCart() {
        UserInfo userInfo = ThreadLocalInterceptor.threadLocal.get();
        // 查询临时购物车数据
        BoundHashOperations<String, Object, Object> tempBoundHash = stringRedisTemplate.boundHashOps(CartConstant.CART_PREFIX + ":" + userInfo.getUserKey());
        Map<Object, Object> entries = tempBoundHash.entries();
        if (entries != null) {
            Set<Map.Entry<Object, Object>> entriesSet = entries.entrySet();
            for (Map.Entry<Object, Object> objectObjectEntry : entriesSet) {
                CartItem cartItem = JSON.parseObject(objectObjectEntry.getValue() + "", CartItem.class);
                saveCartItemToRedisCart(userInfo.getUserId(), cartItem);
            }
            // 删除临时购物车
            stringRedisTemplate.delete(CartConstant.CART_PREFIX + ":" + userInfo.getUserKey());
        }
    }

    @Override
    public void saveCartItemToRedisCart(String cartType, CartItem cartItem) {
        BoundHashOperations<String, Object, Object> boundHash = stringRedisTemplate.boundHashOps(CartConstant.CART_PREFIX + ":" + cartType);
        // 判断当前购物车是否以及存在当前购物项数据
        if (boundHash.hasKey(cartItem.getSkuId() + "")) {
            // 存在当前购物项
            CartItem redisCartItem = JSON.parseObject(boundHash.get(cartItem.getSkuId() + "") + "", CartItem.class);
            redisCartItem.setNum(redisCartItem.getNum() + cartItem.getNum());
            cartItem = redisCartItem;
        }
        // 保存当前购物项至redis中
        boundHash.put(cartItem.getSkuId() + "", JSON.toJSONString(cartItem));
    }

    /**
     * 多次远程调用，可以考虑使用异步操作
     *
     * @param skuId
     * @param num
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public CartItem mappingCartItem(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        CartItem cartItem = new CartItem();
        // 获取当前产品信息
        CompletableFuture<Void> async1 = CompletableFuture.runAsync(() -> {
            R r = productFeignService.skuInfo(skuId);
            SkuInfo skuInfo = JSON.parseObject(JSON.toJSONString(r.get("skuInfo")), SkuInfo.class);
            cartItem.setChecked(true);
            cartItem.setNum(num);
            cartItem.setPrice(skuInfo.getPrice());
            cartItem.setSkuId(skuInfo.getSkuId());
            cartItem.setSkuTitle(skuInfo.getSkuTitle());
            cartItem.setSkuImg(skuInfo.getSkuDefaultImg());
            cartItem.setSpuId(skuInfo.getSpuId());
        }, executor);

        CompletableFuture<Void> async2 = CompletableFuture.runAsync(() -> {
            // 查询当前产品的属性信息 属性名称， 属性值
            R attrRes = productFeignService.infoAttr(skuId);
            List<Attr> attrs = JSON.parseObject(JSON.toJSONString(attrRes.get("attrs")), new TypeReference<List<Attr>>() {
            });
            cartItem.setAttrs(attrs);
        }, executor);
        CompletableFuture.allOf(async1, async2).get();
        return cartItem;
    }

    @Override
    public CartItem findCartItemBySkuId(Long skuId) {
        BoundHashOperations<String, Object, Object> boundHash = getCurrentCart();
        return JSON.parseObject(boundHash.get(skuId.toString()) + "", CartItem.class);
    }

    @Override
    public Cart listCurrentCartList() {
        UserInfo userInfo = ThreadLocalInterceptor.threadLocal.get();
        String userKey = userInfo.getUserKey();
        String userId = userInfo.getUserId();
        String findKey = "";
        if (!StringUtils.isBlank(userId)) {
            // 获取用户购物车数据 --> 合并临时购物车
            mergeTempCartToLoginCart();
            findKey = userId;
        } else {
            // 获取临时购物车数据
            findKey = userKey;
        }
        return findCartByRedisWhitType(findKey);
    }

    @Override
    public Cart findCartByRedisWhitType(String type) {
        BoundHashOperations<String, Object, Object> boundHash = stringRedisTemplate.boundHashOps(CartConstant.CART_PREFIX + ":" + type);
        Map<Object, Object> entries = boundHash.entries();
        if (entries != null) {
            Cart cart = new Cart();
            Set<Map.Entry<Object, Object>> cartItemSet = entries.entrySet();
            List<CartItem> cartItems = cartItemSet.stream().map(item -> JSON.parseObject(item.getValue() + "", CartItem.class)).collect(Collectors.toList());
            cart.setCartItems(cartItems);
            return cart;
        }
        return null;
    }

    @Override
    public void adjustNum(Long skuId, Integer num) {
        if (num == 0) {
            delete(skuId);
        } else {
            CartItem cartItem = findCartItemBySkuId(skuId);
            // 删除
            delete(skuId);
            cartItem.setNum(num);
            UserInfo userInfo = ThreadLocalInterceptor.threadLocal.get();
            if (userInfo.getUserId() != null) {
                saveCartItemToRedisCart(userInfo.getUserId(), cartItem);
            } else {
                saveCartItemToRedisCart(userInfo.getUserKey(), cartItem);
            }
        }
    }

    @Override
    public void delete(Long skuId) {
        BoundHashOperations<String, Object, Object> currentCart = getCurrentCart();
        currentCart.delete(skuId + "");
    }

    private BoundHashOperations<String, Object, Object> getCurrentCart() {
        UserInfo userInfo = ThreadLocalInterceptor.threadLocal.get();
        if (!StringUtils.isBlank(userInfo.getUserId())) {
            return stringRedisTemplate.boundHashOps(CartConstant.CART_PREFIX + ":" + userInfo.getUserId());
        } else {
            return stringRedisTemplate.boundHashOps(CartConstant.CART_PREFIX + ":" + userInfo.getUserKey());
        }
    }

    @Override
    public List<CartItem> findCheckCartItem() {
        Cart cart = listCurrentCartList();
        List<CartItem> cartItems = cart.getCartItems();
        // 当前购物车的商品数据可能是很久以前放入的商品，而商品的价格在实时的变化，这个就保证不了价格的准确性--》当我们
        // 确认订单的时候，我们需要重新查询一遍购物车，这样才能保证价格的准确性
        for (CartItem item : cartItems
                .stream()
                .filter(CartItem::getChecked)
                .collect(Collectors.toList())) {
            Long skuId = item.getSkuId();
            // 远程服务查询
            R r = productFeignService.skuInfo(skuId);
            SkuInfo skuInfo = JSON.parseObject(JSON.toJSONString(r.get("skuInfo")), SkuInfo.class);
            item.setPrice(skuInfo.getPrice());
        }
        return cartItems;
    }

    @Override
    public BigDecimal getTotalPrice() {
        List<CartItem> checkCartItem = findCheckCartItem();
        // 求总价钱
        Optional<BigDecimal> optionalBigDecimal = checkCartItem.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal::add);
        // 获取当前optionalBigDecimal的值，如果没有至使用BigDecimal.ZERO
        return optionalBigDecimal.orElse(BigDecimal.ZERO);
    }
}
