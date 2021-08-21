package com.example.parkingproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends Activity {
    Handler handler=new Handler();
    Runnable r=new Runnable() {
        @Override
        public void run() {
            Intent intent=new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);       //메인 화면으로 전환
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro);      //xml과 java 소스 연결
    }

    @Override
    protected void onResume(){
        super.onResume();
        handler.postDelayed(r,3000);
    }

    @Override
    protected void onPause(){
        super.onPause();
        handler.removeCallbacks(r);
    }
}
