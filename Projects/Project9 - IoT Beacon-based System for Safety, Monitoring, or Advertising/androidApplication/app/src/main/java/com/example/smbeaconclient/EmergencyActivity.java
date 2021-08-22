package com.example.smbeaconclient;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class EmergencyActivity extends AppCompatActivity {
    ImageView imageViewEvac;
    TextView tv1, tv2, tvFloorUser, tvFloorFire, tvTitle;
    private String TAG = "EmergencyActivityLog";
    String imageUri, people1f, people2f, floorUser, floorFire;

    Animation animTitle, animData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        Log.d(getClass().getSimpleName(), "onCreate");

        // Show activity even in lock screen state
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        //init view
        imageViewEvac = findViewById(R.id.imageViewEvac);
        tv1 = findViewById(R.id.tv1);
        tv2 = findViewById(R.id.tv2);
        tvFloorUser = findViewById(R.id.tvUserFloor);
        tvFloorFire = findViewById(R.id.tvFloorFire);
        tvTitle = findViewById(R.id.tvTitle);

        //init title anim
        animTitle = new AlphaAnimation(0.0f,1.0f);
        animTitle.setDuration(200);
        animTitle.setStartOffset(20);
        animTitle.setRepeatMode(Animation.REVERSE);
        animTitle.setRepeatCount(Animation.INFINITE);
        tvTitle.setAnimation(animTitle);

        //init data anim
        animData = new AlphaAnimation(0.0f,0.8f);
        animData.setDuration(600); //100 = 1 second
        animData.setStartOffset(20);

        //get intent
        Intent intent = getIntent();
        setTextView(intent);

    }
    public void setTextView(Intent intent) {
        imageUri = intent.getStringExtra("imageUri");
        floorUser = intent.getStringExtra("floorUser");
        floorFire = intent.getStringExtra("floorFire");
        people1f = intent.getStringExtra("people1f");
        people2f = intent.getStringExtra("people2f");

        Glide.with(EmergencyActivity.this)
                .load(imageUri)
                .fallback(R.drawable.app_icon)
                .error(R.drawable.app_icon)
                .into(imageViewEvac);

        tvFloorUser.setText(floorUser);
        tv1.setText(people1f);
        tv2.setText(people2f);
        tvFloorFire.setText(floorFire);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent called");
        super.onNewIntent(intent);
        setIntent(intent);

        setTextView(intent);

        //setAnimation
        tvFloorUser.startAnimation(animData);
        tv1.startAnimation(animData);
        tv2.startAnimation(animData);
        tvFloorFire.startAnimation(animData);
    }

}