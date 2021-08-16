package com.example.smartwatchfordementiapatient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

//class RequestThread extends Thread {
//    @Override
//    public void run() {
//        try{
//            URL url = new URL("http://3.35.149.182:5000/signup");
//            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//            if(urlConnection != null) {
//                urlConnection.setConnectTimeout(10000); // 10초 동안 기다린 후 응답이 없으면 종료
//                urlConnection.setRequestMethod("POST");
//                urlConnection.setRequestProperty("Content-Type", "application/json");
//                urlConnection.setDoInput(true);
//                urlConnection.setChunkedStreamingMode(0);
//                urlConnection.setDoOutput(true); // 데이터 전송
//                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
//                bw.write(SignActivity.jsonb.toString());
//                Log.e("확인",SignActivity.jsonb.toString());
//                bw.flush();
//                bw.close();
//
//                //서버 내용 수신 받기
//                int resCode = urlConnection.getResponseCode();
//                if(resCode == HttpURLConnection.HTTP_OK){
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
//                    String line = null;
//                    while(true){
//                        line = reader.readLine();
//                        if(line == null)
//                            break;
//                        Log.d("asdddddddddddd",line);
//                    }
//                    reader.close();
//                }
//                urlConnection.disconnect();
//            }
//        }catch(Exception e){
//            e.printStackTrace();
//            Log.e("wrong",String.valueOf(e));
//        }
//    }
//}

public class SignActivity extends AppCompatActivity {

    private Button compton_ban;
    private EditText edit_name;
    private EditText edit_pw;
    private EditText edit_id;
    private EditText edit_phone;
    private EditText edit_patient_inform ;
//    public static JSONObject jsonb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);


        compton_ban = findViewById(R.id.signup_btn);
        compton_ban.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                edit_name = findViewById(R.id.name);
                edit_pw = findViewById(R.id.PW);
                edit_id = findViewById(R.id.ID);
                edit_phone = findViewById(R.id.phone);
                edit_patient_inform = findViewById(R.id.Patient);
                String name = edit_name.getText().toString();
                String pw = edit_pw.getText().toString();
                String id = edit_id.getText().toString();
                String patient = edit_patient_inform.getText().toString();
                String phone = edit_phone.getText().toString();
//                jsonb = new JSONObject();
                try {
//                    jsonb.put("name", name);
//                    jsonb.put("pw", pw);
//                    jsonb.put("id",id);
//                    jsonb.put("patient",patient);
//                    jsonb.put("phone",phone);

//                    RequestThread thread = new RequestThread();
//                    thread.start();
                    Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(),LocationRegisterActivity.class);
                    intent.putExtra("name",name);
                    intent.putExtra("pw",pw);
                    intent.putExtra("id",id);
                    intent.putExtra("patient",patient);
                    intent.putExtra("phone",phone);

                    startActivity(intent);
                    finish();

                }catch(Exception e){
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"wrong signup",Toast.LENGTH_SHORT).show();
                }


            }
        });







    }
}