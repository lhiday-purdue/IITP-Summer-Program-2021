package com.example.smartwatchfordementiapatient;

import android.app.AlarmManager;
import android.app.Application;
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
    private Thread mainThread;
    public static Intent serviceIntent = null;
    JSONObject json_id = new JSONObject();

    public RealService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serviceIntent = intent;

        mainThread = new Thread(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("aa hh:mm");
                boolean run = true;
                try {
                    json_id.put("id", SharedPreference.getAttribute(getApplicationContext(), "id"));
                }catch (Exception e){
                    e.printStackTrace();
                    return;
                }
                while (run) {
                    try {

                         // 1 minute=>대기시ㅏㄱㄴ
                        Date date = new Date();
//                        sendNotification(sdf.format(date));
                        connectAPI();
                        Thread.sleep(1000 * 15 * 1);
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
    public void onDestroy() { // 스레드 종료
        super.onDestroy();

        serviceIntent = null;
        setAlarmTimer();
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

    public void showToast(final Application application, final String msg) {
        Handler h = new Handler(application.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(application, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    protected void setAlarmTimer() {
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.add(Calendar.SECOND, 1); //RealService가 destroy될때마다 1초마다 알림
        Intent intent = new Intent(this, AlarmRecever.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0,intent,0);

        AlarmManager mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), sender);
    }
    
    private void connectAPI(){
        try{

            URL url = new URL("http://13.125.120.0:5000/query-location");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if(urlConnection != null) {
                urlConnection.setConnectTimeout(10000); // 10초 동안 기다린 후 응답이 없으면 종료
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setDoInput(true);
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setDoOutput(true); // 데이터 전송
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
                bw.write(json_id.toString()); // 데이터 삽입
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
                        ArrayList<String> locate = JSONParsing(line);
                        double now_latitude_patient = Double.parseDouble(locate.get(1));
                        double now_longitude_patient = Double.parseDouble(locate.get(0));
                        getDistance(now_latitude_patient,now_longitude_patient);
                        LatLng seoul = new LatLng(now_latitude_patient,now_longitude_patient);
//                        Log.d("locate",locate.get(0)+locate.get(1));
                        Handler h = new Handler(getApplication().getMainLooper()); // MainActivty연결해서 UI설정
                        h.post(new Runnable() {
                            @Override
                            public void run() {

                                MainActivity.main_Map.addMarker(new MarkerOptions().position(seoul).title("patient"));
                                MainActivity.main_Map.moveCamera(CameraUpdateFactory.newLatLng(seoul));
                                MainActivity.main_Map.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul,14));
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
    //for distance checking
    public void getDistance(double lat , double lng){


        Location locationA = new Location("point A");
        locationA.setLatitude(Double.parseDouble(SharedPreference.getAttribute(getApplicationContext(),"latitude")));
        locationA.setLongitude(Double.parseDouble(SharedPreference.getAttribute(getApplicationContext(),"longitude")));

        Location locationB = new Location("point B");
        locationB.setLatitude(lat);
        locationB.setLongitude(lng);

        if(locationA.distanceTo(locationB)>Integer.parseInt(SharedPreference.getAttribute(getApplicationContext(),"patient_range"))){
            sendNotification("WARNING");
        }
    }






    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        String channelId = "fcm_default_channel";//getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)//drawable.splash)
                        .setContentTitle("Service test")
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

