package site.wanjiahao.gulimall.coupon.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
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
@TableName("sms_seckill_session")
public class SeckillSessionEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
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
	 * 上架状态 0: 未上架 1: 上架失败 2: 已上架
	 */
	private Integer publishStatus;

	/**
	 * 非数据库字段
	 */
	@TableField(exist = false)
	private List<SeckillSkuRelationEntity>  seckillSkuRelationEntities;

}
