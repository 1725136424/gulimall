package site.wanjiahao.gulimall.seckill.service;

import site.wanjiahao.common.vo.SeckillSuccessVo;
import site.wanjiahao.gulimall.seckill.to.SeckillSkuRelationEntity;

import java.util.List;

public interface SeckillService {

    void uploadThreeProduct();

    List<String> currentSeckillProducts();

    SeckillSkuRelationEntity seckillInfo(Long skuId);

    SeckillSuccessVo seckill(Long sessionId, Long skuId, Integer num, String token);
}
