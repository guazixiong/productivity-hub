package com.pbad.todo.domain.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 待办批量导入结果.
 */
@Data
public class TodoImportResultVO {
    /**
     * 总记录数。
     */
    private int total;

    /**
     * 成功条数。
     */
    private int success;

    /**
     * 失败条数。
     */
    private int failed;

    /**
     * 本次新建的模块数量。
     */
    private int createdModules;

    /**
     * 失败原因列表。
     */
    private List<String> errors = new ArrayList<>();
}

