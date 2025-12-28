package com.pbad.thirdparty.api;

/**
 * 位置API接口.
 * <p>提供位置查询功能，支持IP地址和经纬度定位.</p>
 *
 * @author pbad
 */
public interface LocationApi {

    /**
     * 根据IP地址获取位置信息（使用天地图API）.
     *
     * @param ip IP地址
     * @return 位置信息
     */
    LocationInfo getLocationByIp(String ip);

    /**
     * 根据经纬度获取位置信息（使用天地图API）.
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return 位置信息
     */
    LocationInfo getLocationByCoordinates(Double latitude, Double longitude);

    /**
     * 位置信息VO.
     */
    class LocationInfo {
        private String city;
        private String province;
        private String address;
        private String district;
        private Double latitude;
        private Double longitude;

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

        public String getDistrict() {
            return district;
        }

        public void setDistrict(String district) {
            this.district = district;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }
    }
}

