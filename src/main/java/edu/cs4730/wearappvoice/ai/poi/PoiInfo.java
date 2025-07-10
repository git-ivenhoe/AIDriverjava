package edu.cs4730.wearappvoice.ai.poi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PoiInfo {
    public String id;
    public String name;
    public String type;
    public String address;
    public double longitude;
    public double latitude;

    // 必须添加这个无参构造函数
    public PoiInfo() {}

    public PoiInfo(String id, String name, String type, String address, String location) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.address = address;
        if (location != null && location.contains(",")) {
            String[] parts = location.split(",");
            this.longitude = Double.parseDouble(parts[0]);
            this.latitude = Double.parseDouble(parts[1]);
        }
    }

    // Getters omitted for brevity


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
