package com.missing.nfp;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    private static final int NUM_ROWS = 6;
    private static final int NUM_COLS = 35;
    private static final int HEADER_TEXT_SIZE = 22;
    Button[][] btnArray = new Button[NUM_ROWS][NUM_COLS];
    TableLayout tLayout;
    Intent intent;
    String activeDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_main);
        tLayout = findViewById(R.id.tableLayout1);

        populateCells("fromPrefs");
        startAlarming(getApplicationContext());



    }

    private void scrollToLastPickedCell() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms


                SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
                final float lastX = prefs.getFloat("LASTX", 0);
                final float lastY = prefs.getFloat("LASTY", 0);

                //scroll horizontally **more important for this app
                final HorizontalScrollView hv = findViewById(R.id.horizontalView);
                hv.smoothScrollTo((int) lastX-200, (int) lastY); // these are your x and y coordinates


                //scroll vertically
                final ScrollView sc = findViewById(R.id.layout);
                sc.smoothScrollTo((int) lastX-200, (int) lastY); // these are your x and y coordinates

            }
        }, 1000);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (requestCode == 1) {
            //Retrieve nfp entry data
            try {
                activeDate = data.getStringExtra("DATE");
                int btnRow = data.getIntExtra("BUTTONROW", -1);
                int btnCol = data.getIntExtra("BUTTONCOL", -1);
                String code = data.getStringExtra("CODE");
                int stickerID = data.getIntExtra("STICKERID", 0);

                btnArray[btnRow][btnCol].setCompoundDrawablesWithIntrinsicBounds(0, stickerID, 0, 0);
                String combined = activeDate +"\n" +code;
                Typeface font = Typeface.createFromAsset(getAssets(), "fonts/NotoSerifTC-Regular.otf");
                btnArray[btnRow][btnCol].setTypeface(font);
                btnArray[btnRow][btnCol].setTransformationMethod(null);
                btnArray[btnRow][btnCol].setText(combined);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == 2 ){
            printPDF();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, NotifSettings.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.action_print) {
            printPDF();
            return true;
        }
        else if (id == R.id.action_clearAll) {
            clearAllFields();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearAllFields() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Clear Everything?");
        builder.setMessage("This will delete all information in your chart.  Are you sure you want to continue?");
        builder.setPositiveButton("Confirm",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //continue deleting
                        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                        editor.putBoolean("legalNoticeUnderstood", false);
                        editor.putFloat("LASTX", 0);
                        editor.putFloat("LASTY", 0);
                        editor.apply();
                        tLayout.removeAllViews();
                        populateCells("reset");
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

    private void populateCells(String tag) {
        TableLayout table = findViewById(R.id.tableLayout1);

        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        Boolean legalNoticeUnderstood = prefs.getBoolean("legalNoticeUnderstood", false);
        if (!legalNoticeUnderstood) {
            //Legal Notice:
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle(R.string.legalLabel);
            builder.setMessage(R.string.legal_Prompt);
            builder.setPositiveButton(R.string.understand,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //continue deleting
                            SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                            editor.putBoolean("legalNoticeUnderstood", true);
                            editor.apply();
                        }
                    });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //close app
                    finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }


        //First Header Row
        TableRow tableRow = new TableRow(this);
        tableRow.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT,
                TableLayout.LayoutParams.MATCH_PARENT,
                1.0f
        ));
        table.addView(tableRow);
        for (int col = 1; col < NUM_COLS + 1; col++) {
            TextView label = new TextView(this);
            label.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.MATCH_PARENT,
                    1.0f
            ));
            label.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            label.setTextColor(Color.BLACK);
            label.setTextSize(HEADER_TEXT_SIZE);
            label.setTypeface(null, Typeface.BOLD);
            label.setBackgroundResource(R.drawable.back);
            label.setText(col + "");
            tableRow.addView(label);
        }


        //All Subsequent Rows
        for (int i = 0; i < btnArray.length; i++) {
            tableRow = new TableRow(this);
            TableRow.LayoutParams params = new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.MATCH_PARENT,
                    1.0f
            );
            params.setMargins(0,0 ,0 ,60 );
            tableRow.setLayoutParams(params);
            table.addView(tableRow);
            for (int j = 0; j < btnArray[i].length; j++) {
                btnArray[i][j] = new Button(this);
                btnArray[i][j].setLayoutParams(params);
                GradientDrawable gd = new GradientDrawable();
                gd.setColor(0xFFFFFFFF); // Changes this drawbale to use a single color instead of a gradient
                gd.setCornerRadius(5);
                gd.setStroke(1, 0xFF000000);
                btnArray[i][j].setBackground(gd);
                btnArray[i][j].setPadding(10, -5,10 ,0 );


                if (tag.equals("reset")){
                    SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                    editor.putString("r"+i+"c"+j+"date", null);
                    editor.putString("r"+i+"c"+j+"code", null);
                    editor.putString("r"+i+"c"+j+"comments", null);
                    editor.putInt("r"+i+"c"+j+"sticker", 0);
                    editor.putInt("r"+i+"c"+j+"stickerButton", 0);
                    editor.apply();
                    btnArray[i][j].setText("");
                    btnArray[i][j].setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
                else{
                    //pull from preferences
                    prefs = getSharedPreferences("Settings", MODE_PRIVATE);
                    String savedDate = prefs.getString("r"+i+"c"+j+"date", null);
                    String savedCode = prefs.getString("r"+i+"c"+j+"code", null);
                    String savedComments = prefs.getString("r"+i+"c"+j+"comments", null);
                    int savedSticker = prefs.getInt("r"+i+"c"+j+"sticker", 0);
                    Log.d("prefs", savedCode + ", " + savedComments + ", " + savedSticker);
                    String combined = savedDate;

                    if (savedCode != null) {
                        combined += "\n" + savedCode;
                    }
                    if (savedComments != null) {
                        combined += "\n" + savedComments;
                    }

                    //set button stuff
                    Typeface font = Typeface.createFromAsset(getAssets(), "fonts/NotoSerifTC-Regular.otf");
                    btnArray[i][j].setTypeface(font);
                    btnArray[i][j].setTransformationMethod(null);
                    btnArray[i][j].setText(combined);
                    if (savedSticker != 0 ){
                        btnArray[i][j].setCompoundDrawablesWithIntrinsicBounds(0, savedSticker, 0, 0);}
                }


                final int finalJ = j;
                final int finalI = i;
                btnArray[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        intent = new Intent(MainActivity.this, NfpEntry.class);
                        intent.putExtra("BUTTONROW", finalI);
                        intent.putExtra("BUTTONCOL", finalJ);
                        float testX = btnArray[finalI][finalJ].getX();
                        float testY = 200 * finalI;
                        intent.putExtra("BUTTONXPOS", testX);
                        intent.putExtra("BUTTONYPOS", testY);
                        startActivityForResult(intent, 1);
                    }
                });
                tableRow.addView(btnArray[i][j]);
            }
        }

        scrollToLastPickedCell();
    }

    private void printPDF() {


        //checking for permission to write
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    2);


            Toast.makeText(this,"Please Print again after you grant permission" , Toast.LENGTH_LONG).show();
        }
        else {
            //Permission has already been granted
            //start creating PDF here.
            String extstoragedir = Environment.getExternalStorageDirectory().toString();
            File fol = new File(extstoragedir, "NFPapp");
            File folder=new File(fol,"pdf archive");
            if(!folder.exists()) {
                folder.mkdirs();
            }
            try {
                final File file = new File(folder, "sample.pdf");
                file.createNewFile();
                FileOutputStream fOut = new FileOutputStream(file);


                Bitmap bm = PDFTools.getScreenshotFromTableView(tLayout);

                PdfDocument document = new PdfDocument();
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bm.getWidth()+100, bm.getHeight()+100, 1).create();
                PdfDocument.Page page = document.startPage(pageInfo);


                // draw table on the page
                Canvas canvas = page.getCanvas();
                canvas.drawBitmap(bm, null, new Rect(50, 50, bm.getWidth(),bm.getHeight()), null);


                // finish the page
                document.finishPage(page);
                // add more pages
                // write the document content
                document.writeTo(fOut);
                document.close();


                if(Build.VERSION.SDK_INT>=24){
                    try{
                        Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                        m.invoke(null);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }


                PDFTools.openPDF(this, Uri.fromFile(file));
            } catch (IOException e) {
                e.printStackTrace();
            }

            recreate();
        }


    }

    public static void startAlarming(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean notifPref = sharedPref.getBoolean("notifications_new_message", true);

        if (notifPref) {
            //daily notifications
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            String timePref = sharedPref.getString("timePref_Key", "7:0");
            String[] separated = timePref.split(":");
            int hour = Integer.parseInt(separated[0]);
            int min = Integer.parseInt(separated[1]);

            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, min);
            calendar.set(Calendar.SECOND, 0);
            if (calendar.getTime().compareTo(new Date()) < 0) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                Log.d("nfpNotifs", "added 1 day since alarm was set to the past");
            }

            Intent intent1 = new Intent(context, MyReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            am.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
            //am.setExactAndAllowWhileIdle(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);

            SimpleDateFormat format = new SimpleDateFormat("EEEE, MMMM d, yyyy 'at' h:mm a", Locale.US);
            Log.d("nfpNotifs", "I am going to send a notif at " + format.format(calendar.getTime()));
            //Toast.makeText(context, "Next notification at "+format.format(calendar.getTime()), Toast.LENGTH_LONG).show();
        } else {
            Log.d("nfpNotifs", "I am NOT going to send notifications.");
        }
    }
}
