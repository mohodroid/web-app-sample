package com.mohdroid.webapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class DisplayWebActivity extends AppCompatActivity {

    private static final String TAG =  "DisplayWebActivity";
    private final String DEFAULT_URL = "https://www.jazzradio.com";
    WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_web);
         webView = findViewById(R.id.webView);
        ProgressBar progressBar = findViewById(R.id.progressbar);
        Intent intent = getIntent();
        String action = intent.getAction();
        Uri data = intent.getData();
        String url;
        if (data != null) {
            Log.d(TAG, data.toString());
            if (data.getHost().equals("popular")) url = "https://www.jazzradio.com";
            else if (data.getHost().equals("apps")) url = "https://www.jazzradio.com/apps";
            else url = DEFAULT_URL;
        } else url = DEFAULT_URL;
        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }
}