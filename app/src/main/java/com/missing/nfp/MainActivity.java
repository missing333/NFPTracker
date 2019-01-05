package com.missing.nfp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.pdf.PdfDocument;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {


    private static final int NUM_ROWS = 3;
    private static final int NUM_COLS = 40;
    TableLayout tLayout;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_main);
        tLayout = findViewById(R.id.tableLayout1);



        //To switch to RecycleView, go to manifest, change default activity to MainActRecycleView instead.

        populateCells();


    }

    private void populateCells() {
        TableLayout table = findViewById(R.id.tableLayout1);
        for (int row = 0; row < NUM_ROWS; row++){
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.WRAP_CONTENT,
                    TableLayout.LayoutParams.MATCH_PARENT,
                    1.0f
            ));
            table.addView(tableRow);

            for (int col = 0; col < NUM_COLS; col++){
                Button button = new Button(this);
                button.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.MATCH_PARENT,
                        1.0f
                ));

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        intent = new Intent(MainActivity.this, NfpEntry.class);
                        startActivityForResult(intent,1);
                    }
                });

                tableRow.addView(button);

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Retrieve data in the intent
        String editTextValue = data.getStringExtra("DATE");
        Toast.makeText(this,"Date is: " + editTextValue, Toast.LENGTH_SHORT).show();
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
            return true;
        }
        else if (id == R.id.action_print) {
            printPDF();
        }
        return super.onOptionsItemSelected(item);
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
                    1);

        }
        else {
            // Permission has already been granted
        }



        //start creating PDF here.
        Toast.makeText(this,"Creating PDF..." ,Toast.LENGTH_SHORT ).show();

        String extstoragedir = Environment.getExternalStorageDirectory().toString();
        File fol = new File(extstoragedir, "NFPapp");
        File folder=new File(fol,"pdf archive");
        if(!folder.exists()) {
            boolean bool = folder.mkdirs();
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

            Toast.makeText(this,"Done with PDF steps" ,Toast.LENGTH_SHORT ).show();

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

        finish();

    }
}
