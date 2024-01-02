package com.example.projectdyy;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PlaceSearchResponse {
    @SerializedName("results")
    private List<com.example.projectdyy.PlaceResult> results;

    @SerializedName("next_page_token")
    private String nextPageToken;

    @SerializedName("exampleField")
    private String exampleField;


    public List<com.example.projectdyy.PlaceResult> getResults() { return results; }

    public String getNextPageToken() { return nextPageToken; }

    public String getExampleField() { return exampleField; }
}
