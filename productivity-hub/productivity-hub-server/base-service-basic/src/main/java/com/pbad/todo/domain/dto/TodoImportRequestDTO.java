package com.pbad.todo.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 待办批量导入请求 DTO.
 */
@Data
public class TodoImportRequestDTO {

    /**
     * 导入的任务列表。
     */
    private List<TodoImportItemDTO> items;
}

