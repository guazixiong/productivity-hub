package com.pbad.customer.helper;

import cn.hutool.core.collection.CollUtil;
import com.pbad.customer.constant.CustomerConstant;
import com.pbad.customer.domain.Customer;
import com.pbad.customer.domain.po.BankCardPO;
import com.pbad.customer.exception.ExceptionMsgEnum;
import com.pbad.customer.mapper.BankCardMapper;
import com.pbad.customer.predicate.PredicateCustomerService;
import common.util.judge.JudgeParameterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 银行卡助手类.
 *
 * @author: pangdi
 * @date: 2023/9/19 17:40
 * @version: 1.1
 */
@Component
public class BankCardHelper {

    private BankCardMapper bankCardMapper;
    private PredicateCustomerService predicateCustomerService;

    @Autowired
    public void initBankCardHelper(BankCardMapper bankCardMapper, PredicateCustomerService predicateCustomerService) {
        this.bankCardMapper = bankCardMapper;
        this.predicateCustomerService = predicateCustomerService;
    }

    /**
     * 获取银行卡信息.
     *
     * @param bankCard 银行卡持久类
     * @return 银行卡信息
     */
    public List<BankCardPO> getBankCardInfoList(BankCardPO bankCard) {
        List<BankCardPO> bankCardInfoList = bankCardMapper.selectByQuery(bankCard);
        return CollUtil.isEmpty(bankCardInfoList) ? new ArrayList<>() : bankCardInfoList;
    }

    /**
     * 批量新增客商银行卡信息.
     *
     * @param customer 客商银行信息业务类
     */
    public void batchInsertCustomerBankCard(Customer customer) {
        if (CollUtil.isEmpty(customer.getBankCardList())) {
            return;
        }
        String operateBy = customer.getOperateBy();
        String customerId = customer.getCustomerId();
        List<BankCardPO> bankCardList = customer.getBankCardList();
        bankCardList.forEach(e -> {
            e.initInsert(customerId, operateBy);
            e.setBindStat(CustomerConstant.BANK_CARD_HAVE_BIND);
        });
        bankCardMapper.batchInsert(bankCardList);
    }

    /**
     * 通过客商id删除银行信息.
     *
     * @param customerId 客商自增id
     */
    public void deleteByCustomerId(String customerId) {
        JudgeParameterUtil.checkNotNull(customerId, ExceptionMsgEnum.CUSTOMER_ID_IS_NULL.getErrorCode(), ExceptionMsgEnum.CUSTOMER_ID_IS_NULL.getErrorMessage());
        bankCardMapper.deleteByQuery(new BankCardPO().setCustomerId(customerId));
    }

    /**
     * 银行卡是否绑定.
     *
     * @param bankCardList 银行卡信息
     * @return true: 全绑定,false: 存在未绑定
     */
    public Boolean bankCardIsAllBind(List<BankCardPO> bankCardList) {
        JudgeParameterUtil.checkListNullOrEmptyThrowException(bankCardList, ExceptionMsgEnum.NOT_FOUND_BANK_CARD.getErrorCode(), ExceptionMsgEnum.NOT_FOUND_BANK_CARD.getErrorMessage());
        List<String> bankCardNoList = bankCardList.stream().map(BankCardPO::getBankCardNo).collect(Collectors.toList());
        List<BankCardPO> bankCardInfoList = getBankCardInfoList(new BankCardPO().setBankCardNoList(bankCardNoList));
        JudgeParameterUtil.checkListNullOrEmptyThrowException(bankCardInfoList, ExceptionMsgEnum.NOT_FOUND_BANK_CARD.getErrorCode(), ExceptionMsgEnum.NOT_FOUND_BANK_CARD.getErrorMessage());
        return bankCardInfoList.stream().anyMatch(predicateCustomerService::isBind);
    }
}
