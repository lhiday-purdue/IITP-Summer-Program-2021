package com.example.ultraman7_01;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class MainActivity extends AppCompatActivity {
    private MqttAndroidClient mqttAndroidClient;
    private Button button;
    private TextView tv_output;
    private TextView tv_output2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button)findViewById(R.id.button);
        tv_output = (TextView)findViewById(R.id.tv_output);
        tv_output2 = (TextView)findViewById(R.id.tv_output2);
//143.198.112.40 , tcp://test.mosquitto.org:1883
        mqttAndroidClient = new MqttAndroidClient(this,  "tcp://"+"143.198.112.40"+":8883","test");
        // 2번째 파라메터 : 브로커의 ip 주소 , 3번째 파라메터 : client 의 id를 지정함 여기서는 paho 의 자동으로 id를 만들어주는것
        try {
            IMqttToken token = mqttAndroidClient.connect(getMqttConnectionOption());    //mqtttoken 이라는것을 만들어 connect option을 달아줌
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    mqttAndroidClient.setBufferOpts(getDisconnectedBufferOptions());    //연결에 성공한경우
                    Log.e("Connect_success", "Success");
                    tv_output.setText("Connect_success");
                    tv_output2.setText("Connect_success");
                    try {
                        mqttAndroidClient.subscribe("myled1", 0 );             //연결에 성공하면 myled1 라는 토픽으로 subscribe함
                        Log.e("subscribe1","myled1");
                        mqttAndroidClient.subscribe("myled2", 0 );             //연결에 성공하면 myled2 라는 토픽으로 subscribe함
                        Log.e("subscribe2","myled2");
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {   //연결에 실패한경우
                    Log.e("connect_fail", "Failure " + exception.toString());
                }
            });
        } catch (
                MqttException e)
        {
            e.printStackTrace();

        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //mqttAndroidClient.publish("soyeonLee", "Hello , my name is soyeon Lee !".getBytes(), 0 , false );
                    //mqttAndroidClient.publish("myled1", "TurnOn".getBytes(), 0 , false );
                    mqttAndroidClient.publish("myled2", "TurnOFF".getBytes(), 0 , false );
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });

        mqttAndroidClient.setCallback(new MqttCallback() {  //클라이언트의 콜백을 처리하는부분
            @Override
            public void connectionLost(Throwable cause) {
            }
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {    //모든 메시지가 올때 Callback method
                String msg = new String(message.getPayload());
                Log.e("arrive message ", msg);

                if (topic.equals("myled1")){     //topic 별로 분기처리하여 작업을 수행할수도있음
                    if(msg.equals("TurnOn"))
                        //tv_ouput.setText("Arrive message : " + msg);
                        tv_output.setText("Turn On myled1");
                    else if(msg.equals("TurnOFF"))
                        tv_output.setText("Turn Off myled1");

                    else
                        tv_output.setText(msg);
                }
                if (topic.equals("myled2")){
                    if(msg.equals("TurnOn"))
                        tv_output2.setText("TurnOn myled2");
                    else if(msg.equals("TurnOFF"))
                        tv_output2.setText("TurnOff myled2");

                    else
                        tv_output2.setText(msg);
                }
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });
    }

    private DisconnectedBufferOptions getDisconnectedBufferOptions() {
        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
        disconnectedBufferOptions.setBufferEnabled(true);
        disconnectedBufferOptions.setBufferSize(100);
        disconnectedBufferOptions.setPersistBuffer(true);
        disconnectedBufferOptions.setDeleteOldestMessages(false);
        return disconnectedBufferOptions;
    }



    private MqttConnectOptions getMqttConnectionOption() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setWill("off", "I am going offline".getBytes(), 1, true);
        return mqttConnectOptions;
    }
}