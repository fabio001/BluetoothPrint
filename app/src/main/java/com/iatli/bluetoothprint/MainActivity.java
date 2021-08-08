package com.iatli.bluetoothprint;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class MainActivity extends AppCompatActivity {
    public static final String TAG ="YENORSAN";
    public static final boolean DEBUG_ENABLE = false;
    private static final int RESULT_CODE_PRINT = 1423;

    private static final String BASE_URL = "http://rafetdurgut.com/Yenorsan/";
    public static final int PERMISSION_BLUETOOTH = 1551;
    private static final String HIDDEN_URL_NAME = "print";

    private WebView webView=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);

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
                    Log.d(TAG, "Web sayfasinin görüntüsü için onFinished fonksiyonuna gelindi: " + url);
                    Intent intent = new Intent(MainActivity.this, PrintActivity.class);
                    intent.putExtra(PrintActivity.PRINT_KEY, url);
                    startActivity(intent);
                }
            }
        });

        if(DEBUG_ENABLE) {
            webView.loadUrl("http://rafetdurgut.com/Yenorsan/print.php?id=223");
        }
        else {
            webView.loadUrl(BASE_URL);
        }
        Log.d(TAG, "App is loaded");

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