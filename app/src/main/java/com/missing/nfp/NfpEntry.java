package com.missing.nfp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NfpEntry extends AppCompatActivity {

    CalendarView date;
    String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfp_entry);

        final int r = getIntent().getIntExtra("BUTTONROW", -1);
        final int c = getIntent().getIntExtra("BUTTONCOL", -1);

        ////Set activity to smaller 'popup' window
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        float pct = (float) 0.8;
        getWindow().setLayout((int) (width * pct), (int) (height * pct));

        ////get date
        date = findViewById(R.id.calendarView);
        Calendar today = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("M/d", Locale.US);
        selectedDate = sdf.format(today.getTime());
        date.setOnDateChangeListener(myCalendarListener);

        ////set Spinners to Centered Text
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.mucusCodesArray,R.layout.spinner_center_item);
        adapter.setDropDownViewResource(R.layout.spinner_center_item);
        Spinner mCodes = findViewById(R.id.mucusCode);
        mCodes.setAdapter(adapter);
        adapter = ArrayAdapter.createFromResource(
                this, R.array.mucusFreqArray,R.layout.spinner_center_item);
        adapter.setDropDownViewResource(R.layout.spinner_center_item);
        Spinner mFreqs = findViewById(R.id.mucusFreq);
        mFreqs.setAdapter(adapter);

        //restore button selections
        restoreSelections(r,c);


        CheckBox lub = findViewById(R.id.L);
        CheckBox d = findViewById(R.id.d);
        CheckBox w = findViewById(R.id.w);
        CheckBox s = findViewById(R.id.s);
        if (lub.isChecked()){
            d.setEnabled(true);
            w.setEnabled(true);
            s.setEnabled(true);
        } else {
            d.setEnabled(false);
            w.setEnabled(false);
            s.setEnabled(false);
        }
        lub.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                    CheckBox d = findViewById(R.id.d);
                    CheckBox w = findViewById(R.id.w);
                    CheckBox s = findViewById(R.id.s);
                    if (isChecked){
                        d.setEnabled(true);
                        w.setEnabled(true);
                        s.setEnabled(true);
                    } else {
                        d.setEnabled(false);
                        w.setEnabled(false);
                        s.setEnabled(false);
                    }
                }
            });

        //effects to happen when saving.
        Button save = findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RadioGroup radioGroup = findViewById(R.id.stickerGroup);
                TextView commentsView = findViewById(R.id.comments);
                String comments = commentsView.getText().toString();
                String code = generateFinalCode();
                String combined = code;
                int stickerID = getStickerID();

                //add new line if anything else is present, then add comments, if any.
                if (comments.length() > 0){
                    if (combined.length()>0) {combined += "\n";}
                    combined += comments;
                }


                Intent intent = getIntent();
                intent.putExtra("DATE", selectedDate);
                intent.putExtra("BUTTONROW",r);
                intent.putExtra("BUTTONCOL",c);
                intent.putExtra("CODE", combined);
                intent.putExtra("STICKERID", stickerID);



                SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putString("r"+r+"c"+c+"date", selectedDate);
                editor.putString("r"+r+"c"+c+"code", code);
                editor.putString("r"+r+"c"+c+"comments", comments);
                editor.putInt("r"+r+"c"+c+"sticker", stickerID);
                editor.putInt("r"+r+"c"+c+"stickerButton", radioGroup.getCheckedRadioButtonId());
                editor.apply();

                setResult(1, intent); //The data you want to send back
                finish();
            }
        });
    }



    private void restoreSelections(int row, int col) {
        String code="";
        Spinner mCode = findViewById(R.id.mucusCode);
        Spinner mRedCode = findViewById(R.id.redCode);
        Spinner mFreq = findViewById(R.id.mucusFreq);
        CheckBox c = findViewById(R.id.c);
        CheckBox k = findViewById(R.id.k);
        CheckBox y = findViewById(R.id.y);
        CheckBox g = findViewById(R.id.g);
        CheckBox p = findViewById(R.id.p);
        CheckBox l = findViewById(R.id.L);
        CheckBox d = findViewById(R.id.d);
        CheckBox w = findViewById(R.id.w);
        CheckBox s = findViewById(R.id.s);
        CheckBox i = findViewById(R.id.Intercourse);
        RadioGroup peakGroup = findViewById(R.id.peakGroup);
        RadioButton peak = findViewById(R.id.peak);
        RadioButton peak1 = findViewById(R.id.p1);
        RadioButton peak2 = findViewById(R.id.p2);
        RadioButton peak3 = findViewById(R.id.p3);
        TextView comments = findViewById(R.id.comments);
        RadioGroup stickerGroup = findViewById(R.id.stickerGroup);

        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        String savedCode = prefs.getString("r"+row+"c"+col+"code", null);
        String savedComments = prefs.getString("r"+row+"c"+col+"comments", null);
        int savedStickerButton = prefs.getInt("r"+row+"c"+col+"stickerButton", 0);

        if (savedComments != null){
            comments.setText(savedComments);
        }

        if (savedCode != null) {
            Log.d("prefs", "Loading code into views: " + savedCode);
            if (savedCode.toLowerCase().contains("c")){
                c.setChecked(true);}
            if (savedCode.toLowerCase().contains("k")){
                k.setChecked(true);}
            if (savedCode.toLowerCase().contains("y")){
                y.setChecked(true);}
            if (savedCode.toLowerCase().contains("g")){
                g.setChecked(true);}
            if (savedCode.toLowerCase().contains("p")){
                p.setChecked(true);}
            if (savedCode.toLowerCase().contains("l")){
                l.setChecked(true);}
            if (savedCode.toLowerCase().contains("d")){
                d.setChecked(true);}
            if (savedCode.toLowerCase().contains("w")){
                w.setChecked(true);}
            if (savedCode.toLowerCase().contains("s")){
                s.setChecked(true);}
            if (savedCode.toLowerCase().contains("i")){
                i.setChecked(true);}

        }
        if (savedStickerButton != 0){
            Log.d("prefs", "Loading sticker into views: " + savedStickerButton);
            RadioButton tempB = stickerGroup.findViewById(savedStickerButton);
            tempB.setChecked(true);
        }

    }

    private int getStickerID() {
        RadioGroup radioGroup = findViewById(R.id.stickerGroup);
        Log.d("codeSticker",radioGroup.getCheckedRadioButtonId()+"" );

        int code = 0;
        switch (radioGroup.getCheckedRadioButtonId()){
            case 2131296353:
                code = getApplicationContext().getResources().getIdentifier("sticker_green", "drawable", getPackageName());
                break;
            case 2131296511:
                code = getApplicationContext().getResources().getIdentifier("sticker_yellow", "drawable", getPackageName());
                break;
            case 2131296415:
                code = getApplicationContext().getResources().getIdentifier("sticker_red", "drawable", getPackageName());
                break;
            case 2131296294:
                code = getApplicationContext().getResources().getIdentifier("sticker_baby_green", "drawable", getPackageName());
                break;
            case 2131296295:
                code = getApplicationContext().getResources().getIdentifier("sticker_baby_yellow", "drawable", getPackageName());
                break;
            case 2131296293:
                code = getApplicationContext().getResources().getIdentifier("sticker_baby", "drawable", getPackageName());
                break;
        }
        return code;
    }

    private String generateFinalCode() {
        String code="";
        Spinner mCode = findViewById(R.id.mucusCode);
        Spinner mRedCode = findViewById(R.id.redCode);
        Spinner mFreq = findViewById(R.id.mucusFreq);
        CheckBox c = findViewById(R.id.c);
        CheckBox k = findViewById(R.id.k);
        CheckBox y = findViewById(R.id.y);
        CheckBox g = findViewById(R.id.g);
        CheckBox p = findViewById(R.id.p);
        CheckBox l = findViewById(R.id.L);
        CheckBox d = findViewById(R.id.d);
        CheckBox w = findViewById(R.id.w);
        CheckBox s = findViewById(R.id.s);
        CheckBox i = findViewById(R.id.Intercourse);
        RadioGroup peakGroup = findViewById(R.id.peakGroup);
        RadioButton peak = findViewById(R.id.peak);
        RadioButton peak1 = findViewById(R.id.p1);
        RadioButton peak2 = findViewById(R.id.p2);
        RadioButton peak3 = findViewById(R.id.p3);
        TextView comments = findViewById(R.id.comments);


        switch (mRedCode.getSelectedItemPosition()){
            case 0:
                break;
            case 1:
                code += "B ";
                break;
            case 2:
                code += "VLR ";
                break;
            case 3:
                code += "LR ";
                break;
            case 4:
                code += "MR ";
                break;
            case 5:
                code += "HR ";
                break;

            default:
                break;
        }

        switch (mCode.getSelectedItemPosition()){
            case 0:
                break;
            case 1:
                code += "0";
                break;
            case 2:
                code += "2";
                break;
            case 3:
                code += "4";
                break;
            case 4:
                code += "6";
                break;
            case 5:
                code += "8";
                break;
            case 6:
                code += "10";
                break;

            default:
                break;
        }


        if(c.isChecked()){ code += "c"; }
        if(k.isChecked()){ code += "k"; }
        if(y.isChecked()){ code += "y"; }
        if(g.isChecked()){ code += "g"; }
        if(p.isChecked()){ code += "p"; }
        if(d.isChecked()){ code += "D"; }
        if(w.isChecked()){ code += "W"; }
        if(s.isChecked()){ code += "S"; }
        if(l.isChecked()){ code += "L"; }

        switch (mFreq.getSelectedItemPosition()){
            case 0:
                break;
            case 1:
                code += " x1";
                break;
            case 2:
                code += " x2";
                break;
            case 3:
                code += " x3";
                break;
            case 4:
                code += " AD";
                break;

            default:
                break;
        }


        //add new line if mucus code is present, then add I if that's checked
        if(i.isChecked()){
            if (code.length()>0) {code += "\n";}
            code += "I";
        }

        if(peak.isChecked()){
            if (code.length()>0) {code += "\n";}
            code += "P"; }
        else if(peak1.isChecked()){
            if (code.length()>0) {code += "\n";}
            code += "1"; }
        else if(peak2.isChecked()){
            if (code.length()>0) {code += "\n";}
            code += "2"; }
        else if(peak3.isChecked()){
            if (code.length()>0) {code += "\n";}
            code += "3"; }


        Log.d("codeMucus",code);
        return code;
    }

    CalendarView.OnDateChangeListener myCalendarListener = new CalendarView.OnDateChangeListener(){

        public void onSelectedDayChange(CalendarView view, int year, int month, int day){

            // add one because month starts at 0
            month = month + 1;
            selectedDate = month+"/"+day;
            //Toast.makeText(getApplicationContext(),selectedDate,Toast.LENGTH_SHORT).show();
        }
    };

}
