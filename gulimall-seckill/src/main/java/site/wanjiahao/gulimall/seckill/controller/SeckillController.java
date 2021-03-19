package site.wanjiahao.gulimall.seckill.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.common.vo.SeckillSuccessVo;
import site.wanjiahao.gulimall.seckill.service.SeckillService;
import site.wanjiahao.gulimall.seckill.to.SeckillSkuRelationEntity;

import java.util.List;

@Controller
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    @GetMapping("/current_seckill_products")
    @ResponseBody
    public List<String> list() {
        // 获取当前的秒杀商品
        return seckillService.currentSeckillProducts();
    }

    @GetMapping("/sku/seckill/info/{skuId}")
    @ResponseBody
    public R seckillInfo(@PathVariable("skuId") Long skuId) {
        // 获取当前商品的秒杀信息
        try {
            SeckillSkuRelationEntity seckillSkuRelationEntity = seckillService.seckillInfo(skuId);
            return R.ok().put("entity", seckillSkuRelationEntity);
        } catch (Exception e) {
            return R.error().put("entity", null);
        }
    }

    // 秒杀逻辑 params: 商品id 数量 随机令牌
    @GetMapping("/auth/seckill")
    public String seckill(@RequestParam("sessionId") Long sessionId,
                                    @RequestParam("skuId") Long skuId,
                                    @RequestParam("num") Integer num,
                                    @RequestParam("token") String token,
                                    Model model) {
        SeckillSuccessVo modelResult = seckillService.seckill(sessionId, skuId, num, token);
        model.addAttribute("model", modelResult);
        return "success";
    }

}
