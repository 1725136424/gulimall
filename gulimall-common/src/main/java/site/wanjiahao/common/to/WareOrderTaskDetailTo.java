package site.wanjiahao.common.to;

import lombok.Data;

import java.io.Serializable;

/**
 * 库存工作单
 * 
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 16:04:38
 */
@Data
public class WareOrderTaskDetailTo implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	private Long id;
	/**
	 * sku_id
	 */
	private Long skuId;
	/**
	 * sku_name
	 */
	private String skuName;
	/**
	 * 购买个数
	 */
	private Integer skuNum;
	/**
	 * 工作单id
	 */
	private Long taskId;
	/**
	 * 仓库id
	 */
	private Long wareId;
	/**
	 * 1-已锁定  2-已解锁  3-扣减
	 */
	private Integer lockStatus;

}
