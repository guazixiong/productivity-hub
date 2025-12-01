package com.pbad.customer.domain.po;

import common.core.domain.BaseDomain;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 银行卡持久类.
 *
 * @author: pangdi
 * @date: 2023/8/29 10:47
 * @version: 1.0
 */
@Data
@Accessors(chain=true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BankCardPO extends BaseDomain {

    /**
     * 自增id bigint(20)
     */
    private Long bankCardId;

    /**
     * 银行卡号 varchar(20)
     */
    private String bankCardNo;

    /**
     * 银行卡名称 varchar(20)
     */
    private String bankCardName;

    /**
     * 银行地址 varchar(255)
     */
    private String address;

    /**
     * 存储金额 decimal(12,2)
     */
    private BigDecimal money;

    /**
     * 绑定状态 0: 未绑定, 1: 已绑定 char(1)
     */
    private String bindStat;

    /**
     * 关联客商自增id varchar(32)
     */
    private String customerId;

    /**
     * 查询条件: 银行卡号集合
     */
    private List<String> bankCardNoList;

    /**
     * 初始化新增参数.
     *
     * @param customerId 客商自增id
     * @param operateBy 操作人
     */
    public void initInsert(String customerId, String operateBy) {
        this.customerId = customerId;
        this.setCreateBy(operateBy);
        this.setCreateTime(new Date());
        this.setUpdateBy(operateBy);
        this.setUpdateTime(new Date());
    }
}
