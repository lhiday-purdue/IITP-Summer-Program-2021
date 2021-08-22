package com.example.smartwatchfordementiapatient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
// Rerun RealService class when it is dead
public class AlarmRecever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //after oreo version then restartservice
            Intent in = new Intent(context, RestartService.class);
            context.startForegroundService(in);
        } else { // else realservice
            Intent in = new Intent(context, RealService.class);
            context.startService(in);
        }
    }

}