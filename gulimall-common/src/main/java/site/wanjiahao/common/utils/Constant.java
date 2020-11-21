/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package site.wanjiahao.common.utils;

/**
 * 常量
 *
 * @author Mark sunlightcs@gmail.com
 */
public class Constant {
    /** 超级管理员ID */
    public static final int SUPER_ADMIN = 1;
    /**
     * 当前页码
     */
    public static final String PAGE = "page";
    /**
     * 每页显示记录数
     */
    public static final String LIMIT = "limit";
    /**
     * 排序字段
     */
    public static final String ORDER_FIELD = "sidx";
    /**
     * 排序方式
     */
    public static final String ORDER = "order";
    /**
     *  升序
     */
    public static final String ASC = "asc";

    /**
     * 价格区间聚合interval
     */
    public static final Double interval = 1000.0;

    /**
     * 检索服务器地址
     */
    public static final String searchServer = "http://search.gulimall.com";

    /**
     * redis验证码前缀
     */
    public static final String CODE_PREFIX = "sms";

    /**
     * 菜单类型
     *
     * @author chenshun
     * @email sunlightcs@gmail.com
     * @date 2016年11月15日 下午1:24:29
     */
    public enum MenuType {
        /**
         * 目录
         */
        CATALOG(0),
        /**
         * 菜单
         */
        MENU(1),
        /**
         * 按钮
         */
        BUTTON(2);

        private int value;

        MenuType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * 定时任务状态
     *
     * @author chenshun
     * @email sunlightcs@gmail.com
     * @date 2016年12月3日 上午12:07:22
     */
    public enum ScheduleStatus {
        /**
         * 正常
         */
        NORMAL(0),
        /**
         * 暂停
         */
        PAUSE(1);

        private int value;

        ScheduleStatus(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * 云服务商
     */
    public enum CloudService {
        /**
         * 七牛云
         */
        QINIU(1),
        /**
         * 阿里云
         */
        ALIYUN(2),
        /**
         * 腾讯云
         */
        QCLOUD(3);

        private int value;

        CloudService(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * 属性类型枚举
     */
    public enum AttrType {
        BASE_ATTR(0, "基本属性"),
        SALE_ATTR(1, "销售属性"),
        OTHER_ATTR(2, "其他属性");

        private final Integer code;

        private final String message;

        AttrType(Integer code, String message) {
            this.code = code;
            this.message = message;
        }

        public Integer getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * 采购单枚举类型
     */
    public enum PurchaseStatus {

        NEW(0, "新建"),
        ASSIGN(1, "已分配"),
        RECEIVE(2, "已领取"),
        COMPLETE(3, "已完成"),
        FAIL(4, "采购失败");

        private final int status;

        private final String message;

        PurchaseStatus(int status, String message) {
            this.status = status;
            this.message = message;
        }

        public int getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * ES索引库
     */
    public enum ESIndex {

        PRODUCT_INDEX("gulimall_product");

        private final String index;

        ESIndex(String index) {
            this.index = index;
        }

        public String getIndex() {
            return index;
        }
    }

    /**
     * 分类层级枚举
     */
    public enum CategoryLevel {
        ONE(1), TWO(2), THREE(3);

        private final Integer level;

        CategoryLevel(Integer level) {
            this.level = level;
        }

        public Integer getLevel() {
            return level;
        }
    }

    /**
     * ES聚合名称
     */
    public enum ESAggregation {
        // 品牌聚合枚举
        BRAND_AGG("brand_agg"),
        BRAND_NAME_AGG("brand_name_agg"),
        BRAND_IMG_AGG("brand_img_agg"),
        // 分类聚合枚举
        CATEGORY_AGG("category_agg"),
        CATEGORY_NAME_AGG("category_name_agg"),
        // 属性枚举
        ATTR_AGG("attr_agg"),
        ATTR_ID_AGG("attr_id_agg"),
        ATTR_NAME_AGG("attr_name_agg"),
        ATTR_VALUE_AGG("attr_value_agg"),
        // 价格区间枚举
        PRICE_RANGE("price_range");

        private final String value;

        ESAggregation(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

}
