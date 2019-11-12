package com.missing.nfp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by mmissildine on 1/17/2018.
 */

public class NotifSettings extends PreferenceActivity {

    private final static String TAG = "nfpNotifs";
    public Preference time;

    public NotifSettings() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "OnCreate NofitfSettingsOverflowMenu");

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new LocationFragment()).commit();

    }

    public static class LocationFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        private final static String TAG = "nfpNotifs";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Log.d(TAG, "OnCreate NotifFragment");
            addPreferencesFromResource(R.xml.pref_notification);
            PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);

            TimePreference tp = (TimePreference) getPreferenceManager().findPreference("timePref_Key");
            tp.showTime(tp.getH(), tp.getM());
        }

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            Intent intent1 = new Intent(getContext(), MyReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);

            if (key.equals("notifications_new_message")) {
                Boolean notifPref = sharedPreferences.getBoolean("notifications_new_message", true);

                if (notifPref) {
                    MainActivity.startAlarming(getContext());
                    Log.d(TAG, "Set alarm from Toggle Notifs ON.");
                } else {
                    am.cancel(pendingIntent);
                    pendingIntent.cancel();
                    Log.d(TAG, "Alarm intent cancelled because NotificationsDisabled");
                }

            }

            if (key.equals("timePref_Key")) {
                MainActivity.startAlarming(getContext());
                Log.d(TAG, "Set alarm from TimeChange.");

            }

            if (key.equals("autoSave")){
                Log.d(TAG, "AutoSave changed");
                sharedPreferences.edit().apply();
            }
        }

    }


}
