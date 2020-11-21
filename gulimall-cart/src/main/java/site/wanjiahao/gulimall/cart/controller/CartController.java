package site.wanjiahao.gulimall.cart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import site.wanjiahao.common.constant.PageConstant;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.gulimall.cart.service.CartService;
import site.wanjiahao.gulimall.cart.vo.Cart;
import site.wanjiahao.gulimall.cart.vo.CartItem;

import java.util.concurrent.ExecutionException;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/cart")
    public String cart(@RequestParam("skuId") Long skuId,
                       @RequestParam("num") Integer num,
                       RedirectAttributes redirectAttributes)
            throws ExecutionException, InterruptedException {
        cartService.mappingCart(skuId, num);
        // 重定向跳转，防止表单重复提交
        redirectAttributes.addAttribute("skuId", skuId);
        return "redirect:"+ PageConstant.CART_URL + "/addToCart.html";
    }

    @GetMapping("/addToCart.html")
    public String success(@RequestParam("skuId") Long skuId,
                          Model model) {
        CartItem cartItem = cartService.findCartItemBySkuId(skuId);
        model.addAttribute("cartItem", cartItem);
        return "success";
    }

    // 购物车页面
    @GetMapping("/cartList")
    public String cartList(Model model) {
        // 获取当时用户的所有购物车信息
        Cart cart = cartService.listCurrentCartList();
        model.addAttribute("cart", cart);
        return "cartList";
    }

    // 调整购物车数量
    @GetMapping("/adjustNum")
    @ResponseBody
    public R adjustNum(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num) {
        if (num < 0) {
            num = 0;
        }
        cartService.adjustNum(skuId, num);
        return R.ok();
    }

    // 删除当前购物车商品
    @GetMapping("/delete")
    public String delete(@RequestParam("skuId") Long skuId) {
        cartService.delete(skuId);
        return "redirect:"+ PageConstant.CART_URL + "/cartList";
    }
}
