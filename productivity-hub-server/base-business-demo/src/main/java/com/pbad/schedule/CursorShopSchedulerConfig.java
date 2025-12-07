package com.pbad.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Cursor 邮箱自助小店定时任务配置.
 *
 * <p>固定调度规则：工作日 09:00~18:00 整点推送，11:00~15:00 及双休日不推送。</p>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CursorShopSchedulerConfig implements SchedulingConfigurer {

    private final CursorShopTask cursorShopTask;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(cursorShopTask::execute, new FixedWindowTrigger());
    }

    /**
     * 自定义触发器：按业务规则计算下一次执行时间.
     */
    private class FixedWindowTrigger implements Trigger {

        @Override
        public java.util.Date nextExecutionTime(TriggerContext triggerContext) {
            LocalDateTime base = resolveBase(triggerContext);
            LocalDateTime next = cursorShopTask.nextAllowedExecution(base);
            return java.util.Date.from(next.atZone(ZoneId.systemDefault()).toInstant());
        }

        private LocalDateTime resolveBase(TriggerContext triggerContext) {
            if (triggerContext.lastCompletionTime() != null) {
                Instant instant = triggerContext.lastCompletionTime().toInstant();
                return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            }
            return LocalDateTime.now();
        }
    }
}

