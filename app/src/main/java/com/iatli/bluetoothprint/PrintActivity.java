package com.iatli.bluetoothprint;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PrintActivity extends AppCompatActivity {
    private WebView webView;
    public static final String PRINT_KEY = "PRINTING_INFO_PASS";
    public static final String PRINT_LIST_KEY = "PRINTING_LIST_PASS";
    private ScrollView scrollView;
    private BluetoothPrinter bluetoothPrinter = null;
    public static Bitmap bmp = null;
    private Thread t;
    public static List<String> listUrls;
    private static final String BASE_URL_PRINT = "http://rafetdurgut.com/Yenorsan/print.php?id=";
    private final int WIDTH = 550;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);

        webView = findViewById(R.id.print_webview);
        scrollView = findViewById(R.id.scrollWeb);


        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);

        Intent intent = getIntent();
        if (intent != null) {
            String listStr = intent.getStringExtra(PRINT_LIST_KEY);
            String url = intent.getStringExtra(PRINT_KEY);
            listUrls = Collections.synchronizedList(new ArrayList<String>());
            if (listStr != null) {
                String[] l = listStr.split("-");
                for (String s : l) {
                    listUrls.add(BASE_URL_PRINT + s);
                }
                Log.d(MainActivity.TAG, String.format("There are %d printing items in the list.", listUrls.size()));
                startPrintingForURL();
            } else {
                if (url != null) {
                    listUrls.add(url);
                    startPrintingForURL();
                }

            }
        }

    }

    private Bitmap getBitmapImageFromWebView() {
        webView.measure(View.MeasureSpec.makeMeasureSpec(
                View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        webView.layout(0, 0, webView.getMeasuredWidth(),
                webView.getMeasuredHeight());

        int height = webView.getMeasuredHeight();
        int webViewHeight = webView.getHeight();
        int scrollHeight = scrollView.getHeight();

        Log.d(MainActivity.TAG, "measured:" + height + ", webviewH:" + webViewHeight + ", scrollH:" + scrollHeight);

        webView.setDrawingCacheEnabled(true);
        webView.buildDrawingCache();
        Bitmap bm = Bitmap.createBitmap(webView.getMeasuredWidth(),
                webView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas bigcanvas = new Canvas(bm);
        Paint paint = new Paint();

        int iHeight = bm.getHeight();
        bigcanvas.drawBitmap(bm, 0, iHeight, paint);
        webView.draw(bigcanvas);
        webView.setDrawingCacheEnabled(false);

        //resize bitmap
        Log.d(MainActivity.TAG, "Resizing the image (w*h):" + bm.getWidth() + "*" + bm.getHeight());
        float scale_ratio = WIDTH / (float) bm.getWidth();
        Bitmap resized = Bitmap.createScaledBitmap(bm, WIDTH, (int) (iHeight * scale_ratio), true);
        Log.d(MainActivity.TAG, "Resized (w*h):" + resized.getWidth() + "*" + resized.getHeight());
        return resized;
        /*if(scrollHeight >= webViewHeight) {
            return bm;
        }
        else{
            Log.d(TAG, "Parsing webview into 10 pixels to draw full height");
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            int h=0;
            while(h < webViewHeight){
                bigcanvas.drawBitmap(bm, 0, h + STEP, paint);
                webView.draw(bigcanvas);
                h += webViewHeight;
                webView.scrollTo(0, h);
            }

            return bm;
        }*/

    }

    private String parseImage(Bitmap fullImage, EscPosPrinter printer) {

        int width = fullImage.getWidth();
        int height = fullImage.getHeight();

        StringBuilder textToPrint = new StringBuilder();
        for (int y = 0; y < height; y += 256) {
            Bitmap bitmap = Bitmap.createBitmap(fullImage, 0, y, width, (y + 256 >= height) ? height - y : 256);
            textToPrint.append("[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, bitmap) + "</img>\n");
        }
        textToPrint.append("[C]\n");

        return textToPrint.toString();
    }


    private void startPrintingForURL() {
        if (listUrls.isEmpty())
            return;


        if (bluetoothPrinter == null) {
            bluetoothPrinter = new BluetoothPrinter();
        }
        //get the formatted text from URL
        Toast.makeText(PrintActivity.this, getString(R.string.printing), Toast.LENGTH_SHORT).show();
        t= new Thread(() -> {
            AtomicInteger item_count = new AtomicInteger();
            synchronized (listUrls) {
                item_count.set(listUrls.size());
            }
            while (item_count.get() > 0) {
                synchronized (listUrls) {
                    Log.d(MainActivity.TAG, "Printing Activity tries to print: " + listUrls.get(0));
                }
                Log.d(MainActivity.TAG, "UI'da site yukleniyor " + listUrls.get(0));
                runOnUiThread(() -> {
                    synchronized (listUrls) {
                        webView.loadUrl(listUrls.get(0));
                    }
                });
                Log.d(MainActivity.TAG, "UI'da site yuklendi. 3sn bekliyor ");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Log.d(MainActivity.TAG, e.getMessage());
                    e.printStackTrace();
                }
                Log.d(MainActivity.TAG, "UI thread ile Goruntu alınıyor");
                runOnUiThread(() -> {
                    Bitmap reportBitmap = getBitmapImageFromWebView();
                    Log.d(MainActivity.TAG, "UI'Bitmap goruntusu olusturdu");

                    //DEBUG
                    if (MainActivity.DEBUG_ENABLE) {
                        Intent intent = new Intent(PrintActivity.this, DebugImageActivity.class);
                        Log.d(MainActivity.TAG, "DEBUG menusune girildi");
                        bmp = reportBitmap;
                        startActivity(intent);
                        return;
                    }

                    if (reportBitmap == null) {
                        Log.d(MainActivity.TAG, "Webview cannot be converted to bitmap!");
                        Toast.makeText(PrintActivity.this, "Web görüntüsü oluşturulamıyor", Toast.LENGTH_SHORT).show();

                    } else {
                        Log.d(MainActivity.TAG, "Görüntü oluşturuldu, print ediliyor");
                        String curl;
                        synchronized (listUrls) {
                            curl = listUrls.get(0);
                        }
                        printUrl(curl, reportBitmap);
                    }
                    Log.d(MainActivity.TAG, "Listeden url cikariliyor...");
                    //continue if url exist
                    synchronized (listUrls) {
                        listUrls.remove(0);
                        Log.d(MainActivity.TAG, "Listeden ilk eleman silindi");
                        item_count.set(listUrls.size());
                        Log.d(MainActivity.TAG, String.format("After removal of the first element, size: %d ", item_count.get()));
                    }
                    Log.d(MainActivity.TAG, "3 sn daha bekleniyor");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        Log.d(MainActivity.TAG, e.getMessage());
                        e.printStackTrace();
                    }
                });
            }
            runOnUiThread(() -> {
                //destroy the activity. MainActivity should continue. This activity is just for printing.
                Log.d(MainActivity.TAG, "Yazdirma bitti, menuden cikiliyor");
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            });
        });
        t.start();
        Log.d(MainActivity.TAG, "Başka bir thread yazma islemine basladı");

    }

    //public void yazdir(View view) {
    private void printUrl(String urlToPrint, Bitmap reportBitmap) {
        Log.d(MainActivity.TAG, "Printing url " + urlToPrint);
        if(true)
            return;
        //request bluetooth permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            Log.d(MainActivity.TAG, "Bluetooth permission is sent");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, MainActivity.PERMISSION_BLUETOOTH);
        } else {
            Log.d(MainActivity.TAG, "Bluetooth is OK. Now downloading text from given URL" + urlToPrint);

            if (bluetoothPrinter == null) {
                bluetoothPrinter = new BluetoothPrinter();
            }
            int numDevices = bluetoothPrinter.getBluetoothPrinterCount();
            if (numDevices <= 0) {
                Toast.makeText(PrintActivity.this, getString(R.string.bluetoothpair), Toast.LENGTH_SHORT).show();
                gotoBluetoothmenu();
                return;
            }

            if (bluetoothPrinter.getPrinterInstance() == null) {
                Log.d(MainActivity.TAG, "Bluetooth printer null. Alet yok");
                Toast.makeText(PrintActivity.this, "Printer ayarlarını kontrol edin. Problem var!", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d(MainActivity.TAG, "Printing the report...");
            String formattedText = parseImage(reportBitmap, bluetoothPrinter.getPrinterInstance());
            // "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(bluetoothPrinter.getPrinterInstance(), reportBitmap)+"</img>\n";


            if (bluetoothPrinter.printOnDevice(formattedText)) {
                Toast.makeText(PrintActivity.this, getString(R.string.printed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(PrintActivity.this, getString(R.string.printError), Toast.LENGTH_SHORT).show();
            }

        }
    }

    public void gotoBluetoothmenu() {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        ComponentName cn = new ComponentName("com.android.settings",
                "com.android.settings.bluetooth.BluetoothSettings");
        intent.setComponent(cn);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d(MainActivity.TAG, "Bluetooth permission retrieved");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case MainActivity.PERMISSION_BLUETOOTH:
                    Toast.makeText(this, getString(R.string.bluetoothpermgrant), Toast.LENGTH_SHORT).show();
                    Log.d(MainActivity.TAG, "Bluetooth permission granted");
                    break;
            }
        } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            switch (requestCode) {
                case MainActivity.PERMISSION_BLUETOOTH:
                    Toast.makeText(this, getString(R.string.bluetoothpermdenied), Toast.LENGTH_LONG).show();
                    Log.d(MainActivity.TAG, "Bluetooth permission not granted");
                    break;
            }
        }
    }
}