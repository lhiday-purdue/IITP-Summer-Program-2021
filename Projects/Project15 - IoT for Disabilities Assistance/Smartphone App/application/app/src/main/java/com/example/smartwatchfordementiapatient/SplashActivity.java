package com.example.smartwatchfordementiapatient;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {

                    // For device above MarshMallow
                    boolean permission = getWritePermission();
                    if(permission) {
                        // If permission Already Granted
                        // Send You SMS here

                        Log.e("SplashActivity:permission","sms permission");
                    }
                }
                else{
                    // Send Your SMS. You don't need Run time permission
                }
                finish();

            }
        },2500);
    }
    public boolean getWritePermission(){
        boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED);
//        Toast.makeText(getApplicationContext(),Boolean.toString(hasPermission),Toast.LENGTH_SHORT).show();
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 10);
        }
        return hasPermission;
    }
    protected void onPause(){
        super.onPause();
        finish();
    }
}