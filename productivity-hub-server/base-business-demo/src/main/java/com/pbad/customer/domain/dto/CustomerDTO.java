package com.pbad.customer.domain.dto;

import com.pbad.customer.domain.po.BankCardPO;
import common.core.domain.dto.QueryDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 客商银行信息传输类.
 *
 * @author: pangdi
 * @date: 2023/8/29 10:48
 * @version: 1.0
 */
@Data
@Accessors(chain=true)
@ApiModel("客商银行信息传输类")
public class CustomerDTO extends QueryDTO {

    /**
     * 客商唯一标识 bigint(20)
     */
    @ApiModelProperty(value = "客商唯一标识")
    private String customerId;

    /**
     * 客商姓名 varchar(50)
     */
    @ApiModelProperty(value = "客商姓名")
    @NotNull(message = "客商姓名不能为空")
    @Size(min = 2,max = 10,message = "客商姓名长度必须是2-10个字符")
    private String customerName;

    /**
     * 客商联系方式 char(20)
     */
    @ApiModelProperty(value = "客商联系方式")
    @NotNull(message = "客商联系方式不能为空")
    private String phone;

    /**
     * 客商地址 varchar(255)
     */
    @ApiModelProperty(value = "客商地址")
    @NotNull(message = "客商地址不能为空")
    private String address;

    /**
     * 备注 varchar(255)
     */
    @ApiModelProperty(value = "备注")
    @Size(max = 255,message = "备注长度必须是0-255个字符")
    private String remark;

    /**
     * 银行卡信息
     */
    @ApiModelProperty(value = "银行卡信息")
    private List<BankCardPO> bankCardList;

    /**
     * 操作人 varchar(50)
     */
    @ApiModelProperty(value = "操作人")
    @NotNull(message = "操作人式不能为空")
    private String operateBy;
}
