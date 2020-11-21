package site.wanjiahao.gulimall.cart.service;

import site.wanjiahao.gulimall.cart.vo.Cart;
import site.wanjiahao.gulimall.cart.vo.CartItem;

import java.util.concurrent.ExecutionException;

public interface CartService {

    void mappingCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    void mergeTempCartToLoginCart();

    void saveCartItemToRedisCart(String cartType, CartItem cartItem);

    CartItem mappingCartItem(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    CartItem findCartItemBySkuId(Long skuId);

    Cart listCurrentCartList();

    Cart findCartByRedisWhitType(String type);

    void adjustNum(Long skuId, Integer num);

    void delete(Long skuId);

}
