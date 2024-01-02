package com.example.projectdyy;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private VPAdepter adapter; // VPAdepter 인스턴스를 멤버 변수로 선언
    private static final int QR_REQUEST_CODE = 1;
    private ViewPager vp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vp = findViewById(R.id.viewpager);
        adapter = new VPAdepter(getSupportFragmentManager()); // 멤버 변수 adapter를 초기화
        vp.setAdapter(adapter);

        //연동
        TabLayout tab = findViewById(R.id.tab);
        tab.setupWithViewPager(vp);

        //버튼 클릭시 액티비티 전환
        Button mapButton = findViewById(R.id.button_map);
        Button qrButton = findViewById(R.id.button_qr);

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });

        qrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, QrActivity.class);
                startActivityForResult(intent, QR_REQUEST_CODE);
            }
        });


    }

    // onActivityResult 메서드를 통해 QrActivity에서의 결과를 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == QR_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                String mqttBroker = data.getStringExtra("mqttBroker");
                String mqttUsername = data.getStringExtra("mqttUsername");
                String mqttPassword = data.getStringExtra("mqttPassword");
                // 수정된 부분: Fragment1에 MQTT 정보 전달
                Fragment1 fragment = (Fragment1) adapter.getItem(0);
                if (fragment != null) {
                    fragment.setMQTTInfo(mqttBroker, mqttUsername, mqttPassword);
                }
            }
        }
    }
}