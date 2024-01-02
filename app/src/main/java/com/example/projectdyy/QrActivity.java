package com.example.projectdyy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class QrActivity extends AppCompatActivity {

    String brokerUrl;
    String username;
    String password;


    // Paho MQTT 변수
    private MqttAndroidClient mqttAndroidClient;
    private final String TAG = "로그";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);


        // QR 코드 스캔 시작
        startQRCodeScan();
    }

    private void startQRCodeScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setBeepEnabled(false);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    // QR 코드 스캔 결과 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                // QR 코드에서 추출한 데이터를 사용하여 MQTT 브로커에 연결
                String qrData = result.getContents();
                Log.d(TAG, "onActivityResult: "+qrData);
                connectToMQTTBroker(qrData);
            } else {
                // 사용자가 스캔을 취소한 경우
                Toast.makeText(this, "스캔이 취소되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void connectToMQTTBroker(String qrData) {
        // MQTT 브로커 연결 로직을 추가
        String[] mqttInfo = qrData.split("\\?");
        if (mqttInfo.length == 2) {
            brokerUrl = mqttInfo[0].substring(5);
            String[] credentials = mqttInfo[1].split("&");
            username = credentials[0].substring(credentials[0].indexOf("=") + 1);
            password = credentials[1].substring(credentials[1].indexOf("=") + 1);
            String clientId = "qr-client";
            mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), brokerUrl, clientId);
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setUserName(username);
            mqttConnectOptions.setPassword(password.toCharArray());
            try {
                IMqttToken token = mqttAndroidClient.connect(mqttConnectOptions);
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.d(TAG, "연결 성공");
                        sendResultToMainActivity(brokerUrl, username, password);;
                    }
                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.e(TAG, "연결 실패: " + exception.getMessage());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            // MQTT 메시지 수신을 처리할 콜백 설정
            mqttAndroidClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.d(TAG, "연결이 끊어짐");
                    // 연결이 끊어졌을 때 처리할 작업을 수행할 수 있습니다.
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    Log.d(TAG, "메시지 도착: " + new String(message.getPayload()));
                    // 메시지를 받았을 때 처리할 작업을 수행할 수 있습니다.
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // 메시지 전달이 완료되었을 때 처리할 작업을 수행할 수 있습니다.
                }
            });
        } else {
            Log.e(TAG, "올바르지 않은 QR 코드 형식");
            Toast.makeText(this, "올바르지 않은 QR 코드 형식입니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendResultToMainActivity(String mqttBroker, String mqttUsername, String mqttPassword) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("mqttBroker", mqttBroker);
        resultIntent.putExtra("mqttUsername", mqttUsername);
        resultIntent.putExtra("mqttPassword", mqttPassword);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void subscribeToTopic(String topic) {
        try {
            IMqttToken token = mqttAndroidClient.subscribe(topic, 0);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    mqttAndroidClient.setCallback(new MqttCallback() {
                        @Override
                        public void connectionLost(Throwable cause) {
                            // 연결이 끊겼을 때 처리
                            Log.e("엠큐티티확인용", "연결 끊김");
                        }

                        @Override
                        public void messageArrived(String topic, MqttMessage message) throws Exception {
                            // 메시지 도착 시 처리
                            Log.v("엠큐티티확인용", "메시지 도착");
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

}