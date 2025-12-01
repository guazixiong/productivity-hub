package com.pbad.customer.mapper;

import com.pbad.customer.domain.bo.CustomerBankBO;
import com.pbad.customer.domain.po.CustomerPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 客商数据层接口.
 *
 * @author: pangdi
 * @date: 2023/8/29 15:48
 * @description: 1.0
 */
@Mapper
public interface CustomerMapper {

    /**
     * 新增.
     *
     * @param customerPO 客商持久类
     * @return 结果集
     */
    int insert(CustomerPO customerPO);

    /**
     * 修改.
     *
     * @param customerPO 客商持久类
     * @return 结果集
     */
    int update(CustomerPO customerPO);

    /**
     * 查询客商信息.
     *
     * @param customerId 客商自增id
     * @return 客商持久类
     */
    CustomerPO selectBaseInfoByCustomerId(String customerId);

    /**
     * 查询客商信息.
     *
     * @param customerPO 客商持久类
     * @return 结果集
     */
    List<CustomerPO> selectBaseInfoByQuery(CustomerPO customerPO);

    /**
     * 查询客商信息.
     *
     * @param customerBankBO 客商银行信息业务类
     * @return 结果集
     */
    List<CustomerBankBO> selectInfoByQuery(CustomerBankBO customerBankBO);

    /**
     * 删除.
     *
     * @param customerId 客商自增id
     */
    void delete(String customerId);
}
