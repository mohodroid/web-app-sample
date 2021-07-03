package com.mohdroid.webapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;


import static com.mohdroid.webapplication.Permissions.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.mohdroid.webapplication.Permissions.PERMISSIONS_REQUEST_CAMERA;

public class MainActivity extends AppCompatActivity {

    WebView webView;
    private boolean safeBrowsingIsInitialized;

     static final String TAG = "WebApp";
    final String DEFAULT_URL = "file:///android_asset/page.html";
     String mGeoLocationRequestOrigin = null;
    GeolocationPermissions.Callback mGeoLocationCallback = null;
    private PermissionRequest mRequest;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PackageInfo webViewPackageInfo = WebViewCompat.getCurrentWebViewPackage(this);
        Log.d(TAG, "WebView version: " + webViewPackageInfo.versionName);
        /*
            The renderer's priority is the same as (or "is bound to") the default priority for the app.
            The true argument decreases the renderer's priority to RENDERER_PRIORITY_WAIVED when the associated WebView object is no longer visible
            In other words, a true argument indicates that your app doesn't care whether the system keeps the renderer process alive.
            In fact, this lower priority level makes it likely that the renderer process is killed in out-of-memory situations.
         */
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webView);
//        Button button = findViewById(R.id.btnJavaCallJs);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webView.setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_BOUND, true);
        }

        /*
            Full control over links user click.
            when override this, webView automatically accumulates a history of visited web pages.
         */
        webView.setWebViewClient(new MyWebViewClient(this));
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
        webView.loadUrl(DEFAULT_URL);

        /*
            JS code is disabled by default in webView
            should enable with webSettings
         */
        WebSettings settings = webView.getSettings();
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptEnabled(true);
        settings.setGeolocationEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.setSafeBrowsingEnabled(true);
        }
        webView.setWebChromeClient(new android.webkit.WebChromeClient() {



            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                Log.d(TAG, "onGeolocationPermissionsShowPrompt()");
                callback.invoke(origin, true, false);
                //get Permission
                Permissions permissions = new Permissions(MainActivity.this);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    int i = permissions.readGeoLocationPermission();
                    if (i == 1)
                        callback.invoke(origin, true, true);
                    else {
                        mGeoLocationCallback = callback;
                        mGeoLocationRequestOrigin = origin;
                    }
                }
            }

            @Override
            public void onPermissionRequest(PermissionRequest request) {
                Log.d(TAG, "onPermissionRequest()");
                //get Permission
                Permissions permissions = new Permissions(MainActivity.this);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    int i = permissions.readCameraPermission();
                    if (i == 1)
                        request.grant(request.getResources());
                    else {
                        mRequest = request;
                    }
                }


            }
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                String message = consoleMessage.message() + " -- From line " +
                        consoleMessage.lineNumber() + " of " + consoleMessage.sourceId();
                Log.d(TAG, message);
                if (message.equals("ERROR TypeError: Cannot read property 'query' of undefined -- From line 1 of https://tourismbank.dashtclub.ir/main-es2015.aff395c7aa88521d32e8.js")) {
                    Permissions permission = new Permissions(MainActivity.this);
                    permission.readCameraPermission();
                }
                return true;

            }
        });
//        settings.setSupportMultipleWindows(true);

        /*
            This will create an interface called Android for js running in the webView
            <input type="button" value="Say hello" onClick="showAndroidToast('Hello Android!')" />
            <script type="text/javascript">
                function showAndroidToast(toast) {
                    Android.showToast(toast);
                }
            </script>
         */
        webView.addJavascriptInterface(new MyWebInterface(this), "MyWebInterface");

    }

    public void onclick(View view) {
        webView.loadUrl("javascript:javaCallJs(" + "'Message From Java'" + ")");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                mRequest.grant(mRequest.getResources());
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
                mRequest.deny();
            }
        } else if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_LONG).show();
                mGeoLocationCallback.invoke(mGeoLocationRequestOrigin, true, true);
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_LONG).show();
                mGeoLocationCallback.invoke(mGeoLocationRequestOrigin, false, true);
            }
        }
    }
}