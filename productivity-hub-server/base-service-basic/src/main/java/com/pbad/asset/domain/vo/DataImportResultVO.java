package com.pbad.asset.domain.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据导入结果视图对象（VO）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class DataImportResultVO {
    /**
     * 成功数量
     */
    private Integer successCount;

    /**
     * 失败数量
     */
    private Integer failCount;

    /**
     * 错误信息列表
     */
    private List<ImportError> errors;

    /**
     * 导入错误信息
     */
    @Data
    public static class ImportError {
        /**
         * 行号（从1开始）
         */
        private Integer row;

        /**
         * 错误消息
         */
        private String message;
    }

    /**
     * 创建成功结果
     */
    public static DataImportResultVO success(int successCount) {
        DataImportResultVO vo = new DataImportResultVO();
        vo.setSuccessCount(successCount);
        vo.setFailCount(0);
        vo.setErrors(new ArrayList<>());
        return vo;
    }

    /**
     * 创建失败结果
     */
    public static DataImportResultVO fail(List<ImportError> errors) {
        DataImportResultVO vo = new DataImportResultVO();
        vo.setSuccessCount(0);
        vo.setFailCount(errors != null ? errors.size() : 0);
        vo.setErrors(errors != null ? errors : new ArrayList<>());
        return vo;
    }

    /**
     * 创建失败结果（单个错误消息）
     */
    public static DataImportResultVO fail(String errorMessage) {
        DataImportResultVO vo = new DataImportResultVO();
        vo.setSuccessCount(0);
        vo.setFailCount(1);
        List<ImportError> errors = new ArrayList<>();
        ImportError error = new ImportError();
        error.setRow(0);
        error.setMessage(errorMessage);
        errors.add(error);
        vo.setErrors(errors);
        return vo;
    }
}

