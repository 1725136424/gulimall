package site.wanjiahao.gulimall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class Cart {

    private List<CartItem> cartItems;

    private Integer totalCount;

    private Integer totalType;

    private BigDecimal totalPrice;

    public Integer getTotalCount() {
        if (cartItems != null && cartItems.size() > 0) {
            Integer count = 0;
            for (CartItem cartItem : cartItems) {
                count += cartItem.getNum();
            }
            return count;
        }
        return 0;
    }

    public BigDecimal getTotalPrice() {
        if (cartItems != null && cartItems.size() > 0) {
            BigDecimal totalPrice = BigDecimal.ZERO;
            for (CartItem cartItem : cartItems) {
                totalPrice = totalPrice.add(cartItem.getTotalPrice());
            }
            return totalPrice;
        }
        return BigDecimal.ZERO;
    }

    public Integer getTotalType() {
        return cartItems == null? 0: cartItems.size();
    }
}
