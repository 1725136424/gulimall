package site.wanjiahao.gulimall.product.vo;

import lombok.Data;
import site.wanjiahao.gulimall.product.entity.SkuInfoEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 秒杀活动商品关联
 * 
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-15 21:10:54
 */
@Data
public class SeckillSkuRelationEntity {

	/**
	 * id
	 */
	private Long id;
	/**
	 * 活动id
	 */
	private Long promotionId;
	/**
	 * 活动场次id
	 */
	private Long promotionSessionId;
	/**
	 * 商品id
	 */
	private Long skuId;
	/**
	 * 秒杀价格
	 */
	private BigDecimal seckillPrice;
	/**
	 * 秒杀总量
	 */
	private BigDecimal seckillCount;
	/**
	 * 每人限购数量
	 */
	private BigDecimal seckillLimit;
	/**
	 * 排序
	 */
	private Integer seckillSort;

	/**
	 * 设置商品随机码
	 */
	private String randomCode;

	/**
	 * 开始
	 */
	private Date startTime;

	/**
	 * 结束时间
	 */
	private Date endTime;

	/**
	 * 商品的详细信息
	 */
	private SkuInfoEntity skuInfoEntity;

}
