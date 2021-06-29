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

import io.sentry.Sentry;

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
        PackageInfo webViewPackageInfo = WebViewCompat.getCurrentWebViewPackage(this);
        Log.d(TAG, "WebView version: " + webViewPackageInfo.versionName);
//        webView = new WebView(this);
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
        webView.loadUrl(url);

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

    private void loadHtml(WebView webView) {
        // Create an unencoded HTML string
        // then convert the unencoded HTML string into bytes, encode
        // it with Base64, and load the data.
        String unencodedHtml =
                "<html><body>'%23' is the percent code for ‘#‘ </body></html>";
        String encodedHtml = Base64.encodeToString(unencodedHtml.getBytes(),
                Base64.NO_PADDING);
        webView.loadData(encodedHtml, "text/html", "base64");
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar

                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
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