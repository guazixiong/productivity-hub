package com.pbad.thirdparty.api.impl;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pbad.thirdparty.api.DailyQuoteApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 每日一签API实现类.
 *
 * @author pbad
 */
@Slf4j
@Service
public class DailyQuoteApiImpl implements DailyQuoteApi {

    private static final String DAILY_QUOTE_API_URL = "https://v1.hitokoto.cn/";

    @Override
    public DailyQuote getDailyQuote() {
        try {
            String body = HttpRequest.get(DAILY_QUOTE_API_URL)
                    .timeout(5000)
                    .header("Accept", "application/json")
                    .header("Accept-Encoding", "identity")
                    .execute()
                    .body();
            JSONObject obj = JSON.parseObject(body);
            if (obj != null) {
                String hitokoto = obj.getString("hitokoto");
                String from = obj.getString("from");
                String fromWho = obj.getString("from_who");
                if (isBlank(fromWho)) {
                    fromWho = from;
                }
                if (isBlank(fromWho)) {
                    fromWho = "未知";
                }
                if (!isBlank(hitokoto)) {
                    return new DailyQuote(hitokoto, fromWho);
                }
            }
        } catch (Exception e) {
            log.warn("获取每日一言失败: {}", e.getMessage());
        }
        return new DailyQuote("快谈对象!!!!", "未知");
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}

