package com.example.projectdyy;

import com.google.gson.annotations.SerializedName;

public class PlaceDetailsResponse {
    @SerializedName("result")
    private PlaceResult result;

    @SerializedName("status")
    private String status;

    @SerializedName("exampleField")
    private String exampleField;


    public PlaceResult getResult() {
        return result;
    }

    public String getStatus() {
        return status;
    }

    public String getExampleField() { return exampleField; }

}
