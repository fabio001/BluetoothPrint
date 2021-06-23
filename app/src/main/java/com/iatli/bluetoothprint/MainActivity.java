package com.iatli.bluetoothprint;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {
    private static final String TAG ="YENORSAN";
    private static final String BASE_URL = "http://rafetdurgut.com/Yenorsan/";
    public static final int PERMISSION_BLUETOOTH = 1551;
    private static final String HIDDEN_URL_NAME = "print";

    private BluetoothPrinter bluetoothPrinter=null;

    //private ImageView imageView;

    private WebView webView=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        //imageView = findViewById(R.id.imgview);

        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d(TAG, url);
                view.loadUrl(url);
                return true;
            }


            //convert to bitmap image and print
            public void onPageFinished(WebView view, String url) {
                //if it contains print in URL
                if(url.contains(HIDDEN_URL_NAME)){
                    Log.d(TAG, "Web sayfasinin görüntüsü için onFinished fonksiyonuna gelindi:" + url);
                    if(bluetoothPrinter == null){
                        bluetoothPrinter = new BluetoothPrinter();
                    }
                    //get the formatted text from URL
                    Toast.makeText(MainActivity.this, getString(R.string.printing), Toast.LENGTH_SHORT).show();
                    Thread t = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Bitmap reportBitmap = getBitmapImageFromWebView();
                                    //imageView.setImageBitmap(reportBitmap);
                                    printUrl(url, reportBitmap);
                                }
                            });

                        }
                    });
                    t.start();
                }
            }
        });

        webView.loadUrl(BASE_URL);
        Log.d(TAG, "App is loaded");
    }

    private Bitmap getBitmapImageFromWebView(){

        webView.measure(View.MeasureSpec.makeMeasureSpec(
                View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        webView.layout(0, 0, webView.getMeasuredWidth(),
                webView.getMeasuredHeight());
        webView.setDrawingCacheEnabled(true);
        webView.buildDrawingCache();
        Bitmap bm = Bitmap.createBitmap(webView.getMeasuredWidth(),
                webView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas bigcanvas = new Canvas(bm);
        Paint paint = new Paint();
        int iHeight = bm.getHeight();
        bigcanvas.drawBitmap(bm, 0, iHeight, paint);
        webView.draw(bigcanvas);
        return bm;

    }

    private String parseImage(Bitmap fullImage, EscPosPrinter printer){
        StringBuilder printText = new StringBuilder();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        fullImage.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        byte[] decodedString = Base64.decode(byteArray, Base64.DEFAULT);

        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        int width = decodedByte.getWidth(), height = decodedByte.getHeight();

        StringBuilder textToPrint = new StringBuilder();
        for(int y = 0; y < height; y += 256) {
            Bitmap bitmap = Bitmap.createBitmap(decodedByte, 0, y, width, (y + 256 >= height) ? height - y : 256);
            textToPrint.append("[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, bitmap) + "</img>\n");
        }

        return printText.toString();
    }

    //public void yazdir(View view) {
    private void printUrl(String urlToPrint, Bitmap reportBitmap){
        Log.d(TAG, "Printing url "+ urlToPrint);
        //request bluetooth permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Bluetooth permission is sent");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, MainActivity.PERMISSION_BLUETOOTH);
        } else {
            Log.d(TAG, "Bluetooth is OK. Now downloading text from given URL" + urlToPrint);

            if(bluetoothPrinter == null){
                bluetoothPrinter = new BluetoothPrinter();
            }
            int numDevices = bluetoothPrinter.getBluetoothPrinterCount();
            if(numDevices<=0){
                Toast.makeText(MainActivity.this, getString(R.string.bluetoothpair), Toast.LENGTH_SHORT).show();
                gotoBluetoothmenu();
                return;
            }
            Log.d(TAG, "Printing the report...");
            String formattedText = parseImage(reportBitmap, bluetoothPrinter.getPrinterInstance());
                   // "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(bluetoothPrinter.getPrinterInstance(), reportBitmap)+"</img>\n";


            if(bluetoothPrinter.printOnDevice(formattedText)) {
                Toast.makeText(MainActivity.this, getString(R.string.printed), Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(MainActivity.this, getString(R.string.printError), Toast.LENGTH_SHORT).show();
            }

        }
    }

    public void gotoBluetoothmenu(){
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        ComponentName cn = new ComponentName("com.android.settings",
                "com.android.settings.bluetooth.BluetoothSettings");
        intent.setComponent(cn);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity( intent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d(TAG, "Bluetooth permission retrieved");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case MainActivity.PERMISSION_BLUETOOTH:
                    Toast.makeText(this,getString(R.string.bluetoothpermgrant), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Bluetooth permission granted");
                    break;
            }
        }
        else if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED){
            switch (requestCode) {
                case MainActivity.PERMISSION_BLUETOOTH:
                    Toast.makeText(this, getString(R.string.bluetoothpermdenied), Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Bluetooth permission not granted");
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }

    }

}