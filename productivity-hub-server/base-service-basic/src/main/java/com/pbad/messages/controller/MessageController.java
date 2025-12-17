package com.pbad.messages.controller;

import com.pbad.messages.domain.dto.MessageSendDTO;
import com.pbad.messages.domain.vo.MessageHistoryVO;
import com.pbad.messages.domain.vo.MessageSendResponseVO;
import com.pbad.messages.service.MessageService;
import common.core.domain.ApiResponse;
import common.core.domain.PageResult;
import common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 消息推送控制器.
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/send")
    public ApiResponse<MessageSendResponseVO> sendMessage(@RequestBody MessageSendDTO sendDTO, HttpServletRequest request) {
        String userId = extractUserId(request);
        if (userId == null) {
            return ApiResponse.unauthorized("Token 无效或过期");
        }

        MessageSendResponseVO response = messageService.sendMessage(sendDTO, userId);
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

    @GetMapping("/history")
    public ApiResponse<PageResult<MessageHistoryVO>> getMessageHistory(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        String userId = extractUserId(request);
        if (userId == null) {
            return ApiResponse.unauthorized("Token 无效或过期");
        }

        PageResult<MessageHistoryVO> history = messageService.getMessageHistory(page, pageSize, userId);
        return ApiResponse.ok(history);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String extractUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = JwtUtil.extractTokenFromHeader(authHeader);
        if (token == null || !JwtUtil.validateToken(token)) {
            return null;
        }
        String userId = JwtUtil.getUserIdFromToken(token);
        if (userId == null || userId.isEmpty()) {
            return null;
        }
        return userId;
    }
}


