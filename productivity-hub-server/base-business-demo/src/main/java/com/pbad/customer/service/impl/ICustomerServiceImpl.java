package com.pbad.customer.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pbad.customer.domain.Customer;
import com.pbad.customer.domain.bo.CustomerBankBO;
import com.pbad.customer.domain.po.CustomerPO;
import com.pbad.customer.exception.ExceptionMsgEnum;
import com.pbad.customer.helper.BankCardHelper;
import com.pbad.customer.helper.CustomerHelper;
import com.pbad.customer.service.ICustomerService;
import com.pbad.websocket.api.WebSocketApi;
import common.exception.BusinessException;
import common.util.SpringCopyUtil;
import common.util.judge.JudgeParameterUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 客商接口实现类.
 *
 * @author: pangdi
 * @date: 2023/8/29 10:49
 * @version: 1.1
 */
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class ICustomerServiceImpl implements ICustomerService {

    private final CustomerHelper customerHelper;
    private final BankCardHelper bankCardHelper;
    private final WebSocketApi webSocketApi;

    /**
     * 分页查询.
     *
     * @param customer 客商银行信息传输类
     * @return 分页信息
     */
    @Override
    @Transactional(readOnly = true)
    public PageInfo<CustomerPO> page(Customer customer) {
        CustomerPO po = SpringCopyUtil.convert(customer, CustomerPO.class);
        PageHelper.startPage(customer.getPageNum(), customer.getPageSize());
        return customerHelper.getCustomerPageList(po);
    }

    /**
     * 新增客商.
     *
     * @param customer 客商银行信息业务类
     */
    @Override
    public void add(Customer customer) {
        // 新增客商
        validInsertCustomerParameter(customer);
        String customerId = customerHelper.insertCustomer(customer);
        // 批量新增客商银行卡信息
        customer.setCustomerId(customerId);
        validInsertBankCardParameter(customer);
        bankCardHelper.batchInsertCustomerBankCard(customer);
        // 消息通知
        webSocketApi.sendMessageToUser(customer.getOperateBy(), "新增客商" + customer.getCustomerName());
    }

    /**
     * 新增客商参数校验.
     *
     * @param customer 客商银行信息业务类
     */
    private void validInsertCustomerParameter(Customer customer) {
        // TODO 自定义新增参数校验
    }

    /**
     * 新增银行卡参数校验.
     *
     * @param customer 客商银行信息业务类
     */
    private void validInsertBankCardParameter(Customer customer) {
        // 银行卡必须为未绑定
        Boolean isBindFlag = bankCardHelper.bankCardIsAllBind(customer.getBankCardList());
        if (Boolean.TRUE.equals(isBindFlag)) {
            throw new BusinessException(ExceptionMsgEnum.BANK_CARD_HAVE_BIND.getErrorCode(), ExceptionMsgEnum.BANK_CARD_HAVE_BIND.getErrorMessage());
        }
    }

    /**
     * 修改客商.
     *
     * @param customer 客商银行信息业务类
     */
    @Override
    public void update(Customer customer) {
        JudgeParameterUtil.checkNotNull(customer.getCustomerId(), ExceptionMsgEnum.CUSTOMER_ID_IS_NULL.getErrorCode(), ExceptionMsgEnum.CUSTOMER_ID_IS_NULL.getErrorMessage());
        JudgeParameterUtil.checkNotNull(customer.getOperateBy(), ExceptionMsgEnum.OPERATE_IS_NULL.getErrorCode(), ExceptionMsgEnum.OPERATE_IS_NULL.getErrorMessage());
        CustomerPO originCustomerInfo = customerHelper.getCustomerByCustomerId(customer.getCustomerId());
        BeanUtils.copyProperties(customer, originCustomerInfo);
        // 更新客商信息
        customerHelper.updateCustomerInfo(originCustomerInfo, customer.getOperateBy());
        // 移除客商关联银行卡信息
        bankCardHelper.deleteByCustomerId(customer.getCustomerId());
        // 更新客商关联银行卡信息
        bankCardHelper.batchInsertCustomerBankCard(customer);
        // 消息通知
        webSocketApi.sendMessageToUser(customer.getOperateBy(), "修改客商" + customer.getCustomerId());
    }

    /**
     * 客商详情.
     *
     * @param customer 客商银行信息传输类
     * @return 客商银行信息VO类
     */
    @Override
    @Transactional(readOnly = true)
    public CustomerBankBO getInfo(Customer customer) {
        CustomerBankBO customerBankBO = SpringCopyUtil.convert(customer, CustomerBankBO.class);
        List<CustomerBankBO> customerBankBOList = customerHelper.selectInfoByQuery(customerBankBO);
        return CollUtil.isEmpty(customerBankBOList) ? new CustomerBankBO() : customerBankBOList.get(0);
    }

    /**
     * 删除客商.
     *
     * @param customerId 客商编号
     * @param isValid    是否校验
     */
    @Override
    public void deleteValidById(String customerId, boolean isValid) {
        JudgeParameterUtil.checkNotNull(customerId,
                ExceptionMsgEnum.CUSTOMER_ID_IS_NULL.getErrorCode(),
                ExceptionMsgEnum.CUSTOMER_ID_IS_NULL.getErrorMessage());
        if (isValid) {
            // TODO 自定义校验
        }
        // 删除客商关联银行卡信息
        bankCardHelper.deleteByCustomerId(customerId);
        // 删除客商信息
        customerHelper.deleteByCustomerId(customerId);
        // 消息通知
        webSocketApi.sendMessageToAll("删除客商" + customerId);
    }
}
