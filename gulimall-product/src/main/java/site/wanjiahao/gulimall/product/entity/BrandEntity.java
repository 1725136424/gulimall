package site.wanjiahao.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import lombok.Data;
import org.hibernate.validator.constraints.URL;
import site.wanjiahao.common.valid.ListVal;
import site.wanjiahao.common.valid.SaveGroup;
import site.wanjiahao.common.valid.UpdateGroup;

import javax.validation.constraints.*;

/**
 * 品牌
 * 
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 16:18:27
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@NotNull(message = "修改必须提交id", groups = UpdateGroup.class)
	@Null(message = "增加id必须为空", groups = SaveGroup.class)
	@TableId
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotBlank(message = "名称不能为空", groups = {SaveGroup.class})
	private String name;
	/**
	 * 品牌logo地址
	 */
	@NotBlank(message = "URL不能为空", groups = {SaveGroup.class})
	@URL(message = "提交的必须是一个URL地址", groups = {UpdateGroup.class, SaveGroup.class})
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	@NotNull(message = "状态值不能为空")
	@ListVal(value = {0, 1}, message = "状态值只能为1或者0", groups = {UpdateGroup.class, SaveGroup.class})
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@NotBlank(message = "首字母不能为空", groups = SaveGroup.class)
	@Pattern(regexp = "^[a-zA-Z]$", message = "检索的必须是一个字母", groups = {SaveGroup.class, UpdateGroup.class})
	private String firstLetter;
	/**
	 * 排序
	 */
	@NotNull(message = "首字母不能为空", groups = SaveGroup.class)
	@Min(value = 0, message = "排序字段必须大于0", groups = {SaveGroup.class, UpdateGroup.class})
	private Integer sort;

}
