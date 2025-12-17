package com.pbad.thirdparty.api.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pbad.thirdparty.api.HotDataApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 热点数据API实现类.
 *
 * @author pbad
 */
@Slf4j
@Service
public class HotDataApiImpl implements HotDataApi {

    private static final String REBANG_TOP_URL = "https://api.rebang.today/v1/items?tab=top&sub_tab=lasthour&page=1&version=1";
    private static final String REBANG_ZHIHU_URL = "https://api.rebang.today/v1/items?tab=zhihu&date_type=now&page=1&version=1";
    private static final String REBANG_WEIBO_URL = "https://api.rebang.today/v1/items?tab=weibo&sub_tab=search&version=2";
    private static final String REBANG_HUPU_URL = "https://api.rebang.today/v1/items?tab=hupu&sub_tab=all-gambia&date_type=now&page=1&version=1";
    private static final String REBANG_XIAOHONGSHU_URL = "https://api.rebang.today/v1/items?tab=xiaohongshu&sub_tab=hot-search&page=1&version=1";
    private static final String REBANG_BILIBILI_URL = "https://api.rebang.today/v1/items?tab=bilibili&sub_tab=popular&date_type=now&page=1&version=1";
    private static final String REBANG_DOUYIN_URL = "https://api.rebang.today/v1/items?tab=douyin&date_type=now&page=1&version=1";
    private static final String REBANG_BAIDU_TIEBA_URL = "https://api.rebang.today/v1/items?tab=baidu-tieba&sub_tab=topic&page=1&version=1";

    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 100;

    @Override
    public List<String> getHotSectionNames() {
        return Arrays.asList(
                "综合热榜",
                "知乎热搜",
                "微博热搜",
                "虎扑热帖",
                "小红书热帖",
                "哔哩哔哩热榜",
                "抖音热榜",
                "百度贴吧热帖"
        );
    }

    @Override
    public HotSectionVO getHotSectionByName(String sectionName, Integer limit) {
        int finalLimit = normalizeLimit(limit);
        List<HotItem> items = fetchHotListBySectionName(sectionName, finalLimit);
        return convertToSectionVO(sectionName, items);
    }

    @Override
    public List<HotSectionVO> getHotSections(Integer limit) {
        int finalLimit = normalizeLimit(limit);
        List<HotSectionVO> sections = new ArrayList<>();
        sections.add(convertToSectionVO("综合热榜", fetchHotList("综合热榜", REBANG_TOP_URL, finalLimit)));
        sections.add(convertToSectionVO("知乎热搜", fetchHotList("知乎热搜", REBANG_ZHIHU_URL, finalLimit)));
        sections.add(convertToSectionVO("微博热搜", fetchHotList("微博热搜", REBANG_WEIBO_URL, finalLimit)));
        sections.add(convertToSectionVO("虎扑热帖", fetchHotList("虎扑热帖", REBANG_HUPU_URL, finalLimit)));
        sections.add(convertToSectionVO("小红书热帖", fetchHotList("小红书热帖", REBANG_XIAOHONGSHU_URL, finalLimit)));
        sections.add(convertToSectionVO("哔哩哔哩热榜", fetchHotList("哔哩哔哩热榜", REBANG_BILIBILI_URL, finalLimit)));
        sections.add(convertToSectionVO("抖音热榜", fetchHotList("抖音热榜", REBANG_DOUYIN_URL, finalLimit)));
        sections.add(convertToSectionVO("百度贴吧热帖", fetchHotList("百度贴吧热帖", REBANG_BAIDU_TIEBA_URL, finalLimit)));
        return sections;
    }

    private HotSectionVO convertToSectionVO(String name, List<HotItem> items) {
        HotSectionVO sectionVO = new HotSectionVO();
        sectionVO.setName(name);
        List<HotItemVO> itemVOs = items.stream().map(item -> {
            HotItemVO vo = new HotItemVO();
            vo.setTitle(item.getTitle());
            vo.setLink(item.getLink());
            vo.setHeat(item.getHeat());
            vo.setDesc(item.getDesc());
            return vo;
        }).collect(Collectors.toList());
        sectionVO.setItems(itemVOs);
        return sectionVO;
    }

