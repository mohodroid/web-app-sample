package com.mohdroid.webapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.SafeBrowsingResponse;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.SafeBrowsingResponseCompat;
import androidx.webkit.WebViewClientCompat;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;

import org.jetbrains.annotations.NotNull;

public class DisplayWebActivity extends AppCompatActivity {

    private static final String TAG =  "DisplayWebActivity";
    private final String DEFAULT_URL = "https://www.jazzradio.com";
    private boolean safeBrowsingIsInitialized;
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

        safeBrowsingIsInitialized = false;
        if (WebViewFeature.isFeatureSupported(WebViewFeature.START_SAFE_BROWSING)) {
            WebViewCompat.startSafeBrowsing(this, new ValueCallback<Boolean>() {
                @Override
                public void onReceiveValue(Boolean success) {
                    //when safe browsing init correctly success= true else false
                    safeBrowsingIsInitialized = true;
                    if (!success) {
                        Log.d(TAG, "Unable to initialize Safe Browsing!");
                    }
                }
            });
        }
        /*
          Load the page with:
          its better wait until safeBrowsingIsInitialized = true before loading url
          TODO("refactor to check safe browsing before load url")
         */
        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClientCompat() {
            @Override
            public boolean shouldOverrideUrlLoading(@NonNull WebView view, WebResourceRequest request) {
                Log.d(MainActivity.TAG, request.getUrl().toString());
                if ("www.jazzradio.com".equals(request.getUrl().getHost())) {
                    // This is my website, so do not override; let my WebView load the page
                    return false;
                }
                // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
                Intent intent = new Intent(Intent.ACTION_VIEW, request.getUrl());
                String title = "open page with";
                Intent chooser = Intent.createChooser(intent, title);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(chooser);
                }
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }

            // Automatically go "back to safety" when attempting to load a website that
            // Google has identified as a known threat. An instance of WebView calls
            // this method only after Safe Browsing is initialized, so there's no
            // conditional logic needed here.
            @Override
            public void onSafeBrowsingHit(@NotNull WebView view, @NotNull WebResourceRequest request, int threatType, @NotNull SafeBrowsingResponseCompat callback) {
                // The "true" argument indicates that your app reports incidents like
                // this one to Safe Browsing.
                if (WebViewFeature.isFeatureSupported(WebViewFeature.SAFE_BROWSING_RESPONSE_BACK_TO_SAFETY)) {
                    callback.backToSafety(true);
                    Toast.makeText(view.getContext(), "Unsafe web page blocked.", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (!detail.didCrash()) {
                        // Renderer was killed because the system ran out of memory.
                        // The app can recover gracefully by creating a new WebView instance
                        // in the foreground.
                        Log.d(MainActivity.TAG, "System killed the WebView rendering process " +
                                "to reclaim memory. Recreating...");
                        if (webView != null) {
                            webView.destroy();
                            webView = null;
                        }
                        // By this point, the instance variable "webView" is guaranteed
                        // to be null, so it's safe to reinitialize it.
                        return true; // The app continues executing.
                    }
                }
                // Renderer crashed because of an internal error, such as a memory access violation.
                Log.d(MainActivity.TAG, "The WebView rendering process crashed!");
                // In this example, the app itself crashes after detecting that the
                // renderer crashed. If you choose to handle the crash more gracefully
                // and allow your app to continue executing, you should 1) destroy the
                // current WebView instance, 2) specify logic for how the app can
                // continue executing, and 3) return "true" instead.
                return false;
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