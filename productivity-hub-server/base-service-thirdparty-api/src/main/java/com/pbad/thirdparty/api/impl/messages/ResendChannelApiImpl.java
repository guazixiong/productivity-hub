package com.pbad.thirdparty.api.impl.messages;

import com.pbad.thirdparty.api.MessageChannelApi;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.Emails;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Resend 邮件推送服务实现.
 *
 * <p>说明：
 * - API Key 从配置参数传入（configs中的apiKey）。
 * - 收件人邮箱从配置参数传入（configs中的toEmail）。
 * - 发件人邮箱需要在 Resend 平台验证，从配置参数传入（configs中的fromEmail）。
 * - data 约定字段：title(邮件标题)、html(HTML邮件内容)。
 */
@Slf4j
@Service("resendChannelApi")
public class ResendChannelApiImpl implements MessageChannelApi {

    @Override
    public String sendMessage(Map<String, Object> data, Map<String, String> configs) {
        String title = (String) data.get("title");
        String html = (String) data.get("html");

        log.info("Resend 发送邮件: title={}", title);

        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Resend title 不能为空");
        }
        if (html == null || html.trim().isEmpty()) {
            throw new IllegalArgumentException("Resend html 不能为空");
        }

        String apiKey = configs == null ? null : configs.get("apiKey");
        String toEmail = configs == null ? null : configs.get("toEmail");
        String fromEmail = configs == null ? null : configs.get("fromEmail");
        
        if (apiKey == null || apiKey.isEmpty()) {
            throw new BusinessException("400", "Resend API Key 未配置");
        }

        // 兼容多收件人（逗号分隔）
        List<String> toEmails = Arrays.stream(toEmail.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        if (toEmails.isEmpty()) {
            throw new BusinessException("400", "Resend 收件人邮箱配置无效");
        }

        try {
            // 初始化 Resend 客户端
            Resend resend = new Resend(apiKey);
            Emails emails = resend.emails();

            // 构建邮件请求
            CreateEmailOptions emailOptions = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(toEmails)
                    .subject(title)
                    .html(html)
                    .build();

            // 发送邮件
            CreateEmailResponse response = emails.send(emailOptions);

            // 构建响应详情
            String responseDetails = "Resend 邮件发送成功: email_id=" + response.getId();
            log.info("Resend 响应详情: {}", responseDetails);

            return responseDetails;
        } catch (ResendException e) {
            log.error("Resend 发送邮件异常: {}", e.getMessage(), e);
            throw new RuntimeException("Resend 发送邮件异常: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Resend 发送邮件未知异常", e);
            throw new RuntimeException("Resend 发送邮件异常: " + e.getMessage(), e);
        }
    }
}

