package com.example.smartwatchfordementiapatient;
// This page for changing patient safe locate
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


class LocationUpdate extends Thread {
    @Override
    public void run() {
        try{
            URL url = new URL("http://13.125.120.0:5000/update-locate");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if(urlConnection != null) {
                urlConnection.setConnectTimeout(10000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setDoInput(true);
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setDoOutput(true);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
                bw.write(LocationSettingActivity.jsonb.toString());
                Log.e("check",LocationSettingActivity.jsonb.toString());
                bw.flush();
                bw.close();

                //get data from the server
                int resCode = urlConnection.getResponseCode();
                if(resCode == HttpURLConnection.HTTP_OK){
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String line = null;
                    while(true){
                        line = reader.readLine();
                        if(line == null)
                            break;
                    }
                    reader.close();
                }
                urlConnection.disconnect();
                sendOutofRangeAPI();
            }
        }catch(Exception e){
            e.printStackTrace();
            Log.e("wrong",String.valueOf(e));
        }
    }

    //when patient is out of range
    // send patient is out or back to smart watch
    private void sendOutofRangeAPI(){
        try{
            URL url = new URL("http://13.125.120.0:5000/update-away");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if(urlConnection != null) {
                urlConnection.setConnectTimeout(10000); // wait until 10seconds
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setDoInput(true);
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setDoOutput(true); // push data

                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
                bw.write(LocationSettingActivity.jsonb.toString()); // input data
                bw.flush();
                bw.close();
                int resCode = urlConnection.getResponseCode();
                if(resCode == HttpURLConnection.HTTP_OK){
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String line = null;
                    while(true){
                        line = reader.readLine();
                        if(line == null)
                            break;
                    }
                    reader.close();
                }
                urlConnection.disconnect();
            }
        }catch(Exception e){
            e.printStackTrace();
            Log.e("wrong",String.valueOf(e));
        }
    }

}



public class LocationSettingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private int radius=1000; // patinet_range

    private EditText radius_et;
    private Button radius_ok_btn;
    private TextView radius_result;
    private RadioGroup search_radioGroup;
    private RadioButton google;
    private RadioButton daum;
    private String search_setting="google";

    private String range_unit="m";
    private RadioGroup radioGroup;
    private RadioButton radio300;
    private RadioButton radio500;
    private RadioButton radio1000;

    private EditText edit_addr;
    private ImageButton search_btn;
    private Button korea_address;
    public static JSONObject jsonb;

    private GoogleMap mMap;
    private Button complete_btn;
    private Button cancel_btn;

