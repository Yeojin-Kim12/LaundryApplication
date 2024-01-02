    package com.example.projectdyy;

    import android.content.Intent;
    import android.os.Bundle;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ProgressBar;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.fragment.app.Fragment;

    import org.eclipse.paho.android.service.MqttAndroidClient;
    import org.eclipse.paho.client.mqttv3.IMqttActionListener;
    import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
    import org.eclipse.paho.client.mqttv3.IMqttToken;
    import org.eclipse.paho.client.mqttv3.MqttCallback;
    import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
    import org.eclipse.paho.client.mqttv3.MqttMessage;

    import java.nio.charset.StandardCharsets;

    /**
     * A simple {@link Fragment} subclass.
     * Use the {@link Fragment1#newInstance} factory method to
     * create an instance of this fragment.
     */
    public class Fragment1 extends Fragment {

        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private static final String ARG_PARAM1 = "param1";
        private static final String ARG_PARAM2 = "param2";
        private MqttAndroidClient mqttClient;
        private double previousPower = 0;
        private boolean isViewEnabled = true;
        double currentPower = 0;
        int washing = 0;
        private ProgressBar progressBarHorizontal;

        // TODO: Rename and change types of parameters`
        private String mParam1;
        private String mParam2;
        TextView status;

        String mqttBroker = "";
        String mqttUsername = "";
        String mqttPassword = "";

        public Fragment1() {
            // Required empty public constructor
        }

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Fragment1.
         */
        // TODO: Rename and change types and number of parameters
        public static Fragment1 newInstance(String param1, String param2) {
            Fragment1 fragment = new Fragment1();
            Bundle args = new Bundle();
            args.putString(ARG_PARAM1, param1);
            args.putString(ARG_PARAM2, param2);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mParam1 = getArguments().getString(ARG_PARAM1);
                mParam2 = getArguments().getString(ARG_PARAM2);
            }
            connectToMQTTBroker();
        }

        public void setMQTTInfo(String mqttBroker, String mqttUsername, String mqttPassword) {
            // MQTT 정보를 처리
            this.mqttBroker = mqttBroker;
            this.mqttUsername = mqttUsername;
            this.mqttPassword = mqttPassword;
            Log.d("프래그먼트","프래그먼트 mqtt");

            // MQTT 연결을 다시 수행
            connectToMQTTBroker();
        }

        private void connectToMQTTBroker() {
            mqttClient = new MqttAndroidClient(getActivity().getApplicationContext(), mqttBroker, "android-client");
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setUserName(mqttUsername);
            options.setPassword(mqttPassword.toCharArray());
            Log.d("프래그먼트","프래그먼트 mqtt: "+mqttBroker);
            try {
                IMqttToken token = mqttClient.connect(options);
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        subscribeToTopic("status/topic");
                        //subscribeToTopic("first/topic");
                        subscribeToTopic("Tenminutes");
                        Log.v("엠큐티티 프래그", "mqtt 성공");
                        //FirstMessage();
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        exception.printStackTrace();
                        Log.e("엠큐티티 프래그", "mqtt 실패" + exception.getMessage(), exception);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    //    private void FirstMessage() {
    //        String topic = "first/topic";
    //        try {
    //            Log.v("프래그먼트 메시지 전달", "1번 메시지 전달 코드까지 옴");
    //            MqttMessage mqttMessage = new MqttMessage();
    //            mqttClient.publish(topic, mqttMessage);
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //        }
    //    }

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
                                Log.v("엠큐티티 프래그먼트 확인", "메시지 도착: "+payload);
                                // 10분 후 view 비활성화
                                if (payload == "off"){
                                    if (isViewEnabled) {
                                        isViewEnabled = false;
                                        if (getView() != null) {
                                            getView().setClickable(false);
                                        }
                                    }
                                } else {
                                    currentPower = Double.parseDouble(payload);
                                    Log.v("엠큐티티 프래그먼트 확인", "메시지 도착: "+currentPower);
                                }
                                updateProgressBar(currentPower);
                                if (currentPower >= 2 && washing == 0){
                                    status.setText("탈수");
                                    washing = 1;
                                }
                                if (currentPower > previousPower && currentPower >= 60) {
                                    if (status.getText() == "헹굼"){
                                        status.setText("탈수");
                                    }else{
                                        if (status.getText() == "세탁"){
                                            status.setText("헹굼");
                                        }
                                    }
                                }
                                if (currentPower < previousPower && currentPower == 0){
                                    status.setText("종료");
                                    publishMessage();
                                }
                                previousPower = currentPower;

                            }

                            @Override
                            public void deliveryComplete(IMqttDeliveryToken token) {
                                // 메시지 전달 완료 시 처리
                                Log.v("엠큐티티 프래그먼트 확인", "메시지 전달 완료함");
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

        private void updateProgressBar(double power) {
            // 프로그레스바 업데이트 로직
            // 1W 이상이면 프로그레스바를 10씩 늘리기
            if (power >= 1) {
                int currentProgress = progressBarHorizontal.getProgress();
                progressBarHorizontal.setProgress(currentProgress+12);
            }
        }

        private void publishMessage() {
            String topic = "Tenminutes";
            try {
                Log.v("프래그먼트 메시지 전달", "메시지 전달 코드까지 옴");
                MqttMessage mqttMessage = new MqttMessage();
                mqttClient.publish(topic, mqttMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            //return inflater.inflate(R.layout.fragment_1, container, false);

            View view = inflater.inflate(R.layout.fragment_1, container, false);

            status = view.findViewById(R.id.w_t);
            progressBarHorizontal = view.findViewById(R.id.progressBarHorizontal);
            if (isViewEnabled) {
                // 프레그먼트 내 레이아웃 클릭 이벤트 처리
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 액티비티로 화면 전환하기
                        Intent intent = new Intent(getActivity(), lock.class);

                        intent.putExtra("mqttBroker", mqttBroker);
                        intent.putExtra("mqttUsername", mqttUsername);
                        intent.putExtra("mqttPassword", mqttPassword);
                        startActivity(intent);
                    }
                });
            }else {
                // 뷰가 비활성화되었으면 클릭 이벤트를 처리하지 않도록 설정
                view.setClickable(false);
            }
            connectToMQTTBroker();
            return view;
        }
    }