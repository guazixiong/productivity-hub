package com.pbad.thirdparty.api;

/**
 * 天气API接口.
 * <p>提供天气查询功能，不依赖数据库，所有参数由调用方传入.</p>
 *
 * @author pbad
 */
public interface WeatherApi {

    /**
     * 获取指定经纬度的天气信息.
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @param cityName  城市名称（可选，用于显示）
     * @return 天气信息
     */
    WeatherInfo getWeatherInfoByCoordinates(Double latitude, Double longitude, String cityName);

    /**
     * 天气信息VO.
     */
    class WeatherInfo {
        private String city;
        private String province;
        private String address;
        private String weather;
        private String temp;
        private String wind;
        private String humidity;

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getWeather() {
            return weather;
        }

        public void setWeather(String weather) {
            this.weather = weather;
        }

        public String getTemp() {
            return temp;
        }

        public void setTemp(String temp) {
            this.temp = temp;
        }

        public String getWind() {
            return wind;
        }

        public void setWind(String wind) {
            this.wind = wind;
        }

        public String getHumidity() {
            return humidity;
        }

        public void setHumidity(String humidity) {
            this.humidity = humidity;
        }
    }
}

