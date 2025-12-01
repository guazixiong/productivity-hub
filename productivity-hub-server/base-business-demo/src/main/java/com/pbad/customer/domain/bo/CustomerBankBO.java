package com.pbad.customer.domain.bo;

import com.pbad.customer.domain.po.BankCardPO;
import common.core.domain.BaseDomain;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 客商银行信息业务类.
 *
 * @author: pangdi
 * @date: 2023/8/29 10:48
 * @version: 1.0
 */
@Data
@Accessors(chain=true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CustomerBankBO extends BaseDomain {
    /**
     * 客商自增id bigint(20)
     */
    private String customerId;

    /**
     * 客商姓名 varchar(50)
     */
    private String customerName;

    /**
     * 客商联系方式 char(20)
     */
    private String phone;

    /**
     * 客商地址 varchar(255)
     */
    private String address;

    /**
     * 备注 varchar(255)
     */
    private String remark;

    /**
     * 关联银行卡信息
     */
    private List<BankCardPO> bankCardList;
}
