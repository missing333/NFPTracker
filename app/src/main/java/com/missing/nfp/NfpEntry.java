package com.missing.nfp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatRadioButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NfpEntry extends AppCompatActivity {

    CalendarView date;
    long dateInMillis;
    RadioButton noBlood, brownBlood, LR, MR, HR;
    RadioButton noMucusButton, M2Button, M4Button, M6Button, M8Button, M10Button;
    RadioButton freqAD, freq1, freq2, freq3;
    String dateCode, bloodCode, mucusCode, freqCode, modifierCode, commentsCode;
    RadioGroup freqGroup, stickerGroup;
    TextView freqLbl;
    TableRow mods;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfp_entry);

        final int index = getIntent().getIntExtra("INDEX", 0);
        final float lastX = getIntent().getFloatExtra("BUTTONXPOS", 0);
        final float lastY = getIntent().getFloatExtra("BUTTONYPOS", 0);

        ////Set activity to smaller 'popup' window
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        float pct = (float) 0.8;
        getWindow().setLayout((int) (width * pct), (int) (height * pct));



        ////get date
        this.date = findViewById(R.id.calendarView);
        Calendar today = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("M/d/YYYY", Locale.US);
        dateCode = sdf.format(today.getTime());
        this.date.setOnDateChangeListener(myCalendarListener);



        ////init UI
        noBlood = findViewById(R.id.noBlood);
        brownBlood = findViewById(R.id.brownBlood);
        LR = findViewById(R.id.lightRedBlood);
        MR = findViewById(R.id.medRedBlood);
        HR = findViewById(R.id.heavyRedBlood);

        noMucusButton = findViewById(R.id.noMucus);
        M2Button = findViewById(R.id.mucus2);
        M4Button = findViewById(R.id.mucus4);
        M6Button = findViewById(R.id.mucus6);
        M8Button = findViewById(R.id.mucus8);
        M10Button = findViewById(R.id.mucus10);

        freqGroup = findViewById(R.id.freqGroup);
        freqLbl = findViewById(R.id.mucusFreqLabel);
        freqAD = findViewById(R.id.allDay);
        freq1 = findViewById(R.id.freq1);
        freq2 = findViewById(R.id.freq2);
        freq3 = findViewById(R.id.freq3);

        mods = findViewById(R.id.modifiersGroup);

        stickerGroup = findViewById(R.id.stickerGroup);



        //restore button selections
        restoreSelections(index);


        //activate Lubricative parent object
        CheckBox lub = findViewById(R.id.L);
        CheckBox d = findViewById(R.id.d);
        CheckBox w = findViewById(R.id.w);
        CheckBox s = findViewById(R.id.s);
        if (lub.isChecked()) {
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
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                CheckBox d = findViewById(R.id.d);
                CheckBox w = findViewById(R.id.w);
                CheckBox s = findViewById(R.id.s);
                if (isChecked) {
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


        //clear cell button
        Button clear = findViewById(R.id.clearCell);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setCancelable(true);
                builder.setTitle("Clear This Cell?");
                builder.setMessage("This will clear all data for this cell.  Continue?");
                builder.setPositiveButton("Confirm",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Intent intent = getIntent();
                                intent.putExtra("DATE", "");
                                intent.putExtra("INDEX", index);
                                intent.putExtra("COMMENTS", "");
                                intent.putExtra("CODE", "");
                                intent.putExtra("STICKER", 0);


                                SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                                editor.putString(index + "date", "");
                                editor.putString(index + "code", "");
                                editor.putString(index + "comments", "");
                                editor.putInt(index + "sticker", 0);
                                editor.putInt(index + "stickerButton", 0);
                                editor.apply();

                                setResult(1, intent); //The data you want to send back

                                finish();

                            }
                        });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //cancel delete
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();


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
                int stickerID = getStickerID();
                String code = generateFinalCode();
                String combined = code ;

                //add new line if anything else is present, otherwise just add comments
                if (comments.length() > 0) {
                    if (combined.length() > 0) {
                        combined += "\n" + comments;
                    }else {
                        combined = comments;
                    }
                }


                Intent intent = getIntent();
                intent.putExtra("INDEX", index);
                intent.putExtra("DATE", dateCode);
                intent.putExtra("CODE", code);
                intent.putExtra("COMMENTS", comments);
                intent.putExtra("STICKER", stickerID);


                SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putString(index + "date", dateCode);
                editor.putString(index + "code", code);
                editor.putString(index + "comments", comments);
                editor.putInt(index + "sticker", stickerID);
                editor.putInt(index + "stickerButton", radioGroup.getCheckedRadioButtonId());
                editor.putFloat("LASTX", lastX);
                editor.putFloat("LASTY", lastY);
                editor.apply();

                setResult(1, intent); //The data you want to send back

                finish();
            }
        });
    }

    private int getStickerID() {
        RadioButton green = findViewById(R.id.green);
        RadioButton yellow = findViewById(R.id.yellow);
        RadioButton red = findViewById(R.id.red);
        RadioButton babyGreen = findViewById(R.id.babyGreen);
        RadioButton babyYellow = findViewById(R.id.babyYellow);
        RadioButton baby = findViewById(R.id.baby);

        int code = 0;
        if (green.isChecked())
            code = R.drawable.sticker_green;
        else if (yellow.isChecked())
            code = R.drawable.sticker_yellow;
        else if (red.isChecked())
            code = R.drawable.sticker_red;
        else if (babyGreen.isChecked())
            code = R.drawable.sticker_baby_green;
        else if (babyYellow.isChecked())
            code = R.drawable.sticker_baby_yellow;
        else if (baby.isChecked())
            code = R.drawable.sticker_baby;
        Log.d("NFPEntry","sticker Code = " + code);
        return code;
    }

    private String generateFinalCode() {
        modifierCode = "";
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



        if (c.isChecked()) {
            modifierCode += "c";
        }
        if (k.isChecked()) {
            modifierCode += "k";
        }
        if (y.isChecked()) {
            modifierCode += "y";
        }
        if (g.isChecked()) {
            modifierCode += "g";
        }
        if (p.isChecked()) {
            modifierCode += "p";
        }
        if (d.isChecked()) {
            modifierCode += "d";
        }
        if (w.isChecked()) {
            modifierCode += "W";
        }
        if (s.isChecked()) {
            modifierCode += "S";
        }
        if (l.isChecked()) {
            modifierCode += "L ";
        }



        //add new line if mucus code is present, then add I if that's checked
        if (i.isChecked()) {
           modifierCode += "\nI";
        }

        if (peak.isChecked()) {
            modifierCode += "\nP";
        } else if (peak1.isChecked()) {
            modifierCode += "\nP1";
        } else if (peak2.isChecked()) {
            modifierCode += "\nP2";
        } else if (peak3.isChecked()) {
            modifierCode += "\nP3";
        }

        String totalCode = bloodCode + " " + mucusCode + freqCode + " "  + modifierCode;
        Log.d("totalCode","mucus code: " + totalCode);
        return totalCode;
    }

    CalendarView.OnDateChangeListener myCalendarListener = new CalendarView.OnDateChangeListener() {

        public void onSelectedDayChange(CalendarView view, int year, int month, int day) {

            // add one because month starts at 0
            month = month + 1;
            dateCode = month + "/" + day + "/" + year;
            dateInMillis = view.getDate();
            Log.d("dateCode", "date changed to: " + dateInMillis + ", aka " + dateCode);
            final ScrollView sc = findViewById(R.id.nfpScrollView);
            sc.smoothScrollTo((int) 0, (int) 350); // these are your x and y coordinates
        }
    };

    public void onRadioBloodButtonClicked(View view) {
        boolean isSelected = ((AppCompatRadioButton) view).isChecked();
        Log.d("bloodRadio","ID is: " +view.getId());
        switch (view.getId()){
            case R.id.noBlood:
                if(isSelected){
                    noBlood.setTextColor(Color.WHITE);
                    brownBlood.setTextColor(getResources().getColor(R.color.brown));
                    LR.setTextColor(getResources().getColor(R.color.lightRed));
                    MR.setTextColor(getResources().getColor(R.color.medRed));
                    HR.setTextColor(getResources().getColor(R.color.darkRed));
                    bloodCode = "";
                }
                break;
            case R.id.brownBlood:
                if(isSelected) {
                    noBlood.setTextColor(Color.BLACK);
                    brownBlood.setTextColor(Color.WHITE);
                    LR.setTextColor(getResources().getColor(R.color.lightRed));
                    MR.setTextColor(getResources().getColor(R.color.medRed));
                    HR.setTextColor(getResources().getColor(R.color.darkRed));
                    bloodCode = "B";
                }
                break;
            case R.id.lightRedBlood:
                if(isSelected) {
                    noBlood.setTextColor(Color.BLACK);
                    brownBlood.setTextColor(getResources().getColor(R.color.brown));
                    LR.setTextColor(Color.WHITE);
                    MR.setTextColor(getResources().getColor(R.color.medRed));
                    HR.setTextColor(getResources().getColor(R.color.darkRed));
                    bloodCode = "LR";
                    RadioButton tempB = stickerGroup.findViewById(R.id.red);
                    tempB.setChecked(true);
                }
                break;
            case R.id.medRedBlood:
                if(isSelected) {
                    noBlood.setTextColor(Color.BLACK);
                    brownBlood.setTextColor(getResources().getColor(R.color.brown));
                    LR.setTextColor(getResources().getColor(R.color.lightRed));
                    MR.setTextColor(Color.WHITE);
                    HR.setTextColor(getResources().getColor(R.color.darkRed));
                    bloodCode = "MR";
                    RadioButton tempB = stickerGroup.findViewById(R.id.red);
                    tempB.setChecked(true);
                }
                break;
            case R.id.heavyRedBlood:
                if(isSelected) {
                    noBlood.setTextColor(Color.BLACK);
                    brownBlood.setTextColor(getResources().getColor(R.color.brown));
                    LR.setTextColor(getResources().getColor(R.color.lightRed));
                    MR.setTextColor(getResources().getColor(R.color.medRed));
                    HR.setTextColor(Color.WHITE);
                    bloodCode = "HR";
                    RadioButton tempB = stickerGroup.findViewById(R.id.red);
                    tempB.setChecked(true);
                }
                break;
        }
        Log.d("bloodCode","bloodCode is: " + bloodCode);
        final ScrollView sc = findViewById(R.id.nfpScrollView);
        sc.smoothScrollTo((int) 0, (int) 550); // these are your x and y coordinates
    }

    public void onRadioMucusButtonClicked(View view) {
        boolean isSelected = ((AppCompatRadioButton) view).isChecked();
        Log.d("mucusRadio","ID is: " +view.getId());
        switch (view.getId()){
            case R.id.noMucus:
                if(isSelected){
                    noMucusButton.setTextColor(Color.WHITE);
                    M2Button.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    M4Button.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    M6Button.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    M8Button.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    M10Button.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    mucusCode = "0 AD";
                    freqCode = "";
                    if(bloodCode == ""){
                        RadioButton tempB = stickerGroup.findViewById(R.id.green);
                        tempB.setChecked(true);
                    }
                }
                break;
            case R.id.mucus2:
                if(isSelected) {
                    noMucusButton.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    M2Button.setTextColor(Color.WHITE);
                    M4Button.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    M6Button.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    M8Button.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    M10Button.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    mucusCode = "2";
                }
                break;
            case R.id.mucus4:
                if(isSelected) {
                    noMucusButton.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    M2Button.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    M4Button.setTextColor(Color.WHITE);
                    M6Button.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    M8Button.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    M10Button.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    mucusCode = "4";
                }
                break;
            case R.id.mucus6:
                if(isSelected) {
                    noMucusButton.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    M2Button.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    M4Button.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    M6Button.setTextColor(Color.WHITE);
                    M8Button.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    M10Button.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    mucusCode = "6";
                }
                break;
            case R.id.mucus8:
                if(isSelected) {
                    noMucusButton.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    M2Button.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    M4Button.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    M6Button.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    M8Button.setTextColor(Color.WHITE);
                    M10Button.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    mucusCode = "8";
                }
                break;
            case R.id.mucus10:
                if(isSelected) {
                    noMucusButton.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    M2Button.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    M4Button.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    M6Button.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    M8Button.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    M10Button.setTextColor(Color.WHITE);
                    mucusCode = "10";
                    RadioButton tempB = stickerGroup.findViewById(R.id.baby);
                    tempB.setChecked(true);
                }
                break;
        }

        Log.d("mucusRadio", "Mucus Code is: " + mucusCode);
        final ScrollView sc = findViewById(R.id.nfpScrollView);
        sc.smoothScrollTo((int) 0, (int) 850); // these are your x and y coordinates
        if(view.getId() == R.id.noMucus){
            showHideGroups(View.GONE);
        }else {
            showHideGroups(View.VISIBLE);
        }

    }

    public void onRadioFreqButtonClicked(View view) {
        boolean isSelected = ((AppCompatRadioButton) view).isChecked();
        Log.d("freqRadio","ID is: " +view.getId());
        switch (view.getId()){
            case R.id.allDay:
                if(isSelected){
                    freqAD.setTextColor(Color.WHITE);
                    freq1.setTextColor(getResources().getColor(R.color.freq));
                    freq2.setTextColor(getResources().getColor(R.color.freq));
                    freq3.setTextColor(getResources().getColor(R.color.freq));
                    freqCode = " AD";
                }
                break;
            case R.id.freq1:
                if(isSelected){
                    freqAD.setTextColor(getResources().getColor(R.color.freq));
                    freq1.setTextColor(Color.WHITE);
                    freq2.setTextColor(getResources().getColor(R.color.freq));
                    freq3.setTextColor(getResources().getColor(R.color.freq));
                    freqCode = " x1";
                }
                break;
            case R.id.freq2:
                if(isSelected){
                    freqAD.setTextColor(getResources().getColor(R.color.freq));
                    freq1.setTextColor(getResources().getColor(R.color.freq));
                    freq2.setTextColor(Color.WHITE);
                    freq3.setTextColor(getResources().getColor(R.color.freq));
                    freqCode = " x2";
                }
                break;
            case R.id.freq3:
                if(isSelected){
                    freqAD.setTextColor(getResources().getColor(R.color.freq));
                    freq1.setTextColor(getResources().getColor(R.color.freq));
                    freq2.setTextColor(getResources().getColor(R.color.freq));
                    freq3.setTextColor(Color.WHITE);
                    freqCode = " x3";
                }
                break;
        }

        Log.d("freqRadio","freqCode is: " + freqCode);
        final ScrollView sc = findViewById(R.id.nfpScrollView);
        sc.smoothScrollTo((int) 0, (int) 1150); // these are your x and y coordinates
    }

    private void restoreSelections(int index) {
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
        RadioButton peak = findViewById(R.id.peak);
        RadioButton peak1 = findViewById(R.id.p1);
        RadioButton peak2 = findViewById(R.id.p2);
        RadioButton peak3 = findViewById(R.id.p3);
        TextView commentsTV = findViewById(R.id.comments);
        RadioGroup stickerGroup = findViewById(R.id.stickerGroup);
        CalendarView cal = findViewById(R.id.calendarView);

        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        String savedDate = prefs.getString(index + "date", "");
        String savedCode = prefs.getString(index + "code", "");
        String savedComments = prefs.getString(index + "comments", "");
        int savedStickerButton = prefs.getInt(index + "stickerButton", 0);

        if (savedDate.length() > 0) {
            String[] parts = savedDate.split("/");

            int day = Integer.parseInt(parts[1]);
            int month = Integer.parseInt(parts[0]) - 1;  //because calendar months are from 0-11
            int year = Integer.parseInt(parts[2]);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);

            long milliTime = calendar.getTimeInMillis();
            cal.setDate(milliTime, true, true);
            dateCode = savedDate;
        }

        if (savedComments != null) {
            commentsTV.setText(savedComments);
            commentsCode=savedComments;
        }

        if (savedCode != null) {
            Log.d("prefs", "Loading code into views: " + savedCode);

            bloodCode = "";
            if (savedCode.toLowerCase().contains("b")) {
                brownBlood.setChecked(true);
                brownBlood.setTextColor(Color.WHITE);
                bloodCode = "B";
            } else if (savedCode.toLowerCase().contains("lr")) {
                LR.setChecked(true);
                LR.setTextColor(Color.WHITE);
                bloodCode = "LR";
            } else if (savedCode.toLowerCase().contains("mr")) {
                MR.setChecked(true);
                MR.setTextColor(Color.WHITE);
                bloodCode = "MR";
            } else if (savedCode.toLowerCase().contains("hr")) {
                HR.setChecked(true);
                HR.setTextColor(Color.WHITE);
                bloodCode = "HR";
            }

            mucusCode = "";
            if (savedCode.toLowerCase().contains("10")) {
                M10Button.setChecked(true);
                M10Button.setTextColor(Color.WHITE);
                mucusCode = "10";
                showHideGroups(View.VISIBLE);
            } else if (savedCode.toLowerCase().contains("8")) {
                M8Button.setChecked(true);
                M8Button.setTextColor(Color.WHITE);
                mucusCode = "8";
                showHideGroups(View.VISIBLE);
            } else if (savedCode.toLowerCase().contains("6")) {
                M6Button.setChecked(true);
                M6Button.setTextColor(Color.WHITE);
                mucusCode = "6";
                showHideGroups(View.VISIBLE);
            } else if (savedCode.toLowerCase().contains("4")) {
                M4Button.setChecked(true);
                M4Button.setTextColor(Color.WHITE);
                mucusCode = "4";
                showHideGroups(View.VISIBLE);
            } else if (savedCode.toLowerCase().contains("2")) {
                M2Button.setChecked(true);
                M2Button.setTextColor(Color.WHITE);
                mucusCode = "2";
                showHideGroups(View.VISIBLE);
            } else if (savedCode.toLowerCase().contains("0")) {
                noMucusButton.setChecked(true);
                noMucusButton.setTextColor(Color.WHITE);
                mucusCode = "0";
            }

            freqCode = "";
            if (savedCode.toLowerCase().contains("ad")) {
                if(mucusCode != "0"){   //i have AD in the 0 case anyway, so don't need to double up.
                    freqAD.setChecked(true);
                    freqAD.setTextColor(Color.WHITE);
                    freqCode = " AD";
                }
            } else if (savedCode.toLowerCase().contains("x1")) {
                freq1.setChecked(true);
                freq1.setTextColor(Color.WHITE);
                freqCode = " x1";
            } else if (savedCode.toLowerCase().contains("x2")) {
                freq2.setChecked(true);
                freq2.setTextColor(Color.WHITE);
                freqCode = " x2";
            } else if (savedCode.toLowerCase().contains("x3")) {
                freq3.setChecked(true);
                freq3.setTextColor(Color.WHITE);
                freqCode = " x3";
            }

            if (savedCode.toLowerCase().contains("c")) {
                c.setChecked(true);
            }
            if (savedCode.toLowerCase().contains("k")) {
                k.setChecked(true);
            }
            if (savedCode.toLowerCase().contains("y")) {
                y.setChecked(true);
            }
            if (savedCode.toLowerCase().contains("g")) {
                g.setChecked(true);
            }
            if (savedCode.contains("p")) {
                p.setChecked(true);
            }
            if (savedCode.contains("L ")) {
                l.setChecked(true);
            }
            if (savedCode.contains("d")) {
                d.setChecked(true);
            }
            if (savedCode.toLowerCase().contains("w")) {
                w.setChecked(true);
            }
            if (savedCode.toLowerCase().contains("s")) {
                s.setChecked(true);
            }
            if (savedCode.toLowerCase().contains("i")) {
                i.setChecked(true);
            }

            //restore Peak, 1, 2, 3
            if (savedCode.contains("P1")) {
                peak1.setChecked(true);
            } else if (savedCode.contains("P2")) {
                peak2.setChecked(true);
            } else if (savedCode.contains("P3")) {
                peak3.setChecked(true);
            } else if (savedCode.contains("P")) {
                peak.setChecked(true);
            }
        }

        RadioButton tempB = stickerGroup.findViewById(savedStickerButton);
        if (savedStickerButton != 0) {
            try {
                tempB.setChecked(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            stickerGroup.clearCheck();
        }


    }

    public void showHideGroups(int state){
        freqLbl.setVisibility(state);
        freqGroup.setVisibility(state);
        mods.setVisibility(state);
    }
}
