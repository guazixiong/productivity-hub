package com.pbad.asset.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 汇率视图对象（VO）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class ExchangeRateVO {
    /**
     * 源货币代码
     */
    private String from;

    /**
     * 目标货币代码
     */
    private String to;

    /**
     * 汇率
     */
    private Double rate;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}

