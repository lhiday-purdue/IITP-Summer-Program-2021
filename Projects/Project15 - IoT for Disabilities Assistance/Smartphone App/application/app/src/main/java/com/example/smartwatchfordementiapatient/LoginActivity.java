package com.example.smartwatchfordementiapatient;
// this is login page
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

class LoginThread extends Thread {

    @Override
    public void run() {
        try{
//            LoginActivity login = new LoginActivity();
            URL url = new URL("http://13.125.120.0:5000/login");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if(urlConnection != null) {
                urlConnection.setConnectTimeout(10000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setDoInput(true);
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setDoOutput(true);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
                bw.write(LoginActivity.jsonb.toString()); // input data(format json)

                bw.flush();
                bw.close();
                int resCode = urlConnection.getResponseCode();
                if(resCode == HttpURLConnection.HTTP_OK){
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String line = null;
                    while(true){
                        line = reader.readLine();
                        if(line==null){
                            break;
                        }
                        if(line.equals("wrong")) {
                            LoginActivity.LOGIN_SUCCESS = 0;
                            break;
                        }else{
                            LoginActivity.userdata = LoginActivity.JSONParsing(line);
                            LoginActivity.LOGIN_SUCCESS=1;
                        }

                    }
                    reader.close();
                }
                urlConnection.disconnect();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}


public class LoginActivity extends AppCompatActivity {

    private EditText username_et;
    private EditText password_et;
    private Button login_btn;
    private Button signin_btn;
    private TextView signup;
    public static JSONObject jsonb;
    public static int LOGIN_SUCCESS;
    public static ArrayList<String> userdata; // for userdata save

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        signup = findViewById(R.id.signup);
        username_et=findViewById(R.id.login_id_et);
        password_et=findViewById(R.id.login_pw_et);
        login_btn=findViewById(R.id.login_bt);

        //click the signup
        signup.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(),SignActivity.class);
                startActivity(intent);
            }
        });
        //click the login button
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //connet to server to get login data...

                //then move to next page
                //move to next page

                jsonb = new JSONObject();
                String id = username_et.getText().toString();
                String pw = password_et.getText().toString();
                LOGIN_SUCCESS = 0;
                try{
                    jsonb.put("id",id);
                    jsonb.put("pw",pw);
                    LoginThread thread = new LoginThread();
                    thread.start();
                    thread.join();
                }catch (Exception e){
                    e.printStackTrace();
                    Log.e("error","wrong login");
                }
                Log.e("LoginActivity:login_success",Integer.toString(LOGIN_SUCCESS));
                if(LoginActivity.LOGIN_SUCCESS==1){
                    Log.e("LoginActivity:success","success");

                    // input some datas in this app.
                    SharedPreference.setAttribute(getApplicationContext(),"id",LoginActivity.userdata.get(0));
                    SharedPreference.setAttribute(getApplicationContext(),"name",LoginActivity.userdata.get(1));
                    SharedPreference.setAttribute(getApplicationContext(),"phone",LoginActivity.userdata.get(2));
                    SharedPreference.setAttribute(getApplicationContext(),"latitude",LoginActivity.userdata.get(3));
                    SharedPreference.setAttribute(getApplicationContext(),"longitude",LoginActivity.userdata.get(4));
                    SharedPreference.setAttribute(getApplicationContext(),"patient_range",LoginActivity.userdata.get(5));

                    Log.d("solve",SharedPreference.getAttribute(getApplicationContext(),"id"));
                    Toast.makeText(LoginActivity.this,"SUCCESS",Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);

                    startActivity(intent);

                    finish();
                }else{
                    Log.e("LoginActivity:fail","fail");
                    Toast.makeText(getApplicationContext(),"FAIL",Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
    //Do jsonparsing
    public static ArrayList<String> JSONParsing(String jsonstring){
        StringBuilder stringBuilder = new StringBuilder();
        ArrayList<String> info = new ArrayList<>();
        try{
            Log.d("json_data",jsonstring);
            JSONObject jsonObject = new JSONObject(jsonstring);

            info.add(jsonObject.getString("id"));
            info.add(jsonObject.getString("name"));
            info.add(jsonObject.getString("phone"));
            info.add(jsonObject.getString("latitude"));
            info.add(jsonObject.getString("longitude"));
            info.add(jsonObject.getString("patient_range"));
        }catch(JSONException e){
            e.printStackTrace();
        }
        return info;
    }


}