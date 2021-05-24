
package com.mohdroid.webapplication;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.webkit.SafeBrowsingResponseCompat;
import androidx.webkit.WebViewClientCompat;
import androidx.webkit.WebViewFeature;

class MyWebViewClient extends WebViewClientCompat {

    private final Context context;

    MyWebViewClient(Context context) {
        this.context = context;
    }

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
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(chooser);
        }
        return true;
    }

    // Automatically go "back to safety" when attempting to load a website that
    // Google has identified as a known threat. An instance of WebView calls
    // this method only after Safe Browsing is initialized, so there's no
    // conditional logic needed here.
    @Override
    public void onSafeBrowsingHit(@NonNull WebView view, @NonNull WebResourceRequest request, int threatType, @NonNull SafeBrowsingResponseCompat callback) {
        // The "true" argument indicates that your app reports incidents like
        // this one to Safe Browsing.
        if (WebViewFeature.isFeatureSupported(WebViewFeature.SAFE_BROWSING_RESPONSE_BACK_TO_SAFETY)) {
            callback.backToSafety(true);
            Toast.makeText(context, "Unsafe web page blocked.", Toast.LENGTH_LONG).show();
        }
    }
}
