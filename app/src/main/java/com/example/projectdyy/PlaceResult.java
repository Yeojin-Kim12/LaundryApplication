package com.example.projectdyy;

import com.google.gson.annotations.SerializedName;

public class PlaceResult {
    @SerializedName("place_id")
    private String placeId;

    @SerializedName("geometry")
    private Geometry geometry;

    @SerializedName("name")
    private String name;

    @SerializedName("formatted_address")
    private String formattedAddress;

    @SerializedName("rating")
    private double rating;

    // 다른 필요한 필드 추가

    public String getPlaceId() {
        return placeId;
    }

    public Geometry getGeometry() { return geometry; }

    public String getName() {
        return name;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public double getRating() {
        return rating;
    }

    public class Geometry {

        @SerializedName("location")
        private com.example.projectdyy.Location location;
        // 다른 필드와 해당하는 getter 메서드들을 추가할 수 있습니다.

        public com.example.projectdyy.Location getLocation() {
            return location;
        }
    }
}

