package com.iatli.bluetoothprint;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;


public class MainActivity extends AppCompatActivity {
    private static final String TAG ="YENORSAN";
    private static final String BASE_URL = "http://rafetdurgut.com/Yenorsan/";
    public static final int PERMISSION_BLUETOOTH = 1551;
    private static final String HIDDEN_URL_NAME = "print";

    private BluetoothPrinter bluetoothPrinter=null;

    private WebView webView=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(url.contains(HIDDEN_URL_NAME)){
                    if(bluetoothPrinter == null){
                        bluetoothPrinter = new BluetoothPrinter();
                    }
                    //get the formatted text from URL
                    Toast.makeText(MainActivity.this, getString(R.string.printing), Toast.LENGTH_SHORT).show();
                    printUrl(url);
                    return true;
                }
                view.loadUrl(url);
                return true;
            }
        });

        Log.d(TAG, "App is loaded");
        webView.loadUrl(BASE_URL);


    }

    //public void yazdir(View view) {
    private void printUrl(String urlToPrint){
        Log.d(TAG, "Printing url "+ urlToPrint);
        //request bluetooth permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Bluetooth permission is sent");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, MainActivity.PERMISSION_BLUETOOTH);
        } else {
            Log.d(TAG, "Bluetooth is OK. Now downloading text from given URL");

            Ion.with(this)
                    .load(urlToPrint)
                    .asJsonObject()
                    .setCallback((e, result) -> {
                       if(result != null){
                           boolean isSuccess = result.get("success").getAsBoolean();
                           if(isSuccess) {
                               String formattedText = result.get("print").getAsString();

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
                               bluetoothPrinter.printOnDevice(formattedText);
                               Toast.makeText(MainActivity.this,getString(R.string.printed), Toast.LENGTH_SHORT).show();



                           }
                           else{
                               String errorMessage = result.get("error").getAsString();
                               Toast.makeText(MainActivity.this, errorMessage,Toast.LENGTH_SHORT).show();
                           }
                       }
                       else{
                           Log.d(TAG, "result null geliyor.");
                           Toast.makeText(MainActivity.this, getString(R.string.warnuser), Toast.LENGTH_LONG).show();

                       }
                    });

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