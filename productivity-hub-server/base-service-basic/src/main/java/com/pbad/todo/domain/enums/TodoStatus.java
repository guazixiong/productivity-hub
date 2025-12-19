package com.pbad.todo.domain.enums;

/**
 * Todo 任务状态.
 */
public enum TodoStatus {
    PENDING,
    IN_PROGRESS,
    PAUSED,
    COMPLETED,
    INTERRUPTED;

    /**
     * 是否为终态（不可继续计时）
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == INTERRUPTED;
    }

    /**
     * 是否允许开始/恢复
     */
    public boolean canStart() {
        return this == PENDING || this == PAUSED || this == INTERRUPTED;
    }
}

