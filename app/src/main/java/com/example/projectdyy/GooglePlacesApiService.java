package com.example.projectdyy;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GooglePlacesApiService {
        //지정된 위치 주변 장소 찾음
        @GET("nearbysearch/json")
        Call<PlaceSearchResponse> getPlaceSearch(@Query("location") String location, @Query("radius") int radius, @Query("type") String type, @Query("key") String apiKey);

        //특정 장소의 상세 정보 요청
        @GET("details/json")
        Call<PlaceDetailsResponse> getPlaceDetails(@Query("place_id") String placeId, @Query("key") String apiKey);

        // 텍스트 기반 검색으로 장소 찾음
        @GET("textsearch/json")
        Call<PlaceSearchResponse> getPlaceTextSearch(@Query("query") String query,@Query("type") String type, @Query("key") String apiKey);
}



