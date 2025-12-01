package com.pbad.customer.mapper;

import com.pbad.customer.domain.po.BankCardPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 银行卡数据层接口.
 *
 * @author: pangdi
 * @date: 2023/8/30 16:00
 * @version: 1.0
 */
@Mapper
public interface BankCardMapper {

    /**
     * 通过银行卡id获取银行卡信息.
     *
     * @param bankCardNo 银行卡号
     * @return 银行卡信息
     */
    BankCardPO selectByCardNo(String bankCardNo);

    /**
     * 查询客商自增id查询银行卡信息.
     *
     * @param customerId 客商自增id
     * @return 银行卡信息集合
     */
    List<BankCardPO> selectByCustomerId(String customerId);

    /**
     * 查询银行卡信息.
     *
     * @param bankCardPO 银行卡持久类
     * @return 银行卡信息集合
     */
    List<BankCardPO> selectByQuery(BankCardPO bankCardPO);

    /**
     * 批量新增银行卡信息.
     *
     * @param bankCardList 银行卡信息
     */
    void batchInsert(List<BankCardPO> bankCardList);

    /**
     * 删除银行卡信息.
     *
     * @param bankCardPO 银行卡持久类
     */
    void deleteByQuery(BankCardPO bankCardPO);
}
