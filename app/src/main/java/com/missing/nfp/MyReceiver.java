package com.missing.nfp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.util.Log;

/**
 * Created by mmissildine on 1/12/2018.
 */

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("nfpNotif", "Recieved intent...processing.");

        String notifText = "Click here to enter your daily mucus reading.";



        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //create notif post SDK26
        final int NOTIFY_ID = 0;
        String aMessage = context.getString(R.string.notifTitle);

        // There are hardcoding only for show it's just strings
        String name = context.getString(R.string.app_name);
        String id = context.getString(R.string.app_name); // The user-visible name of the channel.
        String description = "NFP Channel"; // The user-visible description of the channel.

        Intent intent2;
        PendingIntent pendingIntent;
        NotificationCompat.Builder builder;

        if (notificationManager == null) {
            notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = notificationManager.getNotificationChannel(id);
            if (mChannel == null) {
                mChannel = new NotificationChannel(id, name, importance);
                mChannel.setDescription(description);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{0, 250, 250, 250});
                notificationManager.createNotificationChannel(mChannel);
            }
            builder = new NotificationCompat.Builder(context, id);

            intent2 = new Intent(context, ActivityMain.class);
            pendingIntent = PendingIntent.getActivity(context, 0, intent2, PendingIntent.FLAG_IMMUTABLE);

            builder.setContentTitle(aMessage)  // required
                    .setSmallIcon(R.drawable.notif_icon) // required
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(notifText)) // required
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setTicker(aMessage + notifText);
        } else {

            builder = new NotificationCompat.Builder(context);

            intent2 = new Intent(context, ActivityMain.class);
            pendingIntent = PendingIntent.getActivity(context, 0, intent2, PendingIntent.FLAG_IMMUTABLE);

            builder.setContentTitle(aMessage)                           // required
                    .setSmallIcon(R.drawable.notif_icon) // required
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(notifText))  // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setTicker(aMessage + notifText)
                    .setVibrate(new long[]{100, 200})
                    .setPriority(Notification.PRIORITY_DEFAULT);
        }

        Notification notification = builder.build();
        notificationManager.notify(NOTIFY_ID, notification);


        Log.d("nfpNotif", "Successfully sent notification.");


    }


}

