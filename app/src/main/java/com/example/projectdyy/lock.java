package com.example.projectdyy;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.android.service.MqttAndroidClient;

public class lock extends AppCompatActivity {

    ImageView imgLock;
    TextView textlock;

    // 넘길 count 수 (솔레노이드 활성화 / 홀수가 닫힘, 짝수가 열림)
    public static int count = 2;

    private MqttAndroidClient mqttClient;
    //private boolean isMqttConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        imgLock = findViewById(R.id.imglock);
        textlock = findViewById(R.id.lock);

        // MQTT 연결

        connectToMQTTBroker();

        imgLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ++count;
                if (count >= 10) {
                    count = 2;
                }
                Log.v("제발", "count: " + count);
                if (count % 2 != 0) {
                    imgLock.setImageResource(R.drawable.lockimage);
                    textlock.setText("Lock");
                } else if (count % 2 == 0) {
                    imgLock.setImageResource(R.drawable.unlockimage);
                    textlock.setText("Unlock");
                }
                publishMessage();
            }
        });
    }

    private void connectToMQTTBroker() {

        Intent intent = getIntent();
        String mqttBroker = intent.getStringExtra("mqttBroker");
        String mqttUsername = intent.getStringExtra("mqttUsername");
        String mqttPassword = intent.getStringExtra("mqttPassword");

        Log.d("엠큐티티연결", "mqttBroker: "+mqttBroker);
        Log.d("엠큐티티연결", "mqttUsername: "+mqttUsername);
        Log.d("엠큐티티연결", "mqttPassword: "+mqttPassword);

        mqttClient = new MqttAndroidClient(getApplicationContext(), mqttBroker, "android-client");
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(mqttUsername);
        options.setPassword(mqttPassword.toCharArray());

        try {
            IMqttToken token = mqttClient.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    subscribeToTopic("solenoidControl");
                    Log.v("엠큐티티", "mqtt 성공");
                    // 연결 성공 시 이미지뷰 클릭 시에도 메시지 전송
                    publishMessage();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    exception.printStackTrace();
                    Log.e("엠큐티티", "mqtt 실패" + exception.getMessage(), exception);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void publishMessage() {
        String topic = "solenoidControl";
        try {
            // 버튼 클릭 시 솔레노이드 제어 메시지 전송
            Log.v("메시지 전달", "메시지 전달 코드까지 옴");
            MqttMessage mqttMessage = new MqttMessage(generateToggleMessage().getBytes());
            Log.v("메시지홀/짝", String.valueOf(mqttMessage));
            mqttClient.publish(topic, mqttMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void subscribeToTopic(String topic) {
        try {
            IMqttToken token = mqttClient.subscribe(topic, 0);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    mqttClient.setCallback(new MqttCallback() {
                        @Override
                        public void connectionLost(Throwable cause) {
                            // 연결이 끊겼을 때 처리
                            Log.e("엠큐티티확인용", "연결 끊김");
                        }

                        @Override
                        public void messageArrived(String topic, MqttMessage message) throws Exception {
                            // 메시지 도착 시 처리
                            String payload = new String(message.getPayload());
                            Log.v("엠큐티티확인용", "메시지 도착: "+payload);
                            //Toast.makeText(lock.this, "Received message: " + payload, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken token) {
                            // 메시지 전달 완료 시 처리
                            Log.v("엠큐티티확인용", "메시지 전달 완료함");
                        }
                    });
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    exception.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generateToggleMessage() {
        // 홀수일 경우 "odd", 짝수일 경우 "even" 반환
        return (count % 2 == 1) ? "odd" : "even";
    }
}