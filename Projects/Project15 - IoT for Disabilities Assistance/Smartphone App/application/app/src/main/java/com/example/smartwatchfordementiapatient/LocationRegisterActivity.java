package com.example.smartwatchfordementiapatient;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
//this page is for location Register
// It will work in after input user information
class RequestThread extends Thread {
    @Override
    public void run() {
        try{
            URL url = new URL("http://13.125.120.0:5000/signup");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if(urlConnection != null) {
                urlConnection.setConnectTimeout(10000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setDoInput(true);
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setDoOutput(true);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
                bw.write(LocationRegisterActivity.jsonb.toString());
                Log.e("확인",LocationRegisterActivity.jsonb.toString());
                bw.flush();
                bw.close();

                //get server data
                int resCode = urlConnection.getResponseCode();
                if(resCode == HttpURLConnection.HTTP_OK){
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String line = null;
                    while(true){
                        line = reader.readLine();
                        if(line == null)
                            break;
                        Log.d("LocationRegisterActivity:getdata",line);
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

public class LocationRegisterActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GpsTracker gpsTracker;
    private GoogleMap mMap;
    private Button complete_btn;
    private TextView address_tv;

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
    //initial location is seoul
    private double selected_latitude=37.56638872588792;
    private double selected_longtitude=126.97800947033107;

    public static JSONObject jsonb;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final int SEARCH_ADDRESS_ACTIVITY = 10000;
    private static final int ADDRESS_LIST_SELECTED = 10001;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_registration);
        //get Intent from LoginActivity
        Intent loginintent = getIntent();
        String name = loginintent.getStringExtra("name");
        String id = loginintent.getStringExtra("id");
        String pw = loginintent.getStringExtra("pw");
        String phone = loginintent.getStringExtra("phone");
        String patient = loginintent.getStringExtra("patient");

        //radio group setting
        search_radioGroup=findViewById(R.id.search_radiogroup);
        search_radioGroup.setOnCheckedChangeListener(radioGroupButtonChangeListener1);
        google=findViewById(R.id.google_radio);
        daum=findViewById(R.id.daum_radio);

        radius_et=findViewById(R.id.range_et);
        radius_result=findViewById(R.id.range_tv);
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
                    radius*=1000; //change km to meter
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

        //get current locate
        gpsTracker = new GpsTracker(getApplicationContext());
        selected_latitude = gpsTracker.getLatitude();
        selected_longtitude= gpsTracker.getLongitude();

        search_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(search_setting=="google"){ //searching locate by google data
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
                }else{ //search with daum api (only for korea)
                    int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
                    if(status == NetworkStatus.TYPE_MOBILE || status == NetworkStatus.TYPE_WIFI) {
                        Intent i = new Intent(getApplicationContext(), AddressApiActivity.class);
                        // erase animation
                        overridePendingTransition(0, 0);

                        startActivityForResult(i, SEARCH_ADDRESS_ACTIVITY);
                    }else {
                        Toast.makeText(getApplicationContext(), "Please Check Network", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        address_tv=findViewById(R.id.address_tv);
        //check location permission
        if(checkRunTimePermission()){
            String addr=getCurrentAddress(selected_latitude,selected_longtitude);
            address_tv.setText(addr);
        }


        //click complete button
        complete_btn=findViewById(R.id.complete_btn);
        complete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //connet to server to send location data..
                //then move to next page
                //move to next page

                jsonb = new JSONObject();
                try{
                    jsonb.put("name", name);
                    jsonb.put("pw", pw);
                    jsonb.put("id",id);
//                    jsonb.put("patient",patient);
                    jsonb.put("phone",phone);
                    jsonb.put("selected_latitude",selected_latitude);
                    jsonb.put("selected_longitude",selected_longtitude);
                    jsonb.put("range",radius);
                    RequestThread thread = new RequestThread();
                    thread.start();
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"wrong signup",Toast.LENGTH_SHORT).show();
                }

                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
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
                //Toast.makeText(getApplicationContext(), ""+latitude+" "+longitude, Toast.LENGTH_SHORT).show();
                String addr=getCurrentAddress(selected_latitude,selected_longtitude);
                address_tv.setText(addr);

                // marker's snippet
                mOptions.snippet(latitude + ", " + longitude);
                mOptions.position(new LatLng(latitude, longitude));

                // range 1Km
                CircleOptions circle = new CircleOptions().center(new LatLng(latitude, longitude)) //latitude & longitude of point
                        .radius(radius)      //radius unit : m
                        .strokeWidth(0f)  //line width -> 0f = no line
                        .fillColor(Color.parseColor("#885b9fde")); //background color

                // add marker
                googleMap.addMarker(mOptions);
                googleMap.addCircle(circle);

            }
        });

        //latitude, longitude should be changed to current location
        LatLng seoul = new LatLng(selected_latitude, selected_longtitude);
        mMap.addMarker(new MarkerOptions().position(seoul).title("seoul"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(seoul));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul,14));


        CircleOptions circle = new CircleOptions().center(seoul) //latitude & longitude of point
                .radius(radius)      //radius unit : m
                .strokeWidth(0f)  //line width -> 0f = no line
                .fillColor(Color.parseColor("#885b9fde")); //background color

        mMap.addCircle(circle);

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

        //geocoder
        // change gps to address
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 7);
        } catch (IOException ioException) {
            //network issue
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

    //Gps activation like get permission
    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public boolean checkRunTimePermission(){
        //do runtime permission
        // 1. Check whether user has locate permission
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. If user already has permission
            // under version 6.0, they have no permission so it's okay
            // 3.  get location
            return true;
        } else {  //2. if user don't allow permission request, then need permission request. Two types(3-1.4-1)
            // 3-1.  If the user has refused to perform a permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(LocationRegisterActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. Before proceeding with the request, you need to explain to the user why the permission is required
                Toast.makeText(getApplicationContext(), "need location permission for using App", Toast.LENGTH_SHORT).show();
                // 3-3. Request permissioni to user. Result is received by onRequestPermissionResult method.
                ActivityCompat.requestPermissions(LocationRegisterActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
                return true;


            } else {
                // 4-1. If the user has never denied a permission, request a permission immediately.
                // Request permissioni to user. Result is received by onRequestPermissionResult method.
                ActivityCompat.requestPermissions(LocationRegisterActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
                return true;
            }
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
            // Check whether user's gps is activated or not
            if (checkLocationServicesStatus()) {
                if (checkLocationServicesStatus()) {

                    Log.d("@@@", "onActivityResult : GPS activated");
                    return;
                }
            }
            break;
            case SEARCH_ADDRESS_ACTIVITY:
                Geocoder geocoder = new Geocoder(this);
                if (resultCode == RESULT_OK) {
                    String address = data.getExtras().getString("data");
                    if (address != null) { // address exist
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


    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {

        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);

        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // if request code is PERMISSIONS_REQUEST_CODE, and requested number of permissions has been received

            boolean check_result = true;
            // Check All permission is allowed
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    gpsTracker = new GpsTracker(getApplicationContext());
                    selected_latitude = gpsTracker.getLatitude();
                    selected_longtitude= gpsTracker.getLongitude();
                    break;
                }
            }

            if (!check_result) {
                // If denined permission exist, then explain why the app is unavailable and exit the app. Two case is available.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0]) || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
                    Toast.makeText(getApplicationContext(), "Permission Denined. Run app again and allow permisison.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denined. Permissions must be allowed in settings (app information). ", Toast.LENGTH_SHORT).show();
                }
                return;
            }

        }
    }

}