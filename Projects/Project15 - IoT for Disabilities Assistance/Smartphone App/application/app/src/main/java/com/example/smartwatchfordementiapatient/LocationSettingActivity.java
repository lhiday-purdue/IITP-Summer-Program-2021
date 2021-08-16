package com.example.smartwatchfordementiapatient;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class LocationSettingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button complete_btn;
    private Button cancel_btn;

    private double selected_latitude=37.56638872588792;
    private double selected_longtitude=126.97800947033107;

    private TextView address_tv;
    private ImageButton back_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_setting);

        address_tv=findViewById(R.id.address_tv);
        //check location permission
        String addr=getCurrentAddress(selected_latitude,selected_longtitude);
        address_tv.setText(addr);

        back_btn=findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //click complete button
        complete_btn=findViewById(R.id.complete_btn);
        complete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //connet to server to send location data..

                //then move to next page
                //move to next page
                Toast.makeText(getApplicationContext(), "complete modify location", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();

            }
        });

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

                Double latitude = point.latitude; // 위도
                Double longitude = point.longitude; // 경도
                selected_latitude=latitude;
                selected_longtitude=longitude;
                //change location -> address
                String addr=getCurrentAddress(selected_latitude,selected_longtitude);
                address_tv.setText(addr);

                // 마커의 스니펫(간단한 텍스트) 설정
                mOptions.snippet(latitude + ", " + longitude);
                // LatLng: 위도 경도 쌍을 나타냄
                mOptions.position(new LatLng(latitude, longitude));

                // 반경 1KM원
                CircleOptions circle1KM = new CircleOptions().center(new LatLng(latitude, longitude)) //latitude & longitude of point
                        .radius(1000)      //radius unit : m
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

        // 반경 1KM원
        CircleOptions circle1KM = new CircleOptions().center(seoul) //latitude & longitude of point
                .radius(1000)      //radius unit : m
                .strokeWidth(0f)  //line width -> 0f = no line
                .fillColor(Color.parseColor("#885b9fde")); //background color

        mMap.addCircle(circle1KM);


    }

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

}