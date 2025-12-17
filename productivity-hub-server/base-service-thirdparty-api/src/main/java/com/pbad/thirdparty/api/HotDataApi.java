package com.pbad.thirdparty.api;

import java.util.List;

/**
 * 热点数据API接口.
 * <p>提供热点数据查询功能，不依赖数据库，所有参数由调用方传入.</p>
 *
 * @author pbad
 */
public interface HotDataApi {

    /**
     * 获取所有热点标签名称列表.
     *
     * @return 标签名称列表
     */
    List<String> getHotSectionNames();

    /**
     * 根据标签名称获取热点数据.
     *
     * @param sectionName 标签名称（如：综合热榜、知乎热搜等）
     * @param limit       返回条数限制
     * @return 热点数据
     */
    HotSectionVO getHotSectionByName(String sectionName, Integer limit);

    /**
     * 获取所有热点数据.
     *
     * @param limit 每个标签返回条数限制
     * @return 热点数据列表
     */
    List<HotSectionVO> getHotSections(Integer limit);

    /**
     * 热点数据项VO.
     */
    class HotItemVO {
        private String title;
        private String link;
        private String heat;
        private String desc;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public String getHeat() {
            return heat;
        }

        public void setHeat(String heat) {
            this.heat = heat;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 热点板块VO.
     */
    class HotSectionVO {
        private String name;
        private List<HotItemVO> items;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<HotItemVO> getItems() {
            return items;
        }

        public void setItems(List<HotItemVO> items) {
            this.items = items;
        }
    }
}

