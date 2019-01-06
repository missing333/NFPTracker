package com.missing.nfp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class NfpEntry extends AppCompatActivity {

    CalendarView date;
    String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfp_entry);

        final int r = getIntent().getIntExtra("BUTTONROW", -1);
        final int c = getIntent().getIntExtra("BUTTONCOL", -1);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;
        float pct = (float) 0.8;
        getWindow().setLayout((int) (width * pct), (int) (height * pct));


        date = findViewById(R.id.calendarView);
        Calendar today = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("M/d");
        selectedDate = sdf.format(today.getTime());
        date.setOnDateChangeListener(myCalendarListener);




        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.mucusCodesArray,R.layout.spinner_center_item);
        // set whatever dropdown resource you want
        adapter.setDropDownViewResource(R.layout.spinner_center_item);

        Spinner mCodes = findViewById(R.id.mucusCode);
        mCodes.setAdapter(adapter);

        adapter = ArrayAdapter.createFromResource(
                this, R.array.mucusFreqArray,R.layout.spinner_center_item);
        // set whatever dropdown resource you want
        adapter.setDropDownViewResource(R.layout.spinner_center_item);
        Spinner mFreqs = findViewById(R.id.mucusFreq);
        mFreqs.setAdapter(adapter);


        Button save = findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = getIntent();
                intent.putExtra("DATE", selectedDate);
                intent.putExtra("BUTTONROW",r);
                intent.putExtra("BUTTONCOL",c);
                setResult(1, intent); //The data you want to send back
                finish();
            }
        });
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
