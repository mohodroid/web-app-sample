package com.mohdroid.webapplication;

import android.content.Context;
import android.content.Intent;
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
    public void showToast(String message) {
        Toast.makeText(context, "Message from js" + message, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void showJazzRadio() {
        Intent intent = new Intent(context,DisplayWebActivity.class );
       context.startActivity(intent);
    }

}