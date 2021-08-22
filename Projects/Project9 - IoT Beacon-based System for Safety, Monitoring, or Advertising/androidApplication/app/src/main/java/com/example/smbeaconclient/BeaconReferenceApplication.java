package com.example.smbeaconclient;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.smbeaconclient.firebase.MyFirestore;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

// This class is used for background beacon monitoring and calculate the floor  .
public class BeaconReferenceApplication extends Application implements BootstrapNotifier, BeaconConsumer, RangeNotifier {
    private static final String TAG = "BeaconReferenceApp";
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;
    private DebugActivity debugActivity = null;
    private String cumulativeLog = "";
    BeaconManager beaconManager;
    String token;

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate called");

        token = getSharedPreferences("Token", MODE_PRIVATE).getString("token","");

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")); //Adding iBeacon Layout Code
//        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19")); //EDDYSTONE  UID Layout Code

        beaconManager.setDebug(true);


        // Uncomment the code below to use a foreground service to scan for beacons. This unlocks
        // the ability to continually scan for long periods of time in the background on Andorid 8+
        // in exchange for showing an icon at the top of the screen and a always-on notification to
        // communicate to users that your app is using resources in the background.
///*
        // start; comment
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_beacon);
        builder.setContentTitle("Building entry and exit monitoring");
        Intent intent = new Intent(this, IntroActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(pendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("My Notification Channel ID",
                    "My Notification Name", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("My Notification Channel Description");
            NotificationManager notificationManager = (NotificationManager) getSystemService(
                    Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channel.getId());
        }
        beaconManager.enableForegroundServiceScanning(builder.build(), 456);

        // For the above foreground scanning service to be useful, you need to disable
        // JobScheduler-based scans (used on Android 8+) and set a fast background scan
        // cycle that would otherwise be disallowed by the operating system.
        //
        beaconManager.setEnableScheduledScanJobs(false);
        beaconManager.setBackgroundBetweenScanPeriod(0);
        beaconManager.setBackgroundScanPeriod(1100);
        // end; comment
//*/


        Log.d(TAG, "setting up background monitoring for beacons and power saving");
        // wake up the app when our building's beacon is seen (our building's beacon UUID value is like below.)
        // If you test this app for your own beacon, you need to change the beacon's value below.
        Region region = new Region("backgroundRegion", Identifier.parse("cc36ea67-0748-4394-9840-596a14faa1fd"), null, null);
//        Region region = new Region("backgroundRegion", null, null, null);

        regionBootstrap = new RegionBootstrap(this, region);

        // simply constructing this class and holding a reference to it in your custom Application
        // class will automatically cause the BeaconLibrary to save battery whenever the application
        // is not visible.  This reduces bluetooth power usage by about 60%
        backgroundPowerSaver = new BackgroundPowerSaver(this);

        // If you wish to test beacon detection in the Android Emulator, you can use code like this:
        // BeaconManager.setBeaconSimulator(new TimedBeaconSimulator() );
        // ((TimedBeaconSimulator) BeaconManager.getBeaconSimulator()).createTimedSimulatedBeacons();

//        beaconManager.setBackgroundBetweenScanPeriod(30000l);
//        beaconManager.setForegroundBetweenScanPeriod(2000l);
        beaconManager.bind(this);
    }


    @Override
    public void didEnterRegion(Region region) { //When you entered in Beacon region, this method is called.
        //update "enter" field on the db
        MyFirestore.getWorkplaceColRef().document(token).update("enter", true) //entered the building
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });

        Log.d(TAG, "Access Region: did enter region.");
        // Send a notification to the user whenever a Beacon matching a Region (defined above) are first seen.
        Log.d(TAG, "Sending notification.");
        sendNotification();
        if (debugActivity != null) {
            // If the Monitoring Activity is visible, we log info about the beacons we have seen on its display
            logToDisplay("I see a beacon again" + region);
        }

        // Differentiate insider & outsider
        if (token != null) {
            MyFirestore.getWorkplaceColRef().document(token).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            boolean insider = document.getBoolean("insider");
                            if (insider)
                                Log.d(TAG, "who are you: document exists, our worker! " + document.getData());
                            else Log.d(TAG, "who are you: No such document,stranger");
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        }

        beaconManager.startRangingBeacons(region);
    }

    @Override
    public void didExitRegion(Region region) {
        logToDisplay("You've left the building.");
        Log.d(TAG, "Access Region: You've left the building.");

//        if (region != null && beaconManager != null)
        beaconManager.stopRangingBeacons(region);

        //update "enter" field on the db
        MyFirestore.getWorkplaceColRef().document(token).update("enter", false) //left the building
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        logToDisplay("Current region state is: " + (state == 1 ? "INSIDE" : "OUTSIDE ("+state+")"));
    }

    private void sendNotification() {
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("Beacon Reference Notifications",
                    "Beacon Reference Notifications", NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
            builder = new Notification.Builder(this, channel.getId());
        } else {
            builder = new Notification.Builder(this);
            builder.setPriority(Notification.PRIORITY_HIGH);
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(new Intent(this, IntroActivity.class));
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setSmallIcon(R.drawable.ic_beacon);
        builder.setContentTitle("You Entered Project9 Building");
        builder.setContentText("Tap here to see details in the app");
        builder.setContentIntent(resultPendingIntent);
        notificationManager.notify(1, builder.build());
    }

    public void setMonitoringActivity(DebugActivity activity) {
        this.debugActivity = activity;
    }

    private void logToDisplay(String line) {
        cumulativeLog += (line + "\n");
        if (this.debugActivity != null) {
            this.debugActivity.updateLog(cumulativeLog);
        }
    }

    public String getLog() {
        return cumulativeLog;
    }

    @Override
    public void onBeaconServiceConnect() {
        //called each time the BeaconService gets ranging data, which is nominally once per second when beacons are detected.
        beaconManager.addRangeNotifier(this);
    }
    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        Log.d(TAG, "didRangeBeaconsInRegion called");
        if (beacons != null && !beacons.isEmpty()) {
            ArrayList<Beacon> beaconList = (ArrayList<Beacon>) beacons;
            Log.d(TAG, "[raning] number of recognized beacons : " + beaconList.size());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < beaconList.size(); i++) {
                double roundedDist = Math.round(beaconList.get(i).getDistance()*100)/100.0; // 소수점 두자리에서 반올림한 값으로 출력

                Log.d(TAG, "[raning]" + i+"'s beacon UUID:" + beaconList.get(i).getId1() + ", Major: " + beaconList.get(i).getId2() +
                        ", Minor: " + beaconList.get(i).getId3() + ", RSSI: " + beaconList.get(i).getRssi() + ", Dist: " + roundedDist) ;

                sb.append(i + "'s beacon UUID:" + beaconList.get(i).getId1()
                        + "\nMajor: " + beaconList.get(i).getId2()
                        + "\nMinor: " + beaconList.get(i).getId3()
                        + "\nRSSI: " + beaconList.get(i).getRssi()
                        + "\nDist: " + roundedDist + "\n\n");
            }
            if (debugActivity!= null)
                debugActivity.textViewRanging.setText(sb.toString());

            Collections.sort(beaconList, new Comparator<Beacon>() {
                @Override
                public int compare(Beacon o1, Beacon o2) {
                    if (o1.getRssi() < o2.getRssi()) return 1;
                    else                             return -1;
                }
            });

            //Determining the floor by the major value of the beacon with the most intense Rssi
            int floor = beaconList.get(0).getId2().toInt();

            if (debugActivity != null)
                debugActivity.textViewFloor.setText(floor + "Floor");

            //update "floor" field on the db
            MyFirestore.getWorkplaceColRef().document(token).update("floor", floor)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully updated!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error updating document", e);
                        }
                    });
        }
    }
}
