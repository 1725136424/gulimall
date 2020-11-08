package site.wanjiahao.gulimall.search.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 构造页面发送过来的请求参数
 */
@Data
public class RequestParamsVo implements Serializable {

    /**
     * 检索
     */
    private String keyword;

    /**
     * 当前页码
     */
    private Integer pageNum = 1;

    /**
     * 每一页记录数
     */
    private Integer pageSize = 20;

    /**
     * 三级分类Id
     */
    private Long catalog3Id;

    /**
     * 品牌Id 可以多选品牌
     * brandIds=1&brandIds=2&brandIds=3...
     */
    private List<Long> brandIds;

    /**
     * 价格区间
     * price=100_1000  100-1000
     * price=_1000     <= 1000
     * price=100_      >= 100
     */
    private String price;

    /**
     * 属性
     * attrs=1_5寸:6寸  attrs=[属性id]_[属性值1]:[属性值2]
     */
    private List<String> attrs;

    /**
     * 排序字段
     * sort=saleCount_asc/desc
     * sort=skuPrice_acs/desc
     * sort=hotScore_asc/desc
     */
    private String sort;

    /**
     * 是否显示有货
     */
    private Boolean hasStock;

    /**
     * 请求参数元数据
     */
    private String queryParams;

}
