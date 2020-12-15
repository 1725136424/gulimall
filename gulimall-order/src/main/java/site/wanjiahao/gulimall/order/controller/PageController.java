package site.wanjiahao.gulimall.order.controller;

import com.alipay.easysdk.factory.Factory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import site.wanjiahao.common.constant.PageConstant;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.gulimall.order.feign.MemberFeignService;
import site.wanjiahao.gulimall.order.service.OrderService;
import site.wanjiahao.gulimall.order.utils.AliPayTemplate;
import site.wanjiahao.gulimall.order.vo.*;

import javax.servlet.http.HttpServletRequest;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Controller
public class PageController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private MemberFeignService memberFeignService;

    @GetMapping(value = {"/cash.html", "/cash"})
    public String cash() {
        return "cash";
    }

    @GetMapping(value = {"/orderList.html", "/orderList"})
    public String orderList(Model model) {
        // 获取当前用户所有的订单以及订单项信息
        List<OrderListHtmlVo> orderListHtmlVos = orderService.listOrderWithOrderItem();
        model.addAttribute("orders", orderListHtmlVos);
        return "orderList";
    }

    @Autowired
    private AliPayTemplate aliPayTemplate;

    /**
     * 构造结算页面需要的所有数据
     *
     * @return
     */
    @GetMapping(value = {"/settleAccounts.html", "/settleAccounts"})
    public String settleAccounts(Model model) throws ExecutionException, InterruptedException {
        SettleAccountsVo settleAccountsVo = orderService.structureSettleAccountsVo();
        model.addAttribute("settleAccount", settleAccountsVo);
        return "settleAccounts";
    }

    @GetMapping(value = {"/waitPay.html", "/waitPay"})
    public String waitPay() {
        return "waitPay";
    }

    /**
     * 根据地址获取邮费
     */
    @GetMapping("/getPostage/{addressId}")
    @ResponseBody
    public R getPostage(@PathVariable("addressId") Long addressId) {
        R r = memberFeignService.getPostage(addressId);
        return R.ok().put("postage", r.get("postage"));
    }

    /**
     * 提交订单
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo orderSubmitVo,
                              Model model, RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {
        OrderResponseVo orderResponseVo = new OrderResponseVo();
        try {
            orderResponseVo = orderService.buildOrderResponseVo(orderSubmitVo);
        } catch (Exception e) {
            orderResponseVo.setCode(4);
            e.printStackTrace();
        }
        if (orderResponseVo.getCode() == 0) {
            model.addAttribute("order", orderResponseVo.getOrderEntity());
            return "cash";
        } else {
            String message = "";
            switch (orderResponseVo.getCode()) {
                case 1:
                case 2:
                    message = "订单重复提交";
                    break;
                case 3:
                    message = "你的购物车还有一些商品未计算";
                    break;
                case 4:
                    message = "商品库存不足";
                    break;
            }
            redirectAttributes.addAttribute("message", message);
            return "redirect:" + PageConstant.ORDER_URL + "/settleAccounts";
        }
    }

    /**
     * 支付宝支付
     */
    @ResponseBody
    @GetMapping(value = "/alipay/{orderSn}", produces = "text/html")
    public String alipay(@PathVariable("orderSn") String orderSn) throws Exception {
        AliPayVo pay = orderService.pay(orderSn);
        String price = pay.getTotalMount().setScale(2, RoundingMode.CEILING).toString();
        return aliPayTemplate.pagePay(pay.getSubject(), pay.getOutTradeNo(), price, pay.getReturnUrl());
    }

    /**
     * 支付宝异步回调地址
     */
    @ResponseBody
    @PostMapping("/alipayAsyncNotify/handleOrderResult")
    public String handleOrderResult(HttpServletRequest request, AlipayAsyncNotifyVo alipayAsyncNotifyVo) throws Exception {
        // 验签
        HashMap<String, String> map = new HashMap<>();
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (String s : parameterMap.keySet()) {
            map.put(s, parameterMap.get(s)[0]);
        }
        Boolean isVerify = Factory.Payment.Common().verifyNotify(map);
        if (isVerify) {
            try {
                orderService.handleOrderResult(alipayAsyncNotifyVo);
            } catch (Exception e) {
                e.printStackTrace();
                return "error";
            }
            return "success";
        } else {
            return "error";
        }
    }
}
