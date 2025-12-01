package com.pbad.customer.helper;

import cn.hutool.core.collection.CollUtil;
import com.github.pagehelper.PageInfo;
import com.pbad.customer.constant.CustomerConstant;
import com.pbad.customer.domain.Customer;
import com.pbad.customer.domain.bo.CustomerBankBO;
import com.pbad.customer.domain.po.CustomerPO;
import com.pbad.customer.exception.ExceptionMsgEnum;
import com.pbad.customer.mapper.CustomerMapper;
import com.pbad.generator.api.IdGeneratorApi;
import common.exception.BusinessException;
import common.util.SpringCopyUtil;
import common.util.judge.JudgeParameterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 客商助手类.
 *
 * @author: pangdi
 * @date: 2023/9/19 17:35
 * @version: 1.1
 */
@Component
public class CustomerHelper {

    private IdGeneratorApi idGeneratorApi;
    private CustomerMapper customerMapper;

    @Autowired
    public void initCustomerHelper(IdGeneratorApi idGeneratorApi, CustomerMapper customerMapper) {
        this.idGeneratorApi = idGeneratorApi;
        this.customerMapper = customerMapper;
    }

    /**
     * 通过客商编号,获取客商信息.
     *
     * @param customerId 客商自增id
     * @return 客商银行信息
     */
    public CustomerPO getCustomerByCustomerId(String customerId) {
        CustomerPO customer = customerMapper.selectBaseInfoByCustomerId(customerId);
        if (customer == null) {
            throw new BusinessException(ExceptionMsgEnum.NOT_FOUND_CUSTOMER_INFO.getErrorCode(), ExceptionMsgEnum.NOT_FOUND_CUSTOMER_INFO.getErrorMessage());
        }
        return customer;
    }

    /**
     * 更新客商信息.
     *
     * @param customerInfo 客商信息
     * @param operateBy    操作人
     */
    public Boolean updateCustomerInfo(CustomerPO customerInfo, String operateBy) {
        customerInfo.initUpdate(operateBy);
        return customerMapper.update(customerInfo) > 0;
    }

    /**
     * 分页查询客商信息.
     *
     * @param customer 查询参数
     * @return 分页信息
     */
    public PageInfo<CustomerPO> getCustomerPageList(CustomerPO customer) {
        List<CustomerPO> pageList = customerMapper.selectBaseInfoByQuery(customer);
        return new PageInfo<>(pageList);
    }

    /**
     * 新增客商.
     *
     * @param customer 客商银行信息业务类
     * @return 客商id
     */
    public String insertCustomer(Customer customer) {
        CustomerPO customerPO = SpringCopyUtil.convert(customer, CustomerPO.class);
        customerPO.initInsert(customer.getOperateBy());
        String generatorId = idGeneratorApi.generatorId(CustomerConstant.DEFAULT_WORKER_ID, CustomerConstant.DEFAULT_DATA_CENTER_ID);
        customerPO.setCustomerId(generatorId);
        customerMapper.insert(customerPO);
        return customerPO.getCustomerId();
    }

    /**
     * 查询客商信息.
     *
     * @param customerBankBO 客商银行信息业务类
     * @return 结果集
     */
    public List<CustomerBankBO> selectInfoByQuery(CustomerBankBO customerBankBO) {
        List<CustomerBankBO> customerBankBOList = customerMapper.selectInfoByQuery(customerBankBO);
        return CollUtil.isEmpty(customerBankBOList) ? new ArrayList<>() : customerBankBOList;
    }

    /**
     * 删除客商.
     *
     * @param customerId 客商编号
     */
    public void deleteByCustomerId(String customerId) {
        JudgeParameterUtil.checkNotNull(customerId, ExceptionMsgEnum.CUSTOMER_ID_IS_NULL.getErrorCode(), ExceptionMsgEnum.CUSTOMER_ID_IS_NULL.getErrorMessage());
        customerMapper.delete(customerId);
    }
}
