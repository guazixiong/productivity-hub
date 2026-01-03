package com.pbad.thirdparty.api.impl.messages;

import com.pbad.thirdparty.api.MessageChannelApi;
import common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 钉钉 Webhook 推送服务实现.
 *
 * <p>说明：
 * - Webhook 地址与加签秘钥从配置参数传入（configs中的webhook和sign）。
 * - data 约定字段：msgType(text、markdown、link)、content、atMobiles（可选，逗号分隔）。
 * - 如 sign 为空则不启用加签。
 */
@Slf4j
@Service("dingtalkChannelApi")
public class DingTalkChannelApiImpl implements MessageChannelApi {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String sendMessage(Map<String, Object> data, Map<String, String> configs) {
        String msgType = (String) data.get("msgType");
        String content = (String) data.get("content");
        String atMobilesRaw = (String) data.get("atMobiles");

        log.info("钉钉发送消息: msgType={}", msgType);

        if (msgType == null || msgType.trim().isEmpty()) {
            throw new IllegalArgumentException("DingTalk msgType 不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("DingTalk content 不能为空");
        }

        // 从调用方提供的配置读取 webhook 与 sign
        String webhook = configs == null ? null : configs.get("webhook");
        if (webhook == null || webhook.isEmpty()) {
            throw new BusinessException("400", "DingTalk webhook 未配置");
        }

        String signSecret = configs == null ? null : configs.get("sign");

        String requestUrl = appendSignIfNeeded(webhook, signSecret);

        Map<String, Object> payload = buildPayload(msgType, content, atMobilesRaw);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(requestUrl, payload, String.class);
            log.info("钉钉响应: status={}, body={}", response.getStatusCodeValue(), response.getBody());
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new BusinessException("500", "调用钉钉失败，HTTP 状态码：" + response.getStatusCodeValue());
            }
            return response.getBody() == null ? "DingTalk message sent successfully" : response.getBody();
        } catch (Exception ex) {
            log.error("钉钉发送失败: {}", ex.getMessage(), ex);
            if (ex instanceof BusinessException) {
                throw ex;
            }
            throw new BusinessException("500", "调用钉钉异常: " + ex.getMessage());
        }
    }

    private Map<String, Object> buildPayload(String msgType, String content, String atMobilesRaw) {
        Map<String, Object> payload = new HashMap<>();
        List<String> atMobiles = parseMobiles(atMobilesRaw);

        switch (msgType) {
            case "markdown":
                payload.put("msgtype", "markdown");
                Map<String, Object> markdown = new HashMap<>();
                markdown.put("title", buildMarkdownTitle(content));
                markdown.put("text", content);
                payload.put("markdown", markdown);
                payload.put("at", buildAtBlock(atMobiles));
                break;
            case "link":
                payload.put("msgtype", "link");
                Map<String, Object> link = new HashMap<>();
                link.put("title", buildMarkdownTitle(content));
                link.put("text", content);
                link.put("messageUrl", "https://www.dingtalk.com/"); // 占位链接
                payload.put("link", link);
                break;
            case "text":
            default:
                payload.put("msgtype", "text");
                Map<String, Object> text = new HashMap<>();
                text.put("content", content);
                payload.put("text", text);
                payload.put("at", buildAtBlock(atMobiles));
                break;
        }
        return payload;
    }

    private Map<String, Object> buildAtBlock(List<String> mobiles) {
        Map<String, Object> at = new HashMap<>();
        at.put("atMobiles", mobiles);
        at.put("isAtAll", false);
        return at;
    }

    private List<String> parseMobiles(String atMobilesRaw) {
        if (atMobilesRaw == null || atMobilesRaw.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String[] parts = atMobilesRaw.split(",");
        List<String> mobiles = new ArrayList<>();
        for (String part : parts) {
            String mobile = part.trim();
            if (!mobile.isEmpty()) {
                mobiles.add(mobile);
            }
        }
        return mobiles;
    }

    private String appendSignIfNeeded(String webhook, String signSecret) {
        if (signSecret == null || signSecret.trim().isEmpty()) {
            return webhook;
        }
        try {
            long timestamp = System.currentTimeMillis();
            String stringToSign = timestamp + "\n" + signSecret;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(signSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            String sign = URLEncoder.encode(Base64.getEncoder().encodeToString(signData), StandardCharsets.UTF_8.name());
            String delimiter = webhook.contains("?") ? "&" : "?";
            return webhook + delimiter + "timestamp=" + timestamp + "&sign=" + sign;
        } catch (Exception ex) {
            throw new BusinessException("500", "生成钉钉签名失败: " + ex.getMessage());
        }
    }

    private String buildMarkdownTitle(String content) {
        String normalized = content == null ? "" : content.trim();
        if (normalized.isEmpty()) {
            return "DingTalk Notification";
        }
        return normalized.length() <= 32 ? normalized : normalized.substring(0, 32);
    }
}

