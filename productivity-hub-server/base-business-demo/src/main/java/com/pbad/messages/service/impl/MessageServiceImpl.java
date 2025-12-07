package com.pbad.messages.service.impl;

import com.alibaba.fastjson.JSON;
import com.pbad.generator.api.IdGeneratorApi;
import com.pbad.messages.domain.dto.MessageSendDTO;
import com.pbad.messages.domain.po.MessageHistoryPO;
import com.pbad.messages.domain.vo.MessageHistoryVO;
import com.pbad.messages.domain.vo.MessageSendResponseVO;
import com.pbad.messages.mapper.MessageHistoryMapper;
import com.pbad.messages.service.MessageChannelService;
import com.pbad.messages.service.MessageService;
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
import java.util.stream.Collectors;

/**
 * 消息推送服务实现类.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    private final MessageHistoryMapper messageHistoryMapper;
    private final IdGeneratorApi idGeneratorApi;
    private final MessageChannelService sendgridChannelService;
    private final MessageChannelService dingtalkChannelService;
    private final MessageChannelService resendChannelService;

    public MessageServiceImpl(MessageHistoryMapper messageHistoryMapper,
                              IdGeneratorApi idGeneratorApi,
                              @Qualifier("sendgridChannelService") MessageChannelService sendgridChannelService,
                              @Qualifier("dingtalkChannelService") MessageChannelService dingtalkChannelService,
                              @Qualifier("resendChannelService") MessageChannelService resendChannelService) {
        this.messageHistoryMapper = messageHistoryMapper;
        this.idGeneratorApi = idGeneratorApi;
        this.sendgridChannelService = sendgridChannelService;
        this.dingtalkChannelService = dingtalkChannelService;
        this.resendChannelService = resendChannelService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageSendResponseVO sendMessage(MessageSendDTO sendDTO) {
        // 参数校验
        if (sendDTO == null || sendDTO.getChannel() == null || sendDTO.getData() == null) {
            throw new BusinessException("400", "参数不能为空");
        }

        String channel = sendDTO.getChannel();
        Map<String, Object> data = sendDTO.getData();

        // 记录原始channel值，确保保存历史记录时使用正确的channel
        String originalChannel = channel;
        log.info("收到推送请求: channel={}", originalChannel);

        // 选择对应的渠道服务
        MessageChannelService channelService;
        if ("sendgrid".equals(channel)) {
            channelService = sendgridChannelService;
        } else if ("dingtalk".equals(channel)) {
            channelService = dingtalkChannelService;
        } else if ("resend".equals(channel)) {
            channelService = resendChannelService;
        } else {
            throw new BusinessException("400", "不支持的推送渠道: " + channel);
        }

        // 生成请求ID
        String requestId = "req-" + idGeneratorApi.generateId();

        // 发送消息
        String response;
        String status;
        try {
            response = channelService.sendMessage(data);
            status = "success";
        } catch (Exception e) {
            log.error("发送消息失败: {}", e.getMessage(), e);
            response = "发送失败: " + e.getMessage();
            status = "failed";
        }

        // 保存历史记录（使用独立事务，确保即使主事务回滚也能保存）
        // 使用originalChannel确保保存正确的channel值
        saveMessageHistory(requestId, originalChannel, data, status, response);

        // 构建响应
        MessageSendResponseVO responseVO = new MessageSendResponseVO();
        responseVO.setRequestId(requestId);
        responseVO.setStatus(status);
        responseVO.setDetail(status.equals("success") ? "消息已送达" : response);

        return responseVO;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<MessageHistoryVO> getMessageHistory(int pageNum, int pageSize) {
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        int offset = (safePageNum - 1) * safePageSize;

        List<MessageHistoryPO> poList = messageHistoryMapper.selectPage(offset, safePageSize);
        long total = messageHistoryMapper.countAll();
        List<MessageHistoryVO> items = poList.stream().map(this::convertToVO).collect(Collectors.toList());
        return PageResult.of(safePageNum, safePageSize, total, items);
    }

    /**
     * 保存消息历史记录（使用独立事务，确保即使主事务回滚也能保存）
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void saveMessageHistory(String requestId, String channel, Map<String, Object> data, 
                                   String status, String response) {
        try {
            // 确保channel值不为空且正确
            if (channel == null || channel.trim().isEmpty()) {
                log.error("保存消息历史记录失败: channel为空, requestId={}", requestId);
                return;
            }
            
            MessageHistoryPO history = new MessageHistoryPO();
            history.setId(requestId);
            history.setChannel(channel.trim()); // 去除前后空格
            history.setRequestData(JSON.toJSONString(data));
            history.setStatus(status);
            history.setResponseData(response);
            messageHistoryMapper.insert(history);
            log.info("消息历史记录已保存: requestId={}, channel={}, status={}", requestId, channel, status);
        } catch (Exception e) {
            log.error("保存消息历史记录失败: requestId={}, channel={}, error={}", requestId, channel, e.getMessage(), e);
            // 即使保存历史记录失败，也不影响主流程
        }
    }

    /**
     * 转换为 VO
     */
    private MessageHistoryVO convertToVO(MessageHistoryPO po) {
        MessageHistoryVO vo = new MessageHistoryVO();
        vo.setId(po.getId());
        vo.setChannel(po.getChannel());
        vo.setStatus(po.getStatus());
        vo.setResponse(po.getResponseData());
        vo.setCreatedAt(po.getCreatedAt());

        // 解析请求数据
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

