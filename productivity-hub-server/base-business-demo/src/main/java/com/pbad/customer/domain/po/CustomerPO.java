package com.pbad.customer.domain.po;

import common.core.domain.BaseDomain;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 客商持久类
 *
 * @author: pangdi
 * @date: 2023/8/29 10:47
 * @version: 1.0
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CustomerPO extends BaseDomain {

    /**
     * 自增id varchar(32)
     */
    private String customerId;

    /**
     * 姓名 varchar(10)
     */
    private String customerName;

    /**
     * 联系方式 char(11)
     */
    private String phone;

    /**
     * 地址 varchar(255)
     */
    private String address;

    /**
     * 备注 varchar(255)
     */
    private String remark;

    /**
     * 初始化更新参数.
     *
     * @param operateBy 操作人
     */
    public void initInsert(String operateBy) {
        this.setCreateBy(operateBy);
        this.setCreateTime(new Date());
        this.setUpdateBy(operateBy);
        this.setUpdateTime(new Date());
    }

    /**
     * 初始化更新参数.
     *
     * @param operateBy 操作人
     */
    public void initUpdate(String operateBy) {
        this.setUpdateBy(operateBy);
        this.setUpdateTime(new Date());
    }

}

