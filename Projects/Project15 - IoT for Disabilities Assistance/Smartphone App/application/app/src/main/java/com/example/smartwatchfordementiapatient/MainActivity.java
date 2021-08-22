package com.example.smartwatchfordementiapatient;
// It shows patient's lcoate
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.telephony.SmsManager;
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
    static final int SMS_RECEIVE_PERMISSON=1;
    private Button logout_btn;
    SmsManager sms = SmsManager.getDefault();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                SharedPreference.removeAll(getApplicationContext());

                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                android.os.Process.killProcess(android.os.Process.myPid()); //kill the Realservice process either
                finish();

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

        // ignore power shaving mode
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
            startService(serviceIntent);
        }


    }




    //google map setting
    @Override
    public void onMapReady(GoogleMap googleMap) {
        main_Map = googleMap;

        current_address_tv.setText("Loading...");
        current_longitude.setText("Loading...");
        current_latitude.setText("Loading...");
    }

    //geocoder : longtitude, latitude <-> address
    public String getCurrentAddressforPatient( double latitude, double longitude) {

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 7);
        } catch (IOException ioException) {
            //network error
            return "geocorder not service";
        } catch (IllegalArgumentException illegalArgumentException) {
            return "wrong gps location";
        }
        if (addresses == null || addresses.size() == 0) {
            return "couldn't search address";
        }

        Address address = addresses.get(0);
        String addr=address.getAddressLine(0);
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