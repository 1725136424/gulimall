package site.wanjiahao.gulimall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartItem {

    private Long spuId;

    private Long skuId;

    private String skuTitle;

    private String skuImg;

    private BigDecimal price;

    private Integer num;

    private BigDecimal totalPrice;

    private Boolean checked;

    private List<Attr> attrs;

    public BigDecimal getTotalPrice() {
        return price.multiply(new BigDecimal(num));
    }
}
