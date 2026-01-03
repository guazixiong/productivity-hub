package com.pbad.health.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 运动记录导入结果视图对象.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class ExerciseRecordImportResultVO {

    /**
     * 总记录数
     */
    private Integer total;

    /**
     * 成功数
     */
    private Integer success;

    /**
     * 失败数
     */
    private Integer failed;

    /**
     * 跳过数
     */
    private Integer skipped;

    /**
     * 错误信息列表
     */
    private List<String> errors;
}