    private double selected_latitude;
    private double selected_longtitude;
    private static final int SEARCH_ADDRESS_ACTIVITY = 10000;
    private static final int ADDRESS_LIST_SELECTED = 10001;
    private TextView address_tv;
    private ImageButton back_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_setting);

        //radio group setting
        search_radioGroup=findViewById(R.id.search_radiogroup);
        search_radioGroup.setOnCheckedChangeListener(radioGroupButtonChangeListener1);
        google=findViewById(R.id.google_radio);
        daum=findViewById(R.id.daum_radio);
        radius = Integer.parseInt(SharedPreference.getAttribute(getApplicationContext(),"patient_range"));
        selected_latitude=Double.parseDouble(SharedPreference.getAttribute(getApplicationContext(),"latitude"));
        selected_longtitude=Double.parseDouble(SharedPreference.getAttribute(getApplicationContext(),"longitude"));


        radius_et=findViewById(R.id.range_et); //범위 입력받기
        radius_result=findViewById(R.id.range_tv); //입력받은 범위
        radio300=findViewById(R.id.radio_300);
        radio500=findViewById(R.id.radio_500);
        radioGroup=findViewById(R.id.radiogroup);
        radioGroup.setOnCheckedChangeListener(radioGroupButtonChangeListener);

        radius_ok_btn=findViewById(R.id.ok_btn);

        radius_ok_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                radius=Integer.parseInt(radius_et.getText().toString());
                radius_result.setText("Range: "+radius+range_unit);

                if(range_unit=="km"){
                    radius*=1000;
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(selected_latitude, selected_longtitude),13));
                }
                else{
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(selected_latitude, selected_longtitude),14));
                }

                mMap.clear();
                MarkerOptions mOptions = new MarkerOptions();
                mOptions.position(new LatLng(selected_latitude, selected_longtitude));
                CircleOptions circle = new CircleOptions().center(new LatLng(selected_latitude, selected_longtitude)) //latitude & longitude of point
                        .radius(radius)      //radius unit : m
                        .strokeWidth(0f)  //line width -> 0f = no line
                        .fillColor(Color.parseColor("#885b9fde")); //background color

                mMap.addMarker(mOptions);
                mMap.addCircle(circle);

            }
        });

        edit_addr = findViewById(R.id.address_search_et);
        search_btn = findViewById(R.id.search_btn);
        Geocoder geocoder = new Geocoder(this);
        korea_address = findViewById(R.id.korea_search);
        address_tv=findViewById(R.id.address_tv);
        //check location permission
        String addr=getCurrentAddress(selected_latitude,selected_longtitude);
        address_tv.setText(addr);
        radius_et.setText(Integer.toString(radius));
        back_btn=findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        search_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(search_setting=="google"){ //search with google map
                    List<Address> list = null;
                    String str = edit_addr.getText().toString();
                    TextView tv = findViewById(R.id.address_tv);
                    if (str != "") {
                        try {
                            list = geocoder.getFromLocationName(
                                    str,
                                    10);
                            Log.e("test", Integer.toString(list.size()));
                        } catch (IOException e) {
                            e.printStackTrace();

                        }
                        if (list != null) {
                            if (list.size() == 0) {
                                tv.setText("No Adress information");
                                mMap.clear();
                            } else {

                                ArrayList<Locate> address_list = new ArrayList<>();
                                for(Address temp : list){
                                    address_list.add(new Locate(temp.getLatitude(),temp.getLongitude()));
                                }
                                Intent intent = new Intent(getApplicationContext(), AddressList.class);
                                intent.putExtra("datalist", address_list);
                                overridePendingTransition(0, 0);
                                startActivityForResult(intent,ADDRESS_LIST_SELECTED);


                            }

                        }
                    }
                }else{ //search with daum address api
                    int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
                    if(status == NetworkStatus.TYPE_MOBILE || status == NetworkStatus.TYPE_WIFI) {

                        Intent i = new Intent(getApplicationContext(), AddressApiActivity.class);
                        // erase animation
                        overridePendingTransition(0, 0);
                        // result
                        startActivityForResult(i, SEARCH_ADDRESS_ACTIVITY);

                    }else {
                        Toast.makeText(getApplicationContext(), "Check network connection.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        //click complete button
        complete_btn=findViewById(R.id.complete_btn);
        //do update the data
        complete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //connet to server to send location data..
                //then move to next page
                //move to next page

                jsonb = new JSONObject();
                try{
                    jsonb.put("id",SharedPreference.getAttribute(getApplicationContext(),"id"));
                    jsonb.put("selected_latitude",selected_latitude);
                    jsonb.put("selected_longitude",selected_longtitude);
                    jsonb.put("range",radius);
                    jsonb.put("is_patient_away",false);
//                    jsonb.put("is_patient_away",false);

                    LocationUpdate thread = new LocationUpdate();
                    thread.start();
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"wrong signup",Toast.LENGTH_SHORT).show();
                }
                //change data in SharedPreference(in app)
                //remove
                SharedPreference.removeAttribute(getApplicationContext(),"latitude");
                SharedPreference.removeAttribute(getApplicationContext(),"longitude");
                SharedPreference.removeAttribute(getApplicationContext(),"patient_range");
                //input data
                SharedPreference.setAttribute(getApplicationContext(),"latitude",Double.toString(selected_latitude));
                SharedPreference.setAttribute(getApplicationContext(),"longitude",Double.toString(selected_longtitude));
                SharedPreference.setAttribute(getApplicationContext(),"patient_range",Integer.toString(radius));
                Toast.makeText(getApplicationContext(), "complete modify location", Toast.LENGTH_SHORT).show();
                RealService.patient_in = true;
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();

            }
        });
        //cancel button push
        cancel_btn=findViewById(R.id.cancel_btn);
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "cancel location setting", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // touch on map -> change mark point
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
            @Override
            public void onMapClick(LatLng point) {
                MarkerOptions mOptions = new MarkerOptions();
                // marker title
                mOptions.title("");
                mMap.clear();

                Double latitude = point.latitude; // latitude
                Double longitude = point.longitude; // longitude
                selected_latitude=latitude;
                selected_longtitude=longitude;
                //change location -> address
                String addr=getCurrentAddress(selected_latitude,selected_longtitude);
                address_tv.setText(addr);

                // setting marker
                mOptions.snippet(latitude + ", " + longitude);
                // LatLng: pair of(latitude, longitude)
                mOptions.position(new LatLng(latitude, longitude));

                // range 1km
                CircleOptions circle1KM = new CircleOptions().center(new LatLng(latitude, longitude)) //latitude & longitude of point
                        .radius(radius)      //radius unit : m
                        .strokeWidth(0f)  //line width -> 0f = no line
                        .fillColor(Color.parseColor("#885b9fde")); //background color

                // add marker
                googleMap.addMarker(mOptions);
                googleMap.addCircle(circle1KM);

            }
        });

        //latitude, longitude should be changed to current location
        LatLng seoul = new LatLng(selected_latitude, selected_longtitude);
        mMap.addMarker(new MarkerOptions().position(seoul).title("seoul"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(seoul));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul,14));
        CircleOptions circle1KM = new CircleOptions().center(seoul) //latitude & longitude of point
                .radius(radius)      //radius unit : m
                .strokeWidth(0f)  //line width -> 0f = no line
                .fillColor(Color.parseColor("#885b9fde")); //background color

        mMap.addCircle(circle1KM);


    }
    //radio group click listener
    RadioGroup.OnCheckedChangeListener radioGroupButtonChangeListener1 = new RadioGroup.OnCheckedChangeListener() {
        @Override public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
            if(i == R.id.google_radio){
                search_setting="google";
            } else if(i == R.id.daum_radio){
                search_setting = "daum";
                edit_addr.setText("");
            }
        }
    };
    RadioGroup.OnCheckedChangeListener radioGroupButtonChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
            if(i == R.id.radio_300){
                //m
                range_unit="m";

            } else if(i == R.id.radio_500){
                //km
                range_unit="km";
            }
        }
    };

    //geocoder : longtitude, latitude <-> address
    public String getCurrentAddress( double latitude, double longitude) {

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 7);
        } catch (IOException ioException) {
            //net work error
            return "geocorder not service";
        } catch (IllegalArgumentException illegalArgumentException) {
            return "wrong gps location";
        }
        if (addresses == null || addresses.size() == 0) {
            return "couldn't search address";
        }

        Address address = addresses.get(0);
        String addr=address.getAddressLine(0);
