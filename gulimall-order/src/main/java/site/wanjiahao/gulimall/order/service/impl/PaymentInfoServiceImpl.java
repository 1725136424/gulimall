package site.wanjiahao.gulimall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.Query;
import site.wanjiahao.gulimall.order.dao.PaymentInfoDao;
import site.wanjiahao.gulimall.order.entity.PaymentInfoEntity;
import site.wanjiahao.gulimall.order.service.PaymentInfoService;
import site.wanjiahao.gulimall.order.vo.AlipayAsyncNotifyVo;

import java.util.Map;


@Service("paymentInfoService")
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoDao, PaymentInfoEntity> implements PaymentInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PaymentInfoEntity> page = this.page(
                new Query<PaymentInfoEntity>().getPage(params),
                new QueryWrapper<PaymentInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void savePaymentByAlipayNotify(AlipayAsyncNotifyVo alipayAsyncNotifyVo) {
        PaymentInfoEntity paymentInfoEntity = new PaymentInfoEntity();
        paymentInfoEntity.setAlipayTradeNo(alipayAsyncNotifyVo.getTrade_no());
        paymentInfoEntity.setCallbackContent(alipayAsyncNotifyVo.getPassback_params());
        paymentInfoEntity.setCallbackTime(alipayAsyncNotifyVo.getNotify_time());
        paymentInfoEntity.setConfirmTime(alipayAsyncNotifyVo.getGmt_payment());
        paymentInfoEntity.setOrderSn(alipayAsyncNotifyVo.getOut_trade_no());
        paymentInfoEntity.setPaymentStatus(alipayAsyncNotifyVo.getTrade_status());
        paymentInfoEntity.setSubject(alipayAsyncNotifyVo.getSubject());
        paymentInfoEntity.setTotalAmount(alipayAsyncNotifyVo.getTotal_amount());
        paymentInfoEntity.setCreateTime(alipayAsyncNotifyVo.getGmt_create());
        baseMapper.insert(paymentInfoEntity);
    }

}