package com.pbad.customer.domain;

import com.pbad.customer.domain.po.BankCardPO;
import common.core.domain.QueryDomain;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 客商业务类
 *
 * @author: pangdi
 * @date: 2023/8/29 10:47
 * @version: 1.0
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class Customer extends QueryDomain {

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
     * 银行卡信息
     */
    private List<BankCardPO> bankCardList;
}

