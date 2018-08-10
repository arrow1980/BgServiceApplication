package com.example.abneryu.bgserviceapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class MQBootReceiver extends BroadcastReceiver {
    private static final String TAG = MQBootReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, intent.getAction());
        context.startService(new Intent(context, MQIntentService.class));
//        context.startActivity(new Intent(context, MainActivity.class));
    }
}
