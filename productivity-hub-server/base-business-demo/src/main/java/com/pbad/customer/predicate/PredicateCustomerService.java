package com.pbad.customer.predicate;

import com.pbad.customer.domain.po.BankCardPO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 客商断言接口.
 *
 * @author: pangdi
 * @date: 2023/8/30 16:56
 * @version: 1.0
 */
public interface PredicateCustomerService {

    /**
     * 判断银行卡是否绑定.
     *
     * @param bankCard 银行卡信息
     * @return true: 已绑定, false: 未绑定
     */
    Boolean isBind(BankCardPO bankCard);

    /**
     * 获取绑定的银行卡.
     *
     * @param bankCardList 银行卡集合
     * @return 结果集
     */
    List<BankCardPO> getBindCardList(List<BankCardPO> bankCardList);

    /**
     * 获取满足金额区间的已绑定银行卡.
     *
     * @param bankCardList 银行卡集合
     * @param beginMoney 开始金额
     * @param endMoney 结束金额
     * @return 结果集
     */
    List<BankCardPO> getBindCardMoneyRangeList(List<BankCardPO> bankCardList, BigDecimal beginMoney,BigDecimal endMoney);
}
