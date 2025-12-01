package com.pbad.customer.constant;

/**
 * 客商常量类.
 *
 * @author: pangdi
 * @date: 2023/8/29 10:46
 * @version: 1.0
 */
public interface CustomerConstant {

    /**
     * 模块key
     */
    String MODULE_KEY = "demo";

    /**
     * 默认工作ID
     */
    long DEFAULT_WORKER_ID = 0;

    /**
     * 默认数据中心ID
     */
    long DEFAULT_DATA_CENTER_ID = 0;

    /**
     * 卡绑定状态: 未绑定
     */
    String BANK_CARD_NOT_BIND = "0";

    /**
     * 卡绑定状态: 已绑定
     */
    String BANK_CARD_HAVE_BIND = "1";
}
