package com.example.smbeaconclient;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class IntroActivity extends AppCompatActivity {
    String TAG = "IntroActivitylog";

    private final int PERMISSION_CODE_MULTIPLES = 1;
    private static final int PERMISSION_CODE_BACKGROUND_LOCATION = 2;
    private static final int PERMISSION_CODE_DRAW_OVERLAY = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        Button btnCheck = (Button) findViewById(R.id.btnCheck);
        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        if (chkPermission()) {
            // For mobile phone information, use Telephony Manager
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            Log.d(TAG, "phone number : [ getLine1Number ] >>> " + tm.getLine1Number());
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.Q) //29
    private static final String[] BACKGROUND_LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
    };

    public boolean chkPermission() {
        boolean mPermissionsGranted = false; //Whether you have approved all risk permissions
        String[] mRequiredPermissions = new String[3];

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { //30
            mRequiredPermissions[0] = Manifest.permission.READ_PHONE_NUMBERS;
        }else{
            mRequiredPermissions[0] = Manifest.permission.READ_PHONE_STATE;
        }
        mRequiredPermissions[1] = Manifest.permission.ACCESS_COARSE_LOCATION; //needed for android 12 , GPS
        mRequiredPermissions[2] = Manifest.permission.ACCESS_FINE_LOCATION;   //GPS approximate location

//        mPermissionsGranted = hasPermissions(mReq/uiredPermissions);

        //Check that user have the necessary permissions and ask for the permissions
        for (String permission : mRequiredPermissions) {
            if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(IntroActivity.this, mRequiredPermissions, PERMISSION_CODE_MULTIPLES);
            }
        }

        if (!Settings.canDrawOverlays(getApplicationContext())) showOverlayDialog();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)  != PackageManager.PERMISSION_GRANTED) {
                showBackgroundPermissionDialog();
            }
        }

        return mPermissionsGranted;
    }

    //Ensure that you have the necessary permissions
    public boolean hasPermissions(String[] permissions) {
        for (String permission : permissions) {
            if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED ) {
                return false;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)  != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        if (!Settings.canDrawOverlays(getApplicationContext())) return false;

        return true;
    }

    // Starting with Android API 30, backgroundPermission must be set up directly
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void requestBackgroundPermission() {
        ActivityCompat.requestPermissions(IntroActivity.this, BACKGROUND_LOCATION_PERMISSIONS, PERMISSION_CODE_BACKGROUND_LOCATION);
    }
    private void showBackgroundPermissionDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("This app needs background location access");
        builder.setMessage("Please set it to 'always allow' so this app can detect beacons in the background.");
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestBackgroundPermission();
            }
        }).setNegativeButton("no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }
    void showOverlayDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.dialog_overlaypermission, null);

        androidx.appcompat.app.AlertDialog.Builder msgBuilder = new androidx.appcompat.app.AlertDialog.Builder(IntroActivity.this)
                .setTitle("This app needs drawing over other apps permission")
                .setMessage("Please find our 'Project9' app and set permission")
                .setView(dialoglayout)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestOverlayPermissions();
                    }
                }).setNegativeButton("no", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
        androidx.appcompat.app.AlertDialog msgDlg = msgBuilder.create();
        msgDlg.show();
    }
    private void requestOverlayPermissions() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        startActivityForResult(intent, PERMISSION_CODE_DRAW_OVERLAY);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_CODE_MULTIPLES && (grantResults.length > 0) ||
                requestCode == PERMISSION_CODE_BACKGROUND_LOCATION && (grantResults.length > 0) ||
                requestCode == PERMISSION_CODE_DRAW_OVERLAY && (grantResults.length > 0)        ) {

            for (int i = 0; i < grantResults.length; i++) {
                // If grantResults is 0, it means you allowed it, -1 means you denied it
                if (grantResults[i] == -1) {
                    Toast.makeText(IntroActivity.this, "This app will not be available if you do not accept all of the permissions.", Toast.LENGTH_SHORT).show();
                    chkPermission();
                }
            }
        }
//        } else {
//            Toast.makeText(IntroActivity.this, "This app will not be available if you do not accept all of the permissions.", Toast.LENGTH_SHORT).show();
//            chkPermission();
//        }
    }

}
