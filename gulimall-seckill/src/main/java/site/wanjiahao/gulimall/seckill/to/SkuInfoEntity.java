package site.wanjiahao.gulimall.seckill.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * sku信息
 * 
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 16:18:27
 */
@Data
public class SkuInfoEntity {

	/**
	 * skuId
	 */
	private Long skuId;
	/**
	 * spuId
	 */
	private Long spuId;
	/**
	 * sku名称
	 */
	private String skuName;
	/**
	 * sku介绍描述
	 */
	private String skuDesc;
	/**
	 * 所属分类id
	 */
	private Long catelogId;
	/**
	 * 品牌id
	 */
	private Long brandId;
	/**
	 * 默认图片
	 */
	private String skuDefaultImg;
	/**
	 * 标题
	 */
	private String skuTitle;
	/**
	 * 副标题
	 */
	private String skuSubtitle;
	/**
	 * 价格
	 */
	private BigDecimal price;
	/**
	 * 销量
	 */
	private Long saleCount;

}
