package com.pbad.messages.service.impl;

import com.pbad.config.service.ConfigService;
import com.pbad.messages.service.MessageChannelService;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

/**
 * SendGrid 邮件推送服务实现.
 *
 * <p>说明：
 * - API Key 与发件人邮箱从配置参数模块 sys_config 读取（模块：sendgrid）；
 * - data 约定字段：recipients(逗号分隔多个邮箱)、subject、content。
 */
@Slf4j
@Service("sendgridChannelService")
@RequiredArgsConstructor
public class SendGridChannelServiceImpl implements MessageChannelService {

    private final ConfigService configService;

    @Override
    public String sendMessage(Map<String, Object> data) {
        String recipients = (String) data.get("recipients");
        String subject = (String) data.get("subject");
        String content = (String) data.get("content");

        log.info("SendGrid 发送邮件: recipients={}, subject={}", recipients, subject);

        if (recipients == null || recipients.trim().isEmpty()) {
            throw new IllegalArgumentException("SendGrid recipients 不能为空");
        }

        // 从配置中心读取 SendGrid 相关配置
        String apiKey = configService.getConfigValue("sendgrid", "sendgrid.apiKey");
        String fromEmail = configService.getConfigValue("sendgrid", "sendgrid.fromEmail");
        if (apiKey == null || apiKey.isEmpty() || fromEmail == null || fromEmail.isEmpty()) {
            throw new BusinessException("400", "SendGrid 配置不完整，请在配置中心填写 apiKey 和 fromEmail");
        }

        try {
            SendGrid sg = new SendGrid(apiKey);

            Email from = new Email(fromEmail);
            Content mailContent = new Content("text/plain", content);

            // 当前实现简单处理为单个收件人；如果是多个，可用逗号拆分循环创建 Personalization。
            String firstRecipient = recipients.split(",")[0].trim();
            Email to = new Email(firstRecipient);

            Mail mail = new Mail(from, subject, to, mailContent);

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            String responseDetails = buildResponseDetails(response);
            log.info("SendGrid 响应详情: {}", responseDetails);

            if (!isSuccess(response)) {
                throw new BusinessException("502", responseDetails);
            }
            return responseDetails;
        } catch (IOException e) {
            log.error("SendGrid 发送邮件异常", e);
            throw new RuntimeException("SendGrid 发送邮件异常", e);
        }
    }

    private boolean isSuccess(Response response) {
        return response != null && response.getStatusCode() >= 200 && response.getStatusCode() < 300;
    }

    private String buildResponseDetails(Response response) {
        if (response == null) {
            return "SendGrid 调用无响应";
        }

        StringBuilder builder = new StringBuilder("SendGrid 调用结果: status=")
                .append(response.getStatusCode());

        String body = response.getBody();
        if (body != null && !body.isEmpty()) {
            builder.append(", body=").append(body);
        }

        if (response.getHeaders() != null && !response.getHeaders().isEmpty()) {
            builder.append(", headers=").append(response.getHeaders());
        }

        return builder.toString();
    }
}

