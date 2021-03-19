package site.wanjiahao.gulimall.seckill.to;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 秒杀活动场次
 * 
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 15:55:38
 */
@Data
public class SeckillSessionEntity {

	/**
	 * id
	 */
	private Long id;
	/**
	 * 场次名称
	 */
	private String name;
	/**
	 * 每日开始时间
	 */
	private Date startTime;
	/**
	 * 每日结束时间
	 */
	private Date endTime;
	/**
	 * 启用状态
	 */
	private Integer status;
	/**
	 * 创建时间
	 */
	private Date createTime;

    /**
	 * 非数据库字段
	 */
	private List<SeckillSkuRelationEntity>  seckillSkuRelationEntities;

}
