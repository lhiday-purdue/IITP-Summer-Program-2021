package com.example.smartwatchfordementiapatient;
// This page will work in Background either
// It'll continuously get Patient's locate and show it on google map.
// It'll not die easily
// It checks whether patient goes out safe range or not
// If patient goes out safe range, then push alarm to guardian and send message to patient

import android.app.AlarmManager;
import android.app.Application;
import android.telephony.SmsManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RealService extends Service {
    public static Thread mainThread;
    public static boolean patient_in = true;
    public static Intent serviceIntent = null;
    JSONObject json_id = new JSONObject();
    private boolean run = true;
    public RealService() {
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serviceIntent = intent;

        mainThread = new Thread(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("aa hh:mm");
                run = true;
                while (run) {
                    try {
                        connectAPI();
                        Thread.sleep(1000 * 13 * 1); // for wait server
                    } catch (InterruptedException e) {
                        run = false;
                        e.printStackTrace();
                    }
                }
            }
        });
        mainThread.start();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() { // thread stop
        super.onDestroy();

        serviceIntent = null;
        setAlarmTimer(); // we'll call this service
        Thread.currentThread().interrupt();

        if (mainThread != null) {
            mainThread.interrupt();
            mainThread = null;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }


    protected void setAlarmTimer() {
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.add(Calendar.SECOND, 1);
        Intent intent = new Intent(this, AlarmRecever.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0,intent,0);
        AlarmManager mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), sender);
    }

    //connect to api for getting locate
    private void connectAPI(){
        try{

            URL url = new URL("http://13.125.120.0:5000/query-location");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if(urlConnection != null) {
                urlConnection.setConnectTimeout(10000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setDoInput(true);
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setDoOutput(true);
                json_id.put("id", SharedPreference.getAttribute(getApplicationContext(), "id"));
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
                bw.write(json_id.toString());
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
                        ArrayList<String> locate = JSONParsing(line);
                        double now_latitude_patient = Double.parseDouble(locate.get(1));
                        double now_longitude_patient = Double.parseDouble(locate.get(0));
                        boolean in_distance = getDistance(now_latitude_patient,now_longitude_patient);
                        // if this is first time that patient goes out safe range then push to guardian and send message to patient
                        if(in_distance&&patient_in){
                            json_id.put("is_patient_away",true);
                            patient_in=false;
                            SmsManager sms = SmsManager.getDefault();
                            String message = "you moved out of range Please return to home";
                            //send message
                            sms.sendTextMessage(SharedPreference.getAttribute(getApplicationContext(),"phone"), null, message, null, null);
                            //send notification
                            sendNotification("PATIENT IS BACK", "CHECK");
//                            sendNotification("WARNING PATIENT IS OUT OF RANGE","ALERT");
                            //send watch that patient is out of safe range
                            sendOutofRangeAPI();
                        //if patient is back to safe range
                        }else if(!in_distance&&!patient_in){
                            json_id.put("is_patient_away",false);
                            patient_in=true;
                            //push alaram to guardian
                            sendNotification("PATIENT IS BACK", "CHECK");
                            sendOutofRangeAPI();
                        }
                        LatLng seoul = new LatLng(now_latitude_patient,now_longitude_patient);
                        Handler h = new Handler(getApplication().getMainLooper()); // connect to MainActivty and setting google map
                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.main_Map.clear();
                                MainActivity.main_Map.addMarker(new MarkerOptions().position(seoul).title("patient"));
                                MainActivity.main_Map.moveCamera(CameraUpdateFactory.newLatLng(seoul));
                                MainActivity.main_Map.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul,22));
                                MainActivity.current_address_tv.setText(getCurrentAddressforPatient(now_latitude_patient,now_longitude_patient));
                                MainActivity.current_latitude.setText(Double.toString(now_latitude_patient));
                                MainActivity.current_longitude.setText(Double.toString(now_longitude_patient));
                            }
                        });

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

                json_id.put("id", SharedPreference.getAttribute(getApplicationContext(), "id"));
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
                bw.write(json_id.toString()); // input data
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


    private ArrayList<String> JSONParsing(String jsonstring){
        StringBuilder stringBuilder = new StringBuilder();
        ArrayList<String> locate = new ArrayList<>();
        try{
            JSONObject jsonObject = new JSONObject(jsonstring);

            locate.add(jsonObject.getString("longitude"));
            locate.add(jsonObject.getString("latitude"));

        }catch(JSONException e){
            e.printStackTrace();
        }
        return locate;
    }
    //geocoder : longtitude, latitude <-> address
    public String getCurrentAddressforPatient( double latitude, double longitude) {

        // change the gps to address
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
    //for distance checking
    public boolean getDistance(double lat , double lng){
        Location locationA = new Location("point A");
        locationA.setLatitude(Double.parseDouble(SharedPreference.getAttribute(getApplicationContext(),"latitude")));
        locationA.setLongitude(Double.parseDouble(SharedPreference.getAttribute(getApplicationContext(),"longitude")));
        Location locationB = new Location("point B");
        locationB.setLatitude(lat);
        locationB.setLongitude(lng);
        if(locationA.distanceTo(locationB)>Integer.parseInt(SharedPreference.getAttribute(getApplicationContext(),"patient_range"))){
            return true;
        }else{
            return false;
        }
    }

    //push alaram
    private void sendNotification(String messageBody, String title) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        String channelId = "fcm_default_channel";//getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)//drawable.splash)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,"Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

}

