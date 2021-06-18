package com.iatli.bluetoothprint;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.dantsu.printerthermal_escpos_bluetooth.Printer;
import com.dantsu.printerthermal_escpos_bluetooth.bluetooth.BluetoothPrinterSocketConnection;
import com.dantsu.printerthermal_escpos_bluetooth.bluetooth.BluetoothPrinters;
import com.dantsu.printerthermal_escpos_bluetooth.textparser.PrinterTextParserImg;;
import android.util.DisplayMetrics;

public class MainActivity extends AppCompatActivity {
    private static final String TAG ="RAFY";
    private static final String BASE_URL = "http://rafetdurgut.com/Yenorsan/";
    public static final int PERMISSION_BLUETOOTH = 1551;

    private WebView webView=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = (WebView) findViewById(R.id.webview);

        webView.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        Log.d(TAG, "App is loaded");
        webView.loadUrl(BASE_URL);
    }

    public void printIt() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Bluetooth permission is sent");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, MainActivity.PERMISSION_BLUETOOTH);
        } else {
            Log.d(TAG, "Bluetooth permission is OK. Printing");
            BluetoothPrinterSocketConnection bsocket =  BluetoothPrinters.selectFirstPairedBluetoothPrinter();
            if(bsocket == null) {
                Toast.makeText(this, "Bluetooth socketi boş!", Toast.LENGTH_SHORT).show();
                return;
            }
            Printer printer = new Printer(bsocket, 203, 48f, 32);
            if(printer == null){
                Toast.makeText(this, "Printer objesi oluşturulamıyor!", Toast.LENGTH_SHORT).show();
                return;
            }
            printer.printFormattedText(
                            //"[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, this.getApplicationContext().getResources().getDrawableForDensity(R.drawable.ic_baseline_import_export_24, DisplayMetrics.DENSITY_MEDIUM)) + "</img>\n" +
                                    "[L]\n" +
                                    "[C]<u><font size='big'>RAFET RAPORLAMA</font></u>\n" +
                                    "[L]\n" +
                                    "[C]================================\n" +
                                    "[L]\n" +
                                    "[L]<b>RAPOR MALZEME 1</b>[R]123\n" +
                                    "[L]  + Kalan : 222\n" +
                                    "[L]\n" +
                                    "[L]<b>Rapor Malzeme 2</b>[R]55\n" +
                                    "[L]  + Kalan : 10\n" +
                                    "[L]\n" +
                                    "[C]--------------------------------\n" +
                                    "[R]TOPLAM:[R]178\n" +
                                    "[R]Kalan:[R]232\n" +
                                    "[L]\n" +
                                    "[C]================================\n" +
                                    "[L]\n" +
                                    "[L]<font size='tall'>Geliştirici :</font>\n" +
                                    "[L]Rafet DURGUT\n" +
                                    "[L]www.rafy.com\n" +
                                    "[L]Tel : +90555555555\n"
                    );

            Log.d(TAG, "printing function done");


        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d(TAG, "Bluetooth permission retrieved");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case MainActivity.PERMISSION_BLUETOOTH:
                    Toast.makeText(this,"Bluetooth izni verildi", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Bluetooth permission granted");
                    break;
            }
        }
        else if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED){
            switch (requestCode) {
                case MainActivity.PERMISSION_BLUETOOTH:
                    Toast.makeText(this,"Bluetooth yazici kullanmak için izin vermelisiniz!", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Bluetooth permission not granted");
                    break;
            }
        }
    }

    public void yazdir(View view) {
        Log.d(TAG, "Print clicked");
        if(getBluetoothPrinterCount()>0)
            printIt();
        else {
            Toast.makeText(this, "İlk önce bir bluetooth yazici eşleştirin.", Toast.LENGTH_SHORT).show();
            gotoBluetoothmenu();
        }
    }

    public void kontrolet(View view) {
        BluetoothPrinters b = new BluetoothPrinters();
        BluetoothPrinterSocketConnection array[] = b.getList();
        if(array==null ){
            Toast.makeText(this, "Bluetooth yazici bulunamadi." , Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Bluetooth Yazıcı Sayısı: "+ array.length , Toast.LENGTH_SHORT).show();
    }

    public int getBluetoothPrinterCount(){
        BluetoothPrinters b = new BluetoothPrinters();
        BluetoothPrinterSocketConnection array[] = b.getList();
        if(array==null )
            return 0;
        return array.length;
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



}