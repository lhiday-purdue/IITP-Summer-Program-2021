package com.example.smartwatchfordementiapatient;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

//start activity

public class SplashActivity extends AppCompatActivity {
    Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                //move to next page
                Intent intent;

                if(SharedPreference.getAttribute(getApplicationContext(),"id")==null) {
                    intent = new Intent(getApplicationContext(), LoginActivity.class);
                }else{
                    intent = new Intent(getApplicationContext(), MainActivity.class);
                }
                startActivity(intent);
                finish();

            }
        },2500);
    }
    protected void onPause(){
        super.onPause();
        finish();
    }
}