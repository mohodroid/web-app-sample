package com.mohdroid.webapplication;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;
import androidx.webkit.WebViewCompat;

import io.sentry.Breadcrumb;
import io.sentry.Sentry;
import io.sentry.SentryLevel;

import static com.mohdroid.webapplication.Permissions.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.mohdroid.webapplication.Permissions.PERMISSIONS_REQUEST_CAMERA;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    static final String TAG = "WebApp";
    final String DEFAULT_URL = "https://appassets.androidplatform.net/assets/index.html";
    private String mGeoLocationRequestOrigin = null;
    private GeolocationPermissions.Callback mGeoLocationCallback = null;
    private PermissionRequest mRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PackageInfo webViewPackageInfo = WebViewCompat.getCurrentWebViewPackage(this);
        Log.d(TAG, "WebView version: " + (webViewPackageInfo != null ? webViewPackageInfo.versionName : null));
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webView);
//        Button button = findViewById(R.id.btnJavaCallJs);
        final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .addPathHandler("/res/", new WebViewAssetLoader.ResourcesPathHandler(this))
                .build();
        webView.setWebViewClient(new LocalContentWebViewClient(assetLoader));
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
        settings.setUserAgentString(
                settings.getUserAgentString() + " " +
                        getString(R.string.app_name) + "/" +
                        BuildConfig.APPLICATION_ID + "/" +
                        BuildConfig.VERSION_NAME
        );
        Log.d(TAG, settings.getUserAgentString());
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

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                String message = consoleMessage.message() + " -- From line " +
                        consoleMessage.lineNumber() + " of " + consoleMessage.sourceId();
                log(consoleMessage.messageLevel(), message);
                SentryLevel sentryLevel = convertToSentry(consoleMessage.messageLevel());
                if (sentryLevel != null)
                    Sentry.captureMessage(message, sentryLevel);
                return true;
            }
        });

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

    private SentryLevel convertToSentry(ConsoleMessage.MessageLevel messageLevel) {
        SentryLevel sentryLevel;
        switch (messageLevel) {
            case DEBUG:
                sentryLevel = SentryLevel.DEBUG;
                break;
            case ERROR:
                sentryLevel = SentryLevel.ERROR;
                break;
            case WARNING:
                sentryLevel = SentryLevel.WARNING;
                break;
            default:
                sentryLevel = null;
                break;
        }
        return sentryLevel;
    }

    private void log(ConsoleMessage.MessageLevel messageLevel, String message) {
        switch (messageLevel) {
            case DEBUG:
                Log.d(TAG, message);
                break;
            case ERROR:
                Log.e(TAG, message);
                break;
            case WARNING:
                Log.w(TAG, message);
                break;
            default:
                Log.i(TAG, message);
                break;
        }
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
                webView.loadUrl("javascript:fetchLocation()");
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_LONG).show();
                mGeoLocationCallback.invoke(mGeoLocationRequestOrigin, false, true);
            }
        }
    }

    private static class LocalContentWebViewClient extends WebViewClientCompat {

        private final WebViewAssetLoader mAssetLoader;

        LocalContentWebViewClient(WebViewAssetLoader assetLoader) {
            mAssetLoader = assetLoader;
        }

        @Override
        @RequiresApi(21)
        public WebResourceResponse shouldInterceptRequest(WebView view,
                                                          WebResourceRequest request) {
            return mAssetLoader.shouldInterceptRequest(request.getUrl());
        }

        @Override
        @SuppressWarnings("deprecation") // to support API < 21
        public WebResourceResponse shouldInterceptRequest(WebView view,
                                                          String url) {
            return mAssetLoader.shouldInterceptRequest(Uri.parse(url));
        }
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