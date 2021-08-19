package com.example.ultraman7_04;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    Button button;
    TextView textView;
    TextView tv_url;

    String device = "";
    String value = "";
    String time = "";
    JSONObject jsonObject = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button)findViewById(R.id.bt_http);
        textView = (TextView)findViewById(R.id.tv_http);
        tv_url = (EditText)findViewById(R.id.tv_url);

        jsonObject = new JSONObject();
        try{
            jsonObject.put("count", "2");
            jsonObject.put("next",null);
            jsonObject.put("previous",null);

            JSONObject result_data = new JSONObject();
            result_data.put("device","water");
            result_data.put("value","0.1");
            result_data.put("time","2021-08-03");

            jsonObject.put("results", result_data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendRequest();
            }
        });
        if(AppHelper.requestQueue != null){
            AppHelper.requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
    }
    public void sendRequest() {
        String url = tv_url.getText().toString();
        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json = new JSONObject(response);
                            JSONArray results = json.getJSONArray("results");
                            println(results.toString());
                            for(int i = 0; i<results.length();i++) {
                                JSONObject tmp = (JSONObject)results.get(i);
                                device = (String)tmp.get("device");
                                value = (String)tmp.get("value");
                                time = (String)tmp.get("time");
                            }
                            String data = "device : " + device + "\nvalue : "+value +"\ntime : " +time;
                            println(data);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        println("Error -> " + error.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();
                return params;
            }
        };
        request.setShouldCache(false);
        AppHelper.requestQueue = Volley.newRequestQueue(this);
        AppHelper.requestQueue.add(request);
        println("send request");




    }
    public static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
        Map<String,Object> retMap = new HashMap<String,Object>();

        if(json != JSONObject.NULL) {
            retMap = toMap(json);
        }
        return retMap;
    }

    private static Map<String,Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keys();
        while(keysItr.hasNext()){
            String key = keysItr.next();
            Object value = object.get(key);

            if(value instanceof JSONArray){
                value = toList((JSONArray)value);
            }
            else if(value instanceof JSONObject){
                value = toMap((JSONObject)value);
            }
            map.put(key, value);
        }
        return map;
    }
    private static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for(int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }



    public void println(String data) {
        textView.setText(data +"\n");
    }
    public static class AppHelper{
        public static RequestQueue requestQueue;
    }
}