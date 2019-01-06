package com.missing.nfp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NfpEntry extends AppCompatActivity {

    EditText date;

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

        getWindow().setLayout((int) (width * .8), (int) (height * .8));

        date = findViewById(R.id.date);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d");
        date.setText(dateFormat.format(new Date()));  // it will show Jul 16 2013

        Button save = findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                intent.putExtra("DATE", date.getText().toString()); //value should be your string from the edittext
                intent.putExtra("BUTTONROW",r);
                intent.putExtra("BUTTONCOL",c);
                setResult(1, intent); //The data you want to send back
                finish(); //That's when you onActivityResult() in the first activity will be called
            }
        });
    }

}
