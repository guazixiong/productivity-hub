package com.pbad.customer.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pbad.customer.domain.po.BankCardPO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * 客商银行信息VO类.
 *
 * @author: pangdi
 * @date: 2023/8/29 10:48
 * @version: 1.0
 */
@Data
@ApiModel("客商银行信息VO类")
@NoArgsConstructor
@AllArgsConstructor
public class CustomerBankInfoVO {

    /**
     * 客商唯一标识 bigint(20)
     */
    @ApiModelProperty(value = "客商唯一标识")
    private String customerId;

    /**
     * 客商姓名 varchar(50)
     */
    @ApiModelProperty(value = "客商姓名", example = "pangdi")
    private String customerName;

    /**
     * 客商联系方式 char(20)
     */
    @ApiModelProperty(value = "客商联系方式", example = "13140109161")
    private String phone;

    /**
     * 客商地址 varchar(255)
     */
    @ApiModelProperty(value = "客商地址", example = "种花家")
    private String address;

    /**
     * 备注 varchar(255)
     */
    @ApiModelProperty(value = "备注", example = "这是一个备注")
    private String remark;

    /**
     * 关联银行卡信息
     */
    @ApiModelProperty(value = "关联银行卡信息")
    private List<BankCardPO> bankCardList;

    /**
     * 创建人 varchar(50)
     */
    @ApiModelProperty(value = "创建人", example = "pangdi")
    private String createBy;

    /**
     * 创建时间 datetime
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间", example = "2023-9-7 16:33:39")
    private Date createTime;

    /**
     * 修改人 varchar(50)
     */
    @ApiModelProperty(value = "修改人", example = "pangdi")
    private String updateBy;

    /**
     * 修改时间 datetime
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "修改时间", example = "2023-9-7 16:33:39")
    private Date updateTime;
}
