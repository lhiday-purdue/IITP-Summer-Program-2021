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

class RequestThread extends Thread {
    @Override
    public void run() {
        try{
            URL url = new URL("http://13.125.120.0:5000/signup");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if(urlConnection != null) {
                urlConnection.setConnectTimeout(10000); // 10초 동안 기다린 후 응답이 없으면 종료
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setDoInput(true);
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setDoOutput(true); // 데이터 전송
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
                bw.write(LocationRegisterActivity.jsonb.toString());
                Log.e("확인",LocationRegisterActivity.jsonb.toString());
                bw.flush();
                bw.close();

                //서버 내용 수신 받기
                int resCode = urlConnection.getResponseCode();
                if(resCode == HttpURLConnection.HTTP_OK){
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String line = null;
                    while(true){
                        line = reader.readLine();
                        if(line == null)
                            break;
                        Log.d("asdddddddddddd",line);
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
    private int radius=300; // patinet_range


    private RadioGroup search_radioGroup;
    private RadioButton google;
    private RadioButton daum;
    private String search_setting="google";

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


    // 주소 요청코드 상수 requestCode


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

        radio300=findViewById(R.id.radio_300);
        radio500=findViewById(R.id.radio_500);
        radio1000=findViewById(R.id.radio_1000);
        radioGroup=findViewById(R.id.radiogroup);
        radioGroup.setOnCheckedChangeListener(radioGroupButtonChangeListener);

        edit_addr = findViewById(R.id.address_search_et);
        search_btn = findViewById(R.id.search_btn);
        Geocoder geocoder = new Geocoder(this);
        korea_address = findViewById(R.id.korea_search);

        //현재위치 가져오기
        gpsTracker = new GpsTracker(getApplicationContext());
        selected_latitude = gpsTracker.getLatitude();
        selected_longtitude= gpsTracker.getLongitude();
//        Toast.makeText(getApplicationContext(), "현재위치 \n위도 " + selected_longtitude + "\n경도 " + selected_latitude, Toast.LENGTH_LONG).show();

        search_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(search_setting=="google"){ //구글 주소 검색
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
                            Log.e("test", "입출력 오류 - 주소변환시 에러발생");
                        }
                        Toast.makeText(getApplicationContext(), Integer.toString(list.size()), Toast.LENGTH_SHORT).show();
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
                }else{ //다음 주소 검색
                    Log.i("주소설정페이지", "주소입력창 클릭");
                    int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
                    if(status == NetworkStatus.TYPE_MOBILE || status == NetworkStatus.TYPE_WIFI) {

                        Log.i("주소설정페이지", "주소입력창 클릭");
                        Intent i = new Intent(getApplicationContext(), AddressApiActivity.class);
                        // 화면전환 애니메이션 없애기
                        overridePendingTransition(0, 0);
                        // 주소결과
                        startActivityForResult(i, SEARCH_ADDRESS_ACTIVITY);

                    }else {
                        Toast.makeText(getApplicationContext(), "인터넷 연결을 확인해주세요.", Toast.LENGTH_SHORT).show();
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
                    jsonb.put("patient",patient);
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

                Double latitude = point.latitude; // 위도
                Double longitude = point.longitude; // 경도
                selected_latitude=latitude;
                selected_longtitude=longitude;
                //change location -> address
                //Toast.makeText(getApplicationContext(), ""+latitude+" "+longitude, Toast.LENGTH_SHORT).show();
                String addr=getCurrentAddress(selected_latitude,selected_longtitude);
                address_tv.setText(addr);

                // 마커의 스니펫(간단한 텍스트) 설정
                mOptions.snippet(latitude + ", " + longitude);
                // LatLng: 위도 경도 쌍을 나타냄
                mOptions.position(new LatLng(latitude, longitude));

                // 반경 1KM원
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

        // 반경 1KM원
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
                radius=300;
                mMap.clear();
                MarkerOptions mOptions = new MarkerOptions();
                mOptions.position(new LatLng(selected_latitude, selected_longtitude));
                CircleOptions circle = new CircleOptions().center(new LatLng(selected_latitude, selected_longtitude)) //latitude & longitude of point
                        .radius(radius)      //radius unit : m
                        .strokeWidth(0f)  //line width -> 0f = no line
                        .fillColor(Color.parseColor("#885b9fde")); //background color

                mMap.addMarker(mOptions);
                mMap.addCircle(circle);
                Toast.makeText(getApplicationContext(), "range: 300m", Toast.LENGTH_SHORT).show();

            } else if(i == R.id.radio_500){
                radius=500;
                mMap.clear();
                MarkerOptions mOptions = new MarkerOptions();
                mOptions.position(new LatLng(selected_latitude, selected_longtitude));
                CircleOptions circle = new CircleOptions().center(new LatLng(selected_latitude, selected_longtitude)) //latitude & longitude of point
                        .radius(radius)      //radius unit : m
                        .strokeWidth(0f)  //line width -> 0f = no line
                        .fillColor(Color.parseColor("#885b9fde")); //background color
                mMap.addMarker(mOptions);
                mMap.addCircle(circle);
                Toast.makeText(getApplicationContext(), "range: 500m", Toast.LENGTH_SHORT).show();
            }
            else if(i==R.id.radio_1000){
                radius=1000;
                mMap.clear();
                MarkerOptions mOptions = new MarkerOptions();
                mOptions.position(new LatLng(selected_latitude, selected_longtitude));
                CircleOptions circle = new CircleOptions().center(new LatLng(selected_latitude, selected_longtitude)) //latitude & longitude of point
                        .radius(radius)      //radius unit : m
                        .strokeWidth(0f)  //line width -> 0f = no line
                        .fillColor(Color.parseColor("#885b9fde")); //background color
                mMap.addMarker(mOptions);
                mMap.addCircle(circle);
                Toast.makeText(getApplicationContext(), "range: 1km", Toast.LENGTH_SHORT).show();
            }
        }
    };


    //geocoder : longtitude, latitude <-> address
    public String getCurrentAddress( double latitude, double longitude) {

        //지오코더
        // GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 7);
        } catch (IOException ioException) {
            //네트워크 문제
            return "geocorder not service";
        } catch (IllegalArgumentException illegalArgumentException) {
            //Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "wrong gps location";
        }
        if (addresses == null || addresses.size() == 0) {
            //Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
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
        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
            // 3.  위치 값을 가져올 수 있음
            return true;
        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(LocationRegisterActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(getApplicationContext(), "앱을 사용하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(LocationRegisterActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
                return true;


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(LocationRegisterActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
                return true;
            }
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
            //사용자가 GPS 활성 시켰는지 검사
            if (checkLocationServicesStatus()) {
                if (checkLocationServicesStatus()) {

                    Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                    return;
                }
            }
            break;
            case SEARCH_ADDRESS_ACTIVITY:
                Geocoder geocoder = new Geocoder(this);
                if (resultCode == RESULT_OK) {
                    String address = data.getExtras().getString("data");
                    if (address != null) { // 주소 존재
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
                            Log.e("test","입출력 오류 - 주소변환시 에러발생");
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
                            Log.e("test","입출력 오류 - 주소변환시 에러발생");
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

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;
            // 모든 퍼미션을 허용했는지 체크합니다.
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
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0]) || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
                    Toast.makeText(getApplicationContext(), "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_SHORT).show();
                }
                return;
            }

        }
    }

}