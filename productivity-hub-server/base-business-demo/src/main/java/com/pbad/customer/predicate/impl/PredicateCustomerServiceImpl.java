package com.pbad.customer.predicate.impl;

import cn.hutool.core.collection.CollUtil;
import com.pbad.customer.constant.CustomerConstant;
import com.pbad.customer.domain.po.BankCardPO;
import com.pbad.customer.predicate.PredicateCustomerService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 客商断言接口实现类.
 *
 * @author: pangdi
 * @date: 2023/8/30 16:57
 * @version: 1.0
 */
@Service
public class PredicateCustomerServiceImpl implements PredicateCustomerService {

    /**
     * 判断银行卡是否绑定.
     *
     * @param bankCard 银行卡信息
     * @return true: 已绑定, false: 未绑定
     */
    @Override
    public Boolean isBind(BankCardPO bankCard) {
        return bankCard == null ? Boolean.FALSE : CustomerConstant.BANK_CARD_HAVE_BIND.equals(bankCard.getBindStat());
    }

    /**
     * 获取绑定的银行卡.
     *
     * @param bankCardList 银行卡集合
     * @return 符合要求的结果集
     */
    @Override
    public List<BankCardPO> getBindCardList(List<BankCardPO> bankCardList) {
        if (CollUtil.isEmpty(bankCardList)) {
            return new ArrayList<>();
        }
        return bankCardList.stream()
                .filter(this::isBind)
                .collect(Collectors.toList());
    }

    /**
     * 判断银行卡是否在指定区间.
     *
     * @param money      卡金额
     * @param beginMoney 开始金额
     * @param endMoney   结束金额
     * @return true: 存在, false: 不存在
     */
    private Boolean isCardInRange(BigDecimal money, BigDecimal beginMoney, BigDecimal endMoney) {
        return money.compareTo(beginMoney) >= 0 && money.compareTo(endMoney) <= 0;
    }

    /**
     * 判断银行卡是否有效且在指定区间.
     *
     * @param bankCard   银行卡对象
     * @param beginMoney 开始金额
     * @param endMoney   结束金额
     * @return true: 存在, false: 不存在
     */
    private Boolean isBindAndInRange(BankCardPO bankCard, BigDecimal beginMoney, BigDecimal endMoney) {
        return isBind(bankCard) && isCardInRange(bankCard.getMoney(), beginMoney, endMoney);
    }

    /**
     * 获取满足金额区间的已绑定银行卡.
     *
     * @param bankCardList 银行卡集合
     * @param beginMoney   开始金额
     * @param endMoney     结束金额
     * @return 符合要求的结果集
     */
    @Override
    public List<BankCardPO> getBindCardMoneyRangeList(List<BankCardPO> bankCardList, BigDecimal beginMoney, BigDecimal endMoney) {
        if (CollUtil.isEmpty(bankCardList)) {
            return new ArrayList<>();
        }
        return bankCardList.stream()
                .filter(this::isBind)
                .filter(e -> isBindAndInRange(e, beginMoney, endMoney))
                .collect(Collectors.toList());
    }
}
