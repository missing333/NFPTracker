package com.missing.nfp;

/**
 * Created by mmissildine on 1/24/2018.
 * Broadcast reciever, starts when the device gets starts.
 * Start your repeating alarm here.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class DeviceBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            ActivityMain.startAlarming(context);
            Log.d("DeviceBootReceiver", "Set alarm from BootReceiver.");
        }
    }
}
