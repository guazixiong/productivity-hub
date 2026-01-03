package com.pbad.todo.domain.enums;

/**
 * Todo 优先级.
 */
public enum TodoPriority {
    P0,
    P1,
    P2,
    P3;

    /**
     * 默认优先级
     */
    public static TodoPriority defaultPriority() {
        return P2;
    }
}

