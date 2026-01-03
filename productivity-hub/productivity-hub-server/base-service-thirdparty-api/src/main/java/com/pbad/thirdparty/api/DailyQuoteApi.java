package com.pbad.thirdparty.api;

/**
 * 每日一签API接口.
 * <p>提供每日一言查询功能，不依赖数据库.</p>
 *
 * @author pbad
 */
public interface DailyQuoteApi {

    /**
     * 获取每日一言.
     *
     * @return 每日一言信息
     */
    DailyQuote getDailyQuote();

    /**
     * 每日一言VO.
     */
    class DailyQuote {
        private String quote;
        private String from;

        public DailyQuote() {
        }

        public DailyQuote(String quote, String from) {
            this.quote = quote;
            this.from = from;
        }

        public String getQuote() {
            return quote;
        }

        public void setQuote(String quote) {
            this.quote = quote;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }
    }
}

