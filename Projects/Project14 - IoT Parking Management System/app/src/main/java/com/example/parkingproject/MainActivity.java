package com.example.parkingproject;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView1=(TextView)findViewById(R.id.textView1);
        TextView textView2=(TextView)findViewById(R.id.textView2);
        TextView textView3=(TextView)findViewById(R.id.textView3);
        TextView textView4=(TextView)findViewById(R.id.textView4);
        TextView textView5=(TextView)findViewById(R.id.textView5);
        TextView textView6=(TextView)findViewById(R.id.textView6);

        //Distance value calculation with ultrasonic sensor
        int distance=10;
        if(distance<=8){
            textView1.setBackgroundColor(Color.parseColor("#FF0000"));
        }
        else{
            textView1.setBackgroundColor(Color.parseColor("#00CC00"));
        }

        if(distance<=8){
            textView2.setBackgroundColor(Color.parseColor("#FF0000"));
        }
        else{
            textView2.setBackgroundColor(Color.parseColor("#00CC00"));
        }

        if(distance<=8){
            textView3.setBackgroundColor(Color.parseColor("#FF0000"));
        }
        else{
            textView3.setBackgroundColor(Color.parseColor("#00CC00"));
        }

        if(distance<=8){
            textView4.setBackgroundColor(Color.parseColor("#FF0000"));
        }
        else{
            textView4.setBackgroundColor(Color.parseColor("#00CC00"));
        }

        if(distance<=8){
            textView5.setBackgroundColor(Color.parseColor("#FF0000"));
        }
        else{
            textView5.setBackgroundColor(Color.parseColor("#00CC00"));
        }

        if(distance<=8){
            textView6.setBackgroundColor(Color.parseColor("#FF0000"));
        }
        else{
            textView6.setBackgroundColor(Color.parseColor("#00CC00"));
        }
    }
}