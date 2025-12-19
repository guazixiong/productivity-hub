-- Todo 模块表
CREATE TABLE IF NOT EXISTS todo_module (
    id              VARCHAR(64) PRIMARY KEY,
    user_id         VARCHAR(64) NOT NULL,
    name            VARCHAR(128) NOT NULL,
    description     TEXT,
    status          VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
    sort_order      INT NOT NULL DEFAULT 0,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uniq_user_name (user_id, name),
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Todo 任务表
CREATE TABLE IF NOT EXISTS todo_task (
    id                  VARCHAR(64) PRIMARY KEY,
    user_id             VARCHAR(64) NOT NULL,
    module_id           VARCHAR(64) NOT NULL,
    title               VARCHAR(255) NOT NULL,
    description         TEXT,
    priority            VARCHAR(10) NOT NULL DEFAULT 'P2',
    tags_json           TEXT,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    due_date            DATE NULL,
    started_at          DATETIME NULL,
    ended_at            DATETIME NULL,
    active_start_at     DATETIME NULL,
    pause_started_at    DATETIME NULL,
    duration_ms         BIGINT NOT NULL DEFAULT 0,
    paused_duration_ms  BIGINT NOT NULL DEFAULT 0,
    last_event_at       DATETIME NULL,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_user_status (user_id, status),
    KEY idx_user_module (user_id, module_id),
    CONSTRAINT fk_todo_task_module FOREIGN KEY (module_id) REFERENCES todo_module(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Todo 行为事件表
CREATE TABLE IF NOT EXISTS todo_event (
    id              VARCHAR(64) PRIMARY KEY,
    user_id         VARCHAR(64) NOT NULL,
    todo_id         VARCHAR(64) NOT NULL,
    event_type      VARCHAR(30) NOT NULL,
    occurred_at     DATETIME NOT NULL,
    payload         TEXT,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_todo (todo_id),
    KEY idx_user_date (user_id, occurred_at),
    CONSTRAINT fk_todo_event_task FOREIGN KEY (todo_id) REFERENCES todo_task(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

