package com.pbad.customer.service;

import com.github.pagehelper.PageInfo;
import com.pbad.customer.domain.Customer;
import com.pbad.customer.domain.bo.CustomerBankBO;
import com.pbad.customer.domain.dto.CustomerDTO;
import com.pbad.customer.domain.po.CustomerPO;

/**
 * 客商接口.
 *
 * @author: pangdi
 * @date: 2023/8/29 10:49
 * @version:1.1
 */
public interface ICustomerService {

    /**
     * 分页查询.
     *
     * @param customer 客商银行信息业务类
     * @return 分页信息
     */
    PageInfo<CustomerPO> page(Customer customer);

    /**
     * 新增客商.
     *
     * @param customer 客商银行信息业务类
     */
    void add(Customer customer);

    /**
     * 修改客商.
     *
     * @param customer 客商银行信息业务类
     */
    void update(Customer customer);

    /**
     * 客商详情.
     *
     * @param customer 客商银行信息业务类
     * @return 客商银行信息VO类
     */
    CustomerBankBO getInfo(Customer customer);

    /**
     * 删除客商.
     *
     * @param customerId 客商编号
     * @param isValid    是否校验
     */
    void deleteValidById(String customerId, boolean isValid);
}
