package com.example.projectdyy;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {
    private GoogleMap mMap;
    private GooglePlacesApiService apiService; //API
    private FusedLocationProviderClient fusedLocationProviderClient;
    private String apiKey; // 초기화는 onCreate()에서 수행
    //private static final double DEFAULT_LATITUDE = 37.5665; // 서울의 위도 값
    //private static final double DEFAULT_LONGITUDE = 126.9780; // 서울의 경도 값
    private SearchView searchView; //서치뷰
    private List<MarkerOptions> markerList = new ArrayList<>(); //기존 마커 모두 지우는 메서드
    private static final int REQUEST_LOCATION_PERMISSION = 1; //위치 권한 요청
    private static LatLng Input_LatLng = null;
    private static int AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final String TAG = "FinalTest";
    private static final String filterType = "laundry";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        initView();
        // 위치 허가 확인
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        // API 키 초기화
        apiKey = getResources().getString(R.string.google_maps_api_key);

        // FusedLocationProviderClient 초기화
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // map fragment 가져옴
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        // 진행 하기 전 mapFragment 가 null 인지 확인
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            // fragment 가 null 인 경우 (e.g., incorrect fragment ID in layout)처리
            // 오류를 기록하거나 사용자에게 메시지 표시
            Log.e("MyApp", "Map fragment is null");
        }


        //검색
        //SearchView
        androidx.appcompat.widget.SearchView searchView;


        // Retrofit 인스턴스 가져오기
        Retrofit retrofit = RetrofitClient.getClient(apiKey);

        // GooglePlacesApiService 인스턴스 생성
        apiService = retrofit.create(GooglePlacesApiService.class);
    }

    private void initView(){
        searchView = findViewById(R.id.searchView);
    }

    // 기존 마커들을 모두 지우는 메서드
    private void clearMarkers() {
        for (MarkerOptions marker : markerList) {
            marker = null;
        }
        markerList.clear();
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // 키보드 내리기
                InputMethodManager manager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                // 검색 버튼이 클릭되면 호출됩니다.
                // 이 부분에서 Google Places API를 사용하여 장소를 검색하고 지도에 표시하는 로직을 추가할 수 있습니다.
                // 클릭된 위치에 대한 Google Places API 호출

                GooglePlacesApiService apiService = RetrofitClient.getClient(apiKey).create(GooglePlacesApiService.class);
                Call<PlaceSearchResponse> call = apiService.getPlaceTextSearch(query, filterType, apiKey);

                call.enqueue(new Callback<PlaceSearchResponse>() {
                    @Override
                    public void onResponse(Call<PlaceSearchResponse> call, Response<PlaceSearchResponse> response) {
                        if (response.isSuccessful()) {
                            PlaceSearchResponse placeSearchResponse = response.body();

                            if (placeSearchResponse != null && placeSearchResponse.getResults() != null && !placeSearchResponse.getResults().isEmpty()) {

                                // 기존 마커들 삭제
                                clearMarkers();

                                // 새로운 마커 추가
                                for (PlaceResult result : placeSearchResponse.getResults()) {
                                    String placeId = result.getPlaceId();
                                    String placeName = result.getName();
                                    String placeAddress = result.getFormattedAddress();
                                    double placeLatitude = result.getGeometry().getLocation().getLat();
                                    double placeLongitude = result.getGeometry().getLocation().getLng();

                                    String desc = placeAddress + "\n" + placeId;

                                    LatLng location = new LatLng(placeLatitude, placeLongitude);
                                    addMarkerToMap(placeName, desc, location);
                                }
                                // 검색된 위치로 카메라 이동
                                if (!placeSearchResponse.getResults().isEmpty()) {
                                    PlaceResult firstPlace = placeSearchResponse.getResults().get(0);
                                    double firstPlaceLatitude = firstPlace.getGeometry().getLocation().getLat();
                                    double firstPlaceLongitude = firstPlace.getGeometry().getLocation().getLng();
                                    LatLng firstPlaceLocation = new LatLng(firstPlaceLatitude, firstPlaceLongitude);
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstPlaceLocation, 15));
                                }

                                // 이 정보를 사용하여 앱에서 가게 정보를 표시합니다.
                                /*runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // UI 스레드에서 실행되도록 보장
                                        showPlaceDetailsDialog(placeName, placeAddress);
                                    }
                                });*/
                            } else {
                                Log.d("MyApp", "Place Search response is empty or null.");
                            }
                        } else {
                            Log.d("MyApp", "Place Search API call failed. Code: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<PlaceSearchResponse> call, Throwable t) {
                        Log.e("MyApp", "Place Search API call failed", t);
                    }
                });
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // 검색어가 변경될 때마다 호출됩니다.
                // 이 부분에서 실시간으로 검색어에 따라 검색 결과를 표시하는 로직을 추가할 수 있습니다.
                return false;
            }
        });
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d("MyApp", "onMapReady called");
        mMap = googleMap;

        // 위치허가 재확인
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // 권한이 부여된 경우
            mMap.setMyLocationEnabled(true);

            // 마지막으로 알려진 위치를 불러옴.
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                            // 현재 위치에 마커 추가
                            //mMap.addMarker(new MarkerOptions().position(currentLatLng).title("My Location"));

                            // 현재 위치로 카메라 이동
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));

                            // 가상 매장 추가 함수 호출
                            addVirtualStore(36.336289359611506, 127.45820810545422, "대전 셀프 빨래방", "대전대학교 에튜파크 2층 208호");
                        }
                    });

            // 맵 클릭 이벤트 핸들러 추가
            mMap.setOnMapClickListener(this);
            mMap.setOnMarkerClickListener(this);

            setupSearchView();
        }
    }



    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        Log.d("MainActivity", "Map clicked at: " + latLng.latitude + ", " + latLng.longitude);

        Log.d("MyApp", "onMapClick: " + latLng.toString());

        Log.d("MyApp", "Map clicked at: " + latLng.latitude + ", " + latLng.longitude);

        // 클릭된 위치에 마커 추가
        //mMap.addMarker(new MarkerOptions().position(latLng).title("Clicked Location"));

        // 클릭된 위치에 대한 Google Places API 호출 전 로그
        Log.d("MapActivity", "Before Google Places API call");

        // 클릭된 위치에 대한 Google Places API 호출
        GooglePlacesApiService apiService = RetrofitClient.getClient(apiKey).create(GooglePlacesApiService.class);

        String latLngStr = latLng.latitude + "," + latLng.longitude;
        // Place Search API 요청
        Call<PlaceSearchResponse> call = apiService.getPlaceSearch(
                latLngStr, 1500, filterType, apiKey);

        // 클릭된 위치에 대한 Google Places API 호출 후 로그
        Log.d("MapActivity", "After Google Places API call");

        call.enqueue(new Callback<PlaceSearchResponse>() {
            @Override
            public void onResponse(Call<PlaceSearchResponse> call, Response<PlaceSearchResponse> response) {
                if (response.isSuccessful()) {
                    // 응답이 성공인 경우
                    PlaceSearchResponse placeSearchResponse = response.body();

                    if (placeSearchResponse != null && placeSearchResponse.getResults() != null && !placeSearchResponse.getResults().isEmpty()) {
                        // 기존 마커들 삭제
                        clearMarkers();

                        // 여기서 장소 정보를 가져와 사용할 수 있습니다.
                        // 예를 들어, 마커를 추가하거나 다른 작업을 수행할 수 있습니다.
                        for (PlaceResult result : placeSearchResponse.getResults()){
                            String placeId = result.getPlaceId();
                            String placeName = result.getName();
                            String placeAddress = result.getFormattedAddress();
                            double placeLatitude = result.getGeometry().getLocation().getLat();
                            double placeLongitude = result.getGeometry().getLocation().getLng();

                            String desc = placeAddress + "\n" + placeId;

                            LatLng location = new LatLng(placeLatitude, placeLongitude);
                            addMarkerToMap(placeName, desc, location);
                        }
                    }else {
                        Log.d("MyApp", "Place Search response is empty or null.");
                    }
                } else {
                    Log.d("MyApp", "Place Search API call failed. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PlaceSearchResponse> call, Throwable t) {
                // 실패 시 처리
            }
        });
    }
    private void addMarkerToMap(String placeName, String desc, LatLng location) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(location)
                .title(placeName)
                .snippet(desc);

        mMap.addMarker(markerOptions);
        markerList.add(markerOptions);
    }

    // 가상의 매장을 생성하여 마커로 추가하는 함수
    private void addVirtualStore(double latitude, double longitude, String storeName, String storeAddress) {
        LatLng storeLocation = new LatLng(latitude, longitude);
        Marker storeMarker = mMap.addMarker(new MarkerOptions()
                .position(storeLocation)
                .title("대전 셀프 빨래방 (대전대점)") //마커의 제목을 설정
        );

        storeMarker.setTag(storeAddress);
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d("MyApp", "Marker clicked: " + marker.getTitle());

        // 클릭한 마커의 제목을 통해 place_id 등의 식별자를 얻어옴
        String snippet = marker.getSnippet();
        if(snippet != null){
            String[] desc = marker.getSnippet().split("\n");
            String address = "";
            String placeId = "";
            if(desc.length > 0){
                address = desc[0];
                if(desc.length > 1){
                    placeId = desc[1];

                    Log.d("MyApp", "Before getPlaceDetails");

                    // Place Details API 호출
                    getPlaceDetails(placeId);

                    Log.d("MyApp", "After getPlaceDetails");


                }
            }
        }else {
            Log.d("MyApp", "Snippet is null for this marker.");
        }

        Log.d("MyApp", "Before getPlaceDetails");

        Log.d("MyApp", "After getPlaceDetails");

        // 마커를 클릭할 때 매장 정보를 다이얼로그로 표시 (가상의 빨래방 지정)
        String storeName = marker.getTitle();
        String storeAddress = (String) marker.getTag();

        if (storeAddress != null) {
            showPlaceDetailsDialog(storeName, storeAddress);
        } else {
            // 정보가 없는 경우에 대한 처리
            Log.d("MyApp", "No information available for this store.");
        }

        //기본 동작 막기
        return true;
    }

    private void getPlaceDetails(String placeId) {
        // Google Places API를 사용하여 Place Details를 수행
        GooglePlacesApiService apiService = RetrofitClient.getClient(apiKey).create(GooglePlacesApiService.class);

        // Place Details API 요청
        Call<PlaceDetailsResponse> detailsCall = apiService.getPlaceDetails(placeId, apiKey);

        detailsCall.enqueue(new Callback<PlaceDetailsResponse>() {
            @Override
            public void onResponse(Call<PlaceDetailsResponse> call, Response<PlaceDetailsResponse> response) {
                if (response.isSuccessful()) {
                    // 응답이 성공인 경우;
                    PlaceDetailsResponse placeDetailsResponse = response.body();

                    if (placeDetailsResponse != null && placeDetailsResponse.getResult() != null) {
                        // 장소의 세부 정보를 사용합니다.
                        String name = placeDetailsResponse.getResult().getName();
                        String address = placeDetailsResponse.getResult().getFormattedAddress();

                        // 이 정보를 사용하여 앱에서 가게 정보를 표시합니다.
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // UI 스레드에서 실행되도록 보장
                                showPlaceDetailsDialog(name, address);
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<PlaceDetailsResponse> call, Throwable t) {
                Log.e("MyApp", "Place Details API call failed", t);
                // 실패 시 처리
            }
        });
    }
    private void showPlaceDetailsDialog(String name, String address) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(name)
                .setMessage("주소: " + address)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // OK 버튼 클릭 시 처리
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}