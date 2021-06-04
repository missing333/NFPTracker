package com.missing.nfp;

import android.Manifest;
import android.app.Activity;
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
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.LruCache;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ActivityMain extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    SharedPreferences sharedPreferences;
    List<Cell> AllCells;
    int NumRows;
    int NumCols;
    final static int defaultNumRows = 3;
    final static int defaultNumCols = 35;
    RecyclerViewAdapter myAdapter;
    RecyclerView myRecycleView;
    NestedScrollView myScrollView;
    androidx.recyclerview.widget.GridLayoutManager gridLayoutManager;
    Context mainContext = this;
    FloatingActionButton fab1, fab2, fab3, fab4, fabmenu;
    boolean isFABOpen=false;
    //TODO: make scrolling horizontally more forgiving
    //TODO: update add/rem row/col button icons


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Starting On Create");
        setContentView(R.layout.activity_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainContext);
        NumRows = sharedPreferences.getInt("numRows", defaultNumRows);  //add one for header row, one for footer row
        NumCols = sharedPreferences.getInt("numCols", defaultNumCols);  //add one for Add/Subtract column
        Log.d(TAG, "Cycles(w/header&footer): " + NumRows + ", Days(w/add&subtr col): " + NumCols);

        AllCells = new ArrayList<>();

        if (isWritePermissionNeeded()) {
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(mainContext);
            builder.setMessage("Permission to access Storage is required for this app to create PDF backups.")
                    .setTitle("Permission required");

            builder.setPositiveButton("I Understand",
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {
                            Log.i(TAG, "OK Clicked");
                            makeWritePermissionRequest();
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        }


        fab1 = findViewById(R.id.idRowAdd);
        fab2 = findViewById(R.id.idRowDelete);
        fab3 = findViewById(R.id.idColDelete);
        fab4 = findViewById(R.id.idColAdd);
        fabmenu = findViewById(R.id.idMenu);

        myRecycleView = findViewById(R.id.id_recyclerview);
        myScrollView = findViewById(R.id.id_scrollView);
        myAdapter = new RecyclerViewAdapter(this, AllCells);
        myAdapter.setNumRows(NumRows);
        myAdapter.setNumCols(NumCols);
        gridLayoutManager = new GridLayoutManager(mainContext, NumRows, LinearLayoutManager.HORIZONTAL,false);

        myRecycleView.setLayoutManager(gridLayoutManager);

        myRecycleView.setNestedScrollingEnabled(true);
        myRecycleView.setAdapter(myAdapter);

        populateCells(NumRows);
        scrollToLastPickedCell();

        startAlarming(getApplicationContext());

        Log.d("timing", "End On Create");
    }

    protected boolean isWritePermissionNeeded(){
        if (ContextCompat.checkSelfPermission(mainContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG,"Write pemissions are needed.");
            return true;
        }else{
            Log.d(TAG,"Write pemissions are granted.");
            return false;
        }
    }
    protected void makeWritePermissionRequest(){

        //checking for permission to write
        if (isWritePermissionNeeded()) {

            // Permission is not granted
            // request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    2);

        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        //get scroll positions
        int scrollY = myScrollView.getScrollY();
        RecyclerView recyclerView = findViewById(R.id.id_recyclerview);
        View firstChild = recyclerView.getChildAt(0);
        int firstVisiblePosition = recyclerView.getChildAdapterPosition(firstChild);

        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putInt("LASTINDEX", firstVisiblePosition);
        editor.putInt("LASTROW", scrollY);
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPref.getBoolean("autoSave", true)){
            printPDF("autoSave", false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //this method handles the return data from the NFP Entry screen.

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 333) {
            //Retrieve nfp entry data
            try {
                int index = data.getIntExtra("INDEX", -1);
                String activeDate = data.getStringExtra("DATE");
                String code = data.getStringExtra("CODE");
                String comments = data.getStringExtra("COMMENTS");
                int stickerID = data.getIntExtra("STICKER", 0);

                AllCells.set(index, new Cell(activeDate,code,comments,stickerID));

                myAdapter.notifyItemChanged(index);

            } catch (Exception e) {
                e.printStackTrace();
            }
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


        if (id == R.id.action_settings) {
            Intent intent = new Intent(ActivityMain.this, NotifSettings.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_print) {
            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MMM-dd", Locale.US);
            String formattedDate = df.format(c);
            if(!isWritePermissionNeeded())
                printPDF(formattedDate, true);
            return true;
        } else if (id == R.id.action_view) {
            openDownloads(this);
            return true;
        } else if (id == R.id.action_clearAll) {
            clearAllFields();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static void openDownloads(@NonNull Activity activity) {
        if (isSamsung()) {
            Intent intent = activity.getPackageManager()
                    .getLaunchIntentForPackage("com.sec.android.app.myfiles");
            if (intent != null) {
                intent.setAction("samsung.myfiles.intent.action.LAUNCH_MY_FILES");
            }
            if (intent != null) {
                intent.putExtra("samsung.myfiles.intent.extra.START_PATH",
                        getChartiFile());
            }
            activity.startActivity(intent);
        }
        else {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            Intent chooser = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.parse(getChartiFile());
            chooser.setDataAndType(uri,"*/*");
            try {
                activity.startActivity(chooser);
            }
            catch (android.content.ActivityNotFoundException ex)
            {
                Toast.makeText(activity, "Please install a File Manager.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
    public static boolean isSamsung() {
        String manufacturer = Build.MANUFACTURER;
        if (manufacturer != null) return manufacturer.toLowerCase().equals("samsung");
        return false;
    }
    public static String getChartiFile() {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/Charti";
        Log.d("chartiPath", "path = " + path);
        return path;
    }

    public void populateCells(int newRows) {
        Log.d("timing", "Starting Populate Cells");

        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        NumRows = sharedPreferences.getInt("numRows", defaultNumRows);
        NumCols = sharedPreferences.getInt("numCols", defaultNumCols);
        boolean legalNoticeUnderstood = prefs.getBoolean("legalNoticeUnderstood", false);
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
                            editor.putBoolean("legalNoticeUnderstood", true).apply();
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


        AllCells.clear();
        for (int i = 0; i < (NumRows * NumCols) + NumRows ; i++){
            //pull saved info for each cell
            String savedDate = prefs.getString(i + "date", "");
            String savedCode = prefs.getString(i + "code", "");
            String savedComments = prefs.getString(i + "comments", "");
            int savedStickerButton = prefs.getInt(i + "sticker", 0);


            if (newRows > NumRows){
                //increasing, so add buffer cell each column.
                if (i % NumRows == 0 && i > 0){
                    //just add new spacer cell here.  adding the saved info comes later
                    AllCells.add(new Cell());
                }
            }
            if (newRows < NumRows) {
                //decreasing, so remove the NumRow'th cell.
                if (i % NumRows != NumRows - 1) {   //if NOT last row, add cell to AllCells, otherwise, skip it.
                    AllCells.add(new Cell(savedDate, savedCode, savedComments, savedStickerButton));
                }
            }else{
                //add saved info if adding more rows, or same number of rows.
                AllCells.add(new Cell(savedDate, savedCode, savedComments, savedStickerButton));
            }
        }

        if (newRows > NumRows){
            //add the last buffer cell after we are all done when making MORE rows.
            AllCells.add(new Cell());
        }


        //re-save info for each cell so that it displays correctly for the next re-populate
        for (int i = 0; i < AllCells.size(); i++){
            prefs.edit().putString(i + "date", AllCells.get(i).getDate()).apply();
            prefs.edit().putString(i + "code", AllCells.get(i).getCode()).apply();
            prefs.edit().putString(i + "comments", AllCells.get(i).getComments()).apply();
            prefs.edit().putInt(i + "sticker", AllCells.get(i).getSticker()).apply();
        }

        myAdapter.notifyDataSetChanged();

        Log.d("timing", "End Populate Cells");
    }


    private void clearAllCells(){
        AllCells.clear();
        for(int i=0;i<NumRows * NumCols;i++){
            AllCells.add(new Cell("", "", "", 0));
            SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
            editor.putString(i + "date", "");
            editor.putString(i + "code", "");
            editor.putString(i + "comments", "");
            editor.putInt(i + "sticker", 0);
            editor.apply();
        }
        myAdapter.notifyDataSetChanged();
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
                        Log.d("clearAll","Clearing All Cells");
                        Date c = Calendar.getInstance().getTime();
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MMM-dd", Locale.US);
                        String formattedDate = df.format(c);

                        //auto-save before clearing everything
                        Toast.makeText(ActivityMain.this, "Saving...", Toast.LENGTH_LONG).show();
                        printPDF( "Before Clear on " + formattedDate, false);

                        //update prefs
                        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                        editor.putBoolean("legalNoticeUnderstood", false);
                        editor.putInt("LASTINDEX", 0);
                        editor.putInt("LASTROW", 0);
                        editor.apply();

                        clearAllCells();
                        populateCells(NumRows);
                        scrollToLastPickedCell();
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

    public void scrollToLastPickedCell() {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        final int lastY = Math.round(prefs.getInt("LASTROW", 0));
        final int lastIndex = prefs.getInt("LASTINDEX", 0);
        //Log.d(TAG,"LastIndex: " + lastIndex + ", LastY: " + lastY);

        //scroll in x
        myRecycleView.smoothScrollToPosition(lastIndex + NumRows*3);

        //scroll in y
        myScrollView.post(new Runnable() {
            @Override
            public void run() {
                myScrollView.smoothScrollTo(0, lastY); // these are your x and y coordinates
            }
        });


    }

    public static void startAlarming(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean notifPref = sharedPref.getBoolean("notifications_new_message", true);

        if (notifPref) {
            //daily notifications
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            String timePref = sharedPref.getString("timePref_Key", "7:0");
            String[] separated = new String[0];
            if (timePref != null) {
                separated = timePref.split(":");
            }
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void printPDF(String pdfName, Boolean displayPDF) {

        //Permission has already been granted
        //start creating PDF here.
        File fol = new File(getChartiFile());
        if(!fol.exists()) {
            fol.mkdirs();
        }
        try {
            File file = new File(fol,  pdfName + ".pdf");
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);

            if (displayPDF){
                Toast.makeText(ActivityMain.this, "Saving...", Toast.LENGTH_LONG).show();
            }
            Bitmap bm = getScreenshotFromRecyclerView(myRecycleView);

            int reducer = 7;
            int pageWidth = 842;
            int pageHeight = 595;
            PdfDocument document = null;

            boolean oldWayToPrint = false;
            if (oldWayToPrint) {
                //Page1
                int right = bm.getWidth()/reducer;
                int bottom = bm.getHeight()/reducer;
                document = new PdfDocument();
                PdfDocument.PageInfo pageInfo1 = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
                PdfDocument.Page page1 = document.startPage(pageInfo1);
                page1.getCanvas().drawBitmap(bm, null, new Rect(10, 0, right, bottom), null);
                document.finishPage(page1);

                //Page2
                int left = -bm.getWidth()/(reducer*2);
                PdfDocument.PageInfo pageInfo2 = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create();
                PdfDocument.Page page2 = document.startPage(pageInfo2);
                page2.getCanvas().drawBitmap(bm, null, new Rect(left, 0, -left, bottom), null);
                document.finishPage(page2);
            } else {
                NumRows = sharedPreferences.getInt("numRows", defaultNumRows);  //add one for header row, one for footer row
                NumCols = sharedPreferences.getInt("numCols", defaultNumCols);  //add one for Add/Subtract column
                int edge = 1650;
                int bottom = 100 * NumRows;
                if (NumCols > 0) {
                    //Page1
                    document = new PdfDocument();
                    PdfDocument.PageInfo pageInfo1 = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
                    PdfDocument.Page page1 = document.startPage(pageInfo1);
                    page1.getCanvas().drawBitmap(bm, null, new Rect(10, 0, edge, bottom), null);
                    document.finishPage(page1);
                }

                edge = edge /2;
                if (NumCols > 18) {
                    //Page2
                    PdfDocument.PageInfo pageInfo2 = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create();
                    PdfDocument.Page page2 = document.startPage(pageInfo2);
                    page2.getCanvas().drawBitmap(bm, null, new Rect(-edge, 0, edge, bottom), null);
                    document.finishPage(page2);
                }
            }





            // write the document content
            document.writeTo(fOut);
            document.close();

            if (Build.VERSION.SDK_INT >= 24) {
                try {
                    Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                    m.invoke(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            if (displayPDF) {
                PDFTools.openPDF(this, Uri.fromFile(file));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        //recreate();
    }

    public Bitmap getScreenshotFromRecyclerView(RecyclerView view) {
        RecyclerView.Adapter adapter = view.getAdapter();
        Bitmap bigBitmap = null;
        if (adapter != null) {
            int size = adapter.getItemCount();
            int width = 0;
            Paint paint = new Paint();
            int iHeight = 0;
            int iWidth = 0;
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

            // Use 1/8th of the available memory for this memory cache.
            final int cacheSize = maxMemory / 8;
            LruCache<String, Bitmap> bitmaCache = new LruCache<>(cacheSize);

            //step through all items in the list
            for (int i = 0; i < size; i++) {
                RecyclerView.ViewHolder holder = adapter.createViewHolder(view, adapter.getItemViewType(i));
                adapter.onBindViewHolder(holder, i);
                holder.itemView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(view.getHeight(), View.MeasureSpec.EXACTLY));
                iWidth = holder.itemView.getMeasuredWidth();
                holder.itemView.measure(View.MeasureSpec.makeMeasureSpec(view.getWidth(), View.MeasureSpec.EXACTLY),
                                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                iHeight = holder.itemView.getMeasuredHeight();
                holder.itemView.layout(0, 0, iWidth, iHeight);
                holder.itemView.setDrawingCacheEnabled(true);
                holder.itemView.buildDrawingCache();
                Bitmap drawingCache = holder.itemView.getDrawingCache();
                if (drawingCache != null) {
                    bitmaCache.put(String.valueOf(i), drawingCache);
                }
                width += iWidth;
                //iHeight = tempHeight;
            }

            NumRows = sharedPreferences.getInt("numRows", defaultNumRows);  //add one for header row, one for footer row

            bigBitmap = Bitmap.createBitmap(width/NumRows, view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            Canvas bigCanvas = new Canvas(bigBitmap);
            bigCanvas.drawColor(Color.WHITE);


            //this part draws the cache onto the BigBitmap
            float tempWidth = 0f;
            float tempHeight = 0f;
            for (int i = 0; i < size; i++) {
                Bitmap bitmap = bitmaCache.get(String.valueOf(i));
                assert bitmap != null;
                bigCanvas.drawBitmap(bitmap, tempWidth, tempHeight, paint);
                tempHeight += bitmap.getHeight();
                bitmap.recycle();
                if (i % NumRows == NumRows-1){
                    tempWidth += iWidth;
                    tempHeight = 0;
                }
            }

        }

        return bigBitmap;
    }

    public void AddRow(View view) {
        if (isProInstalled(this)){
            final int newRows = sharedPreferences.getInt("numRows",defaultNumRows)+1;
            if (newRows > 7){
                Toast.makeText(this,"Can't Add more rows than this.",Toast.LENGTH_SHORT).show();
            }else {
                myAdapter.setNumRows(newRows);
                gridLayoutManager = new GridLayoutManager(mainContext, newRows, LinearLayoutManager.HORIZONTAL,false);
                //staggeredGridLayoutManager = new StaggeredGridLayoutManager(newRows, LinearLayoutManager.HORIZONTAL);
                //myRecycleView.setLayoutManager(staggeredGridLayoutManager);
                myRecycleView.setLayoutManager(gridLayoutManager);
                Log.d(TAG,"NumRows: " + newRows);
                populateCells(newRows);
                sharedPreferences.edit().putInt("numRows",newRows).apply();
            }

        }else{
            //launch playstore activity to buy pro version
            startActivity(new Intent(this, ActivityPlayStorePrompt.class));
        }
    }

    public void DeleteRow(View view) {
        if (isProInstalled(this)){
            final int newRows = sharedPreferences.getInt("numRows",defaultNumRows)-1;
            if (newRows <= 1){
                Toast.makeText(this,"Can't Delete the last row",Toast.LENGTH_SHORT).show();
            }else {
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(mainContext);
                builder.setMessage("This row will be deleted.  Data will not be saved.")
                        .setTitle("Caution!");

                builder.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                myAdapter.setNumRows(newRows);
                                gridLayoutManager = new GridLayoutManager(mainContext, newRows, LinearLayoutManager.HORIZONTAL,false);
                                myRecycleView.setLayoutManager(gridLayoutManager);
                                Log.d(TAG,"NumRows: " + newRows);
                                populateCells(newRows);
                                sharedPreferences.edit().putInt("numRows",newRows).apply();
                            }
                        });
                builder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }else{
            //launch playstore activity to buy pro version
            startActivity(new Intent(this, ActivityPlayStorePrompt.class));
        }
    }

    public void AddCol(View view) {
        if (isProInstalled(this)){
            //scroll to the end so you can see it being added/removed
            //scroll in x
            myRecycleView.smoothScrollToPosition(myAdapter.getItemCount());
            //scroll in y
            myScrollView.post(new Runnable() {
                @Override
                public void run() {
                    myScrollView.smoothScrollTo(0, 0); // these are your x and y coordinates
                }
            });

            int newCols = NumCols+1;
            myAdapter.setNumCols(newCols);
            sharedPreferences.edit().putInt("numCols",newCols).apply();
            Log.d(TAG,"NumCols: " + newCols);
            populateCells(sharedPreferences.getInt("numRows",defaultNumRows));

        }else{
            //launch playstore activity to buy pro version
            startActivity(new Intent(this, ActivityPlayStorePrompt.class));
        }
    }

    public void DeleteCol(View view) {
        if (isProInstalled(this)){
            //scroll to the end so you can see it being added/removed
            //scroll in x
            myRecycleView.smoothScrollToPosition(myAdapter.getItemCount());
            //scroll in y
            myScrollView.post(new Runnable() {
                @Override
                public void run() {
                    myScrollView.smoothScrollTo(0, 0); // these are your x and y coordinates
                }
            });
            int newCols = NumCols-1;
            myAdapter.setNumCols(newCols);
            sharedPreferences.edit().putInt("numCols",newCols).apply();
            Log.d(TAG,"NumCols: " + newCols);
            populateCells(sharedPreferences.getInt("numRows",defaultNumRows));

        }else{
            //launch playstore activity to buy pro version
            startActivity(new Intent(this, ActivityPlayStorePrompt.class));
        }
    }


    protected boolean isProInstalled(Context context) {
        PackageManager manager = context.getPackageManager();
        /*try {
            String packageName = context.getPackageName();
            if (manager.checkSignatures(packageName, "com.missing.chartideluxe") == PackageManager.SIGNATURE_MATCH) {
                //Pro key installed, and signatures match
                return true;
            }
        } catch (Exception e) {
            return false;
        }*/  //old method.  didn't work with chartiDeluxe.
        try {
            //TODO: uncomment this when ready to deploy chartiDeluxe.
            //manager.getPackageInfo("com.missing.chartideluxe", 0);
            manager.getPackageInfo(context.getPackageName(), 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public void ToggleMenu(View view) {
        if(!isFABOpen){
            isFABOpen=true;
            fabmenu.animate().rotation(45);
            fab1.animate().translationY(-getResources().getDimension(R.dimen.standard_55)).rotation(90);
            fab2.animate().translationY(-getResources().getDimension(R.dimen.standard_105)).rotation(-90);
            fab3.animate().translationX(-getResources().getDimension(R.dimen.standard_105)).rotation(180);
            fab4.animate().translationX(-getResources().getDimension(R.dimen.standard_55));
        }else{
            isFABOpen=false;
            fabmenu.animate().rotation(0);
            fab1.animate().translationY(0);
            fab2.animate().translationY(0);
            fab3.animate().translationX(0);
            fab4.animate().translationX(0);
        }
    }
}


