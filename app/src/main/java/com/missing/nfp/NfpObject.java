package com.missing.nfp;

import android.graphics.drawable.Drawable;
import android.util.Log;
import java.util.Date;

public class NfpObject {
    int stampColor;
    String date;
    String mucusCode = "";
    String mucusModifiers = "";
    String frequency = "";
    String redLevel = "";
    String notes = "";

    String getFullEntry(){
        String text = redLevel + "" + mucusCode + mucusModifiers + " " + frequency+ " " + notes;
        Log.d("entry", text);
        return (text);
    }
}