//        addr=addr.substring(4);
        //return address.getAddressLine(0).toString()+"\n";
        return addr;
    }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SEARCH_ADDRESS_ACTIVITY:
                Geocoder geocoder = new Geocoder(this);
                if (resultCode == RESULT_OK) {
                    String address = data.getExtras().getString("data");
                    if (address != null) {
                        Log.i("test", "data:" + address);
                        edit_addr.setText(address);
                        List<Address> list = null;
                        String str = edit_addr.getText().toString();
                        TextView tv = findViewById(R.id.address_tv);
                        try{
                            list = geocoder.getFromLocationName(
                                    str,
                                    10);
                            Log.e("test",Integer.toString(list.size()));
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                        Toast.makeText(getApplicationContext(),Integer.toString(list.size()),Toast.LENGTH_SHORT).show();
                        if(list!=null) {
                            if (list.size() == 0) {
                                tv.setText("No Adress information");
                                mMap.clear();
                            } else {

                                tv.setText(list.get(0).getAddressLine(0));
                                selected_latitude = list.get(0).getLatitude();
                                selected_longtitude = list.get(0).getLongitude();
                                mMap.clear();
                                MarkerOptions mOptions = new MarkerOptions();
                                mOptions.position(new LatLng(selected_latitude, selected_longtitude));
                                CircleOptions circle = new CircleOptions().center(new LatLng(selected_latitude, selected_longtitude)) //latitude & longitude of point
                                        .radius(radius)      //radius unit : m
                                        .strokeWidth(0f)  //line width -> 0f = no line
                                        .fillColor(Color.parseColor("#885b9fde")); //background color

                                mMap.addMarker(mOptions);
                                mMap.addCircle(circle);
                                LatLng newArea = new LatLng(selected_latitude, selected_longtitude);
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(newArea));
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newArea, 14));

                            }
                        }

                    }
                }
                break;
            case ADDRESS_LIST_SELECTED:
                Geocoder geocoder1 = new Geocoder(this);
                if(resultCode==RESULT_OK){
                    String text = data.getExtras().getString("data");
                    if(text!=""){
                        edit_addr.setText(text);
                        List<Address> list = null;
                        TextView tv = findViewById(R.id.address_tv);
                        try{
                            list = geocoder1.getFromLocationName(
                                    text,
                                    10);
                            Log.e("test",Integer.toString(list.size()));
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                        //Toast.makeText(getApplicationContext(),Integer.toString(list.size()),Toast.LENGTH_SHORT).show();
                        if(list!=null) {
                            if (list.size() == 0) {
                                tv.setText("No Adress information");
                                mMap.clear();
                            } else {

                                tv.setText(list.get(0).getAddressLine(0));
                                selected_latitude = list.get(0).getLatitude();
                                selected_longtitude = list.get(0).getLongitude();
                                mMap.clear();
                                MarkerOptions mOptions = new MarkerOptions();
                                mOptions.position(new LatLng(selected_latitude, selected_longtitude));
                                CircleOptions circle = new CircleOptions().center(new LatLng(selected_latitude, selected_longtitude)) //latitude & longitude of point
                                        .radius(radius)      //radius unit : m
                                        .strokeWidth(0f)  //line width -> 0f = no line
                                        .fillColor(Color.parseColor("#885b9fde")); //background color

                                mMap.addMarker(mOptions);
                                mMap.addCircle(circle);
                                LatLng newArea = new LatLng(selected_latitude, selected_longtitude);
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(newArea));
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newArea, 14));

                            }
                        }
                    }
                }
        }
    }



}