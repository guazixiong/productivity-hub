package com.pbad.messages.service.impl;

import com.alibaba.fastjson.JSON;
import com.pbad.config.service.ConfigService;
import com.pbad.generator.api.IdGeneratorApi;
import com.pbad.messages.domain.dto.MessageSendDTO;
import com.pbad.messages.domain.po.MessageHistoryPO;
import com.pbad.messages.domain.vo.MessageHistoryVO;
import com.pbad.messages.domain.vo.MessageSendResponseVO;
import com.pbad.messages.mapper.MessageHistoryMapper;
import com.pbad.messages.service.MessageService;
import com.pbad.thirdparty.api.MessageChannelApi;
import common.core.domain.PageResult;
import common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 消息推送服务实现类.
 */
@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    private final MessageHistoryMapper messageHistoryMapper;
    private final IdGeneratorApi idGeneratorApi;
    private final MessageChannelApi sendgridChannelApi;
    private final MessageChannelApi dingtalkChannelApi;
    private final MessageChannelApi resendChannelApi;
    private final ConfigService configService;

    public MessageServiceImpl(MessageHistoryMapper messageHistoryMapper,
                              IdGeneratorApi idGeneratorApi,
                              ConfigService configService,
                              @Qualifier("sendgridChannelApi") MessageChannelApi sendgridChannelApi,
                              @Qualifier("dingtalkChannelApi") MessageChannelApi dingtalkChannelApi,
                              @Qualifier("resendChannelApi") MessageChannelApi resendChannelApi) {
        this.messageHistoryMapper = messageHistoryMapper;
        this.idGeneratorApi = idGeneratorApi;
        this.configService = configService;
        this.sendgridChannelApi = sendgridChannelApi;
        this.dingtalkChannelApi = dingtalkChannelApi;
        this.resendChannelApi = resendChannelApi;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageSendResponseVO sendMessage(MessageSendDTO sendDTO, String userId) {
        if (sendDTO == null || sendDTO.getChannel() == null || sendDTO.getData() == null) {
            throw new BusinessException("400", "参数不能为空");
        }

        String channel = sendDTO.getChannel();
        Map<String, Object> data = sendDTO.getData();
        String originalChannel = channel;
        log.info("收到推送请求: channel={}", originalChannel);

        MessageChannelApi channelApi = resolveChannel(channel);
        Map<String, String> channelConfigs = buildChannelConfigs(channel, userId);

        String requestId = "req-" + idGeneratorApi.generateId();

        String response;
        String status;
        try {
            response = channelApi.sendMessage(data, channelConfigs);
            status = "success";
        } catch (Exception e) {
            log.error("发送消息失败: {}", e.getMessage(), e);
            response = "发送失败: " + e.getMessage();
            status = "failed";
        }

        saveMessageHistory(requestId, originalChannel, data, status, response, userId);

        MessageSendResponseVO responseVO = new MessageSendResponseVO();
        responseVO.setRequestId(requestId);
        responseVO.setStatus(status);
        responseVO.setDetail(Objects.equals(status, "success") ? "消息已送达" : response);
        return responseVO;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<MessageHistoryVO> getMessageHistory(int pageNum, int pageSize, String userId) {
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        int offset = (safePageNum - 1) * safePageSize;

        List<MessageHistoryPO> poList = messageHistoryMapper.selectPage(userId, offset, safePageSize);
        long total = messageHistoryMapper.countAll(userId);
        List<MessageHistoryVO> items = poList.stream().map(this::convertToVO).collect(Collectors.toList());
        return PageResult.of(safePageNum, safePageSize, total, items);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void saveMessageHistory(String requestId, String channel, Map<String, Object> data,
                                   String status, String response, String userId) {
        try {
            if (channel == null || channel.trim().isEmpty()) {
                log.error("保存消息历史记录失败: channel为空, requestId={}", requestId);
                return;
            }

            MessageHistoryPO history = new MessageHistoryPO();
            history.setId(requestId);
            history.setUserId(userId);
            history.setChannel(channel.trim());
            history.setRequestData(JSON.toJSONString(data));
            history.setStatus(status);
            history.setResponseData(response);
            messageHistoryMapper.insert(history);
            log.info("消息历史记录已保存: requestId={}, channel={}, status={}", requestId, channel, status);
        } catch (Exception e) {
            log.error("保存消息历史记录失败: requestId={}, channel={}, error={}", requestId, channel, e.getMessage(), e);
        }
    }

    private MessageChannelApi resolveChannel(String channel) {
        if ("sendgrid".equals(channel)) {
            return sendgridChannelApi;
        } else if ("dingtalk".equals(channel)) {
            return dingtalkChannelApi;
        } else if ("resend".equals(channel)) {
            return resendChannelApi;
        }
        throw new BusinessException("400", "不支持的推送渠道: " + channel);
    }

    private Map<String, String> buildChannelConfigs(String channel, String userId) {
        boolean useTemplate = "system".equals(userId);
        switch (channel) {
            case "dingtalk":
                return buildDingTalkConfig(useTemplate, userId);
            case "sendgrid":
                return buildSendGridConfig(useTemplate, userId);
            case "resend":
                return buildResendConfig(useTemplate, userId);
            default:
                return new HashMap<>();
        }
    }

    private Map<String, String> buildDingTalkConfig(boolean useTemplate, String userId) {
        Map<String, String> cfg = new HashMap<>();
        String webhook = useTemplate
                ? configService.getTemplateConfigValue("dingtalk", "dingtalk.webhook")
                : configService.getConfigValue("dingtalk", "dingtalk.webhook", userId);
        String sign = useTemplate
                ? configService.getTemplateConfigValue("dingtalk", "dingtalk.sign")
                : configService.getConfigValue("dingtalk", "dingtalk.sign", userId);
        cfg.put("webhook", webhook);
        if (sign != null) {
            cfg.put("sign", sign);
        }
        return cfg;
    }

    private Map<String, String> buildSendGridConfig(boolean useTemplate, String userId) {
        Map<String, String> cfg = new HashMap<>();
        cfg.put("apiKey", useTemplate
                ? configService.getTemplateConfigValue("sendgrid", "sendgrid.apiKey")
                : configService.getConfigValue("sendgrid", "sendgrid.apiKey", userId));
        cfg.put("fromEmail", useTemplate
                ? configService.getTemplateConfigValue("sendgrid", "sendgrid.fromEmail")
                : configService.getConfigValue("sendgrid", "sendgrid.fromEmail", userId));
        return cfg;
    }

    private Map<String, String> buildResendConfig(boolean useTemplate, String userId) {
        Map<String, String> cfg = new HashMap<>();
        cfg.put("apiKey", useTemplate
                ? configService.getTemplateConfigValue("resend", "resend.apiKey")
                : configService.getConfigValue("resend", "resend.apiKey", userId));
        cfg.put("toEmail", useTemplate
                ? configService.getTemplateConfigValue("resend", "resend.toEmail")
                : configService.getConfigValue("resend", "resend.toEmail", userId));
        cfg.put("fromEmail", useTemplate
                ? configService.getTemplateConfigValue("resend", "resend.fromEmail")
                : configService.getConfigValue("resend", "resend.fromEmail", userId));
        return cfg;
    }

    private MessageHistoryVO convertToVO(MessageHistoryPO po) {
        MessageHistoryVO vo = new MessageHistoryVO();
        vo.setId(po.getId());
        vo.setChannel(po.getChannel());
        vo.setStatus(po.getStatus());
        vo.setResponse(po.getResponseData());
        vo.setCreatedAt(po.getCreatedAt());

        try {
            Map<String, Object> request = JSON.parseObject(po.getRequestData(), Map.class);
            vo.setRequest(request);
        } catch (Exception e) {
            log.warn("解析请求数据失败: {}", e.getMessage());
            vo.setRequest(new HashMap<>());
        }
        return vo;
    }
}


