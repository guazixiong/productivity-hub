package com.pbad.generator.domain.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * id生成信息.
 *
 * @author: pangdi
 * @date: 2023/9/8 11:29
 * @version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain=true)
public class IdGeneratorInfoPO {

    /**
     * 模块唯一标识 varchar(20)
     */
    private String moduleKey;

    /**
     * 模块名称 varchar(50)
     */
    private String moduleName;

    /**
     * 工作ID bigint(32)
     */
    private long workerId;

    /**
     * 数据中心ID bigint(32)
     */
    private long datacenterId;

    /**
     * 状态 StatusEnum.class
     */
    private String status;

    /**
     * 备注 varchar(255)
     */
    private String remark;
}
