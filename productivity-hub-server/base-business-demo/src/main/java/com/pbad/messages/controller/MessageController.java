package com.pbad.messages.controller;

import com.pbad.messages.domain.dto.MessageSendDTO;
import com.pbad.messages.domain.vo.MessageHistoryVO;
import com.pbad.messages.domain.vo.MessageSendResponseVO;
import com.pbad.messages.service.MessageService;
import common.core.domain.ApiResponse;
import common.core.domain.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 消息推送控制器.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 发送消息
     *
     * @param sendDTO 发送请求
     * @return 发送响应
     */
    @PostMapping("/send")
    public ApiResponse<MessageSendResponseVO> sendMessage(@RequestBody MessageSendDTO sendDTO) {
        MessageSendResponseVO response = messageService.sendMessage(sendDTO);
        if (!"success".equalsIgnoreCase(response.getStatus())) {
            String detail = response.getDetail();
            String message = isBlank(detail) ? "推送失败" : detail;
            if (!isBlank(response.getRequestId())) {
                message = String.format("%s（请求ID: %s）", message, response.getRequestId());
            }
            return ApiResponse.fail(message);
        }
        return ApiResponse.ok(response);
    }

    /**
     * 获取推送历史（分页，按时间倒序）
     *
     * @param page     页码（默认 1）
     * @param pageSize 每页条数（默认 10）
     * @return 分页结果
     */
    @GetMapping("/history")
    public ApiResponse<PageResult<MessageHistoryVO>> getMessageHistory(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {
        PageResult<MessageHistoryVO> history = messageService.getMessageHistory(page, pageSize);
        return ApiResponse.ok(history);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

