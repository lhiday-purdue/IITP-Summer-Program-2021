package com.example.smartwatchfordementiapatient;
//Signin page first
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

public class SignActivity extends AppCompatActivity {

    private Button compton_ban;
    private EditText edit_name;
    private EditText edit_pw;
    private EditText edit_id;
    private EditText edit_phone;
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
//                edit_patient_inform = findViewById(R.id.Patient);
                String name = edit_name.getText().toString();
                String pw = edit_pw.getText().toString();
                String id = edit_id.getText().toString();
//                String patient = edit_patient_inform.getText().toString();
                String phone = edit_phone.getText().toString();

                try {

                    Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(),LocationRegisterActivity.class);
                    intent.putExtra("name",name);
                    intent.putExtra("pw",pw);
                    intent.putExtra("id",id);
//                    intent.putExtra("patient",patient);
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