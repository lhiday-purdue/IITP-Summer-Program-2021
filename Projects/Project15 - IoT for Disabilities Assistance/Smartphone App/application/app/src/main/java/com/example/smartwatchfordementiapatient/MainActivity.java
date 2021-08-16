package com.example.smartwatchfordementiapatient;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static TextView current_address_tv;
    public static TextView current_latitude;
    public static TextView current_longitude;

    public static GoogleMap main_Map;

    private NavigationView navigationView;
    private DrawerLayout drawer_layout;
    private ImageButton menu_btn;
    private Intent serviceIntent;

    private Button logout_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("Main",SharedPreference.getAttribute(getApplicationContext(),"id"));
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(POWER_SERVICE);

        //google map initial setting
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //if get patient's location(latitude, longitude) from server, change to address by using 'getCurrentAddress' function
        current_address_tv=findViewById(R.id.current_address_tv);


        drawer_layout=findViewById(R.id.drawer_layout);
        navigationView=findViewById(R.id.navigation_menu);
        current_latitude = findViewById(R.id.latitude_tv);
        current_longitude = findViewById(R.id.longitude_tv);

        //click menu button
        menu_btn=findViewById(R.id.menu_btn);
        menu_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                drawer_layout.openDrawer(GravityCompat.START);
            }
        });

        //click logout button
        logout_btn=findViewById(R.id.logout_btn);
        logout_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"logout",Toast.LENGTH_SHORT).show();
            }
        });

        //navigation menu
        navigationView=findViewById(R.id.navigation_menu);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                drawer_layout.closeDrawers();

                int id = menuItem.getItemId();
                String title = menuItem.getTitle().toString();

                if(id == R.id.menu_location_setting){
                    startActivity(new Intent(getApplicationContext(),LocationSettingActivity.class));
                }
                return true;
            }
        });

        boolean isWhiteListing = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            isWhiteListing = pm.isIgnoringBatteryOptimizations(getApplicationContext().getPackageName());
        }
        if (!isWhiteListing) {
            Intent intent = new Intent();
            intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
            startActivity(intent);
        }

        if (RealService.serviceIntent==null) {
            serviceIntent = new Intent(this, RealService.class);
            startService(serviceIntent);
        } else {
            serviceIntent = RealService.serviceIntent;//getInstance().getApplication();
            Toast.makeText(getApplicationContext(), "already", Toast.LENGTH_LONG).show();
        }


    }



    //google map setting
    @Override
    public void onMapReady(GoogleMap googleMap) {
        main_Map = googleMap;

        //latitude, longitude should be changed to patient's current location
        //location should be get from server db
//        Toast.makeText(getApplicationContext(),"asdasdasdasd",Toast.LENGTH_SHORT).show();
//        LatLng seoul = new LatLng(37, 128);
//        main_Map.addMarker(new MarkerOptions().position(seoul).title("seoul"));
//        main_Map.moveCamera(CameraUpdateFactory.newLatLng(seoul));
//        main_Map.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul,14));

        current_address_tv.setText("Loading...");
        current_longitude.setText("Loading...");
        current_latitude.setText("Loading...");
    }

    //geocoder : longtitude, latitude <-> address
    public String getCurrentAddressforPatient( double latitude, double longitude) {

        //지오코더
        // GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 7);
        } catch (IOException ioException) {
            //네트워크 문제w
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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceIntent!=null) {
            stopService(serviceIntent);
            serviceIntent = null;
        }
    }

}