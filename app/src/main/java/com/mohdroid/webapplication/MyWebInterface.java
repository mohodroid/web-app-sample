package com.mohdroid.webapplication;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class MyWebInterface {
    private final Context context;

    MyWebInterface(Context context) {
        this.context = context;
    }

    /**
     * Show a toast from the web page
     */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
    }
}