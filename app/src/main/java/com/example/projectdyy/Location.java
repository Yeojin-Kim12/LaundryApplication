package com.example.projectdyy;

import com.google.gson.annotations.SerializedName;

public class Location {
    @SerializedName("lat")
    private double lat;

    @SerializedName("lng")
    private double lng;

    // 다른 필드와 해당하는 getter 메서드들을 추가할 수 있습니다.

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }
}