    private List<HotItem> fetchHotListBySectionName(String sectionName, int limit) {
        switch (sectionName) {
            case "综合热榜":
                return fetchHotList("综合热榜", REBANG_TOP_URL, limit);
            case "知乎热搜":
                return fetchHotList("知乎热搜", REBANG_ZHIHU_URL, limit);
            case "微博热搜":
                return fetchHotList("微博热搜", REBANG_WEIBO_URL, limit);
            case "虎扑热帖":
                return fetchHotList("虎扑热帖", REBANG_HUPU_URL, limit);
            case "小红书热帖":
                return fetchHotList("小红书热帖", REBANG_XIAOHONGSHU_URL, limit);
            case "哔哩哔哩热榜":
                return fetchHotList("哔哩哔哩热榜", REBANG_BILIBILI_URL, limit);
            case "抖音热榜":
                return fetchHotList("抖音热榜", REBANG_DOUYIN_URL, limit);
            case "百度贴吧热帖":
                return fetchHotList("百度贴吧热帖", REBANG_BAIDU_TIEBA_URL, limit);
            default:
                log.warn("未知的标签名称: {}", sectionName);
                return new ArrayList<>();
        }
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private List<HotItem> fetchHotList(String source, String url, int limit) {
        try {
            String body = HttpUtil.get(url, 5000);
            JSONObject obj = JSON.parseObject(body);
            if (obj == null || obj.getInteger("code") == null || obj.getInteger("code") != 200) {
                log.warn("获取 {} 失败，code 非 200", source);
                return fallback(source, limit);
            }
            JSONObject data = obj.getJSONObject("data");
            Object rawList = data.get("list");
            JSONArray array;
            if (rawList instanceof String) {
                array = JSON.parseArray((String) rawList);
            } else if (rawList instanceof JSONArray) {
                array = (JSONArray) rawList;
            } else {
                log.warn("获取 {} 失败，list 结构未知", source);
                return fallback(source, limit);
            }
            List<HotItem> items = new ArrayList<>();
            for (int i = 0; i < array.size() && items.size() < limit; i++) {
                JSONObject item = array.getJSONObject(i);
                String title = item.getString("title");
                if ("百度贴吧热帖".equals(source) && isBlank(title)) {
                    title = item.getString("name");
                }
                String link = firstNonBlank(item.getString("www_url"), item.getString("mobile_url"), item.getString("url"));
                if (isBlank(link)) {
                    if ("哔哩哔哩热榜".equals(source)) {
                        String bvid = item.getString("bvid");
                        if (!isBlank(bvid)) {
                            link = "https://www.bilibili.com/video/" + bvid;
                        }
                    } else if ("抖音热榜".equals(source)) {
                        String awemeId = item.getString("aweme_id");
                        if (!isBlank(awemeId)) {
                            link = "https://www.douyin.com/video/" + awemeId;
                        }
                    } else if ("百度贴吧热帖".equals(source)) {
                        String topicId = item.getString("id");
                        if (!isBlank(topicId)) {
                            link = "https://tieba.baidu.com/hottopic/browse/hottopic?topic_id=" + topicId;
                        }
                    }
                }
                String heat = firstNonBlank(item.getString("heat_str"),
                        numberToHeat(item.getDouble("heat_num")),
                        item.getString("light_str"),
                        item.getString("reply_str"),
                        item.getString("label_str"));
                String desc = firstNonBlank(item.getString("desc"), item.getString("describe"));
                if (isBlank(title) || isBlank(link)) {
                    continue;
                }
                items.add(new HotItem(title, link, heat, desc));
            }
            if (items.size() < limit && items.size() < array.size()) {
                log.warn("获取 {} 数据不足，期望 {} 条，实际返回 {} 条，API 返回 {} 条",
                        source, limit, items.size(), array.size());
            }
            if (!items.isEmpty()) {
                log.debug("获取 {} 成功，返回 {} 条数据（limit: {}）", source, items.size(), limit);
                return items;
            }
        } catch (Exception e) {
            log.warn("获取 {} 失败，使用备用数据: {}", source, e.getMessage());
        }
        log.debug("获取 {} 失败，返回备用数据（limit: {}）", source, limit);
        return fallback(source, limit);
    }

    private List<HotItem> fallback(String source, int limit) {
        return new ArrayList<HotItem>() {{
            add(new HotItem(source + " 暂不可用", "https://rebang.today", "稍后重试", null));
        }}.stream().limit(limit).collect(Collectors.toList());
    }

    private String numberToHeat(Double num) {
        if (num == null) {
            return null;
        }
        if (num >= 10000) {
            return String.format("%.1f 万热度", num / 10000);
        }
        return String.format("%.0f 热度", num);
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (!isBlank(v)) {
                return v;
            }
        }
        return null;
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    private static class HotItem {
        private String title;
        private String link;
        private String heat;
        private String desc;

        public HotItem(String title, String link, String heat, String desc) {
            this.title = title;
            this.link = link;
            this.heat = heat;
            this.desc = desc;
        }

        public String getTitle() {
            return title;
        }

        public String getLink() {
            return link;
        }

        public String getHeat() {
            return heat;
        }

        public String getDesc() {
            return desc;
        }
    }
}

