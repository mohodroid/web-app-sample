  # WebView Demo
  
  This sample will demonstrate best practices around the useage of the [AndroidX WebKit API](https://developer.android.com/guide/webapps/webview), solve main scenarios
  and challenges of hosting web pages in Android Application.
  
  In [MainActivity](https://github.com/mohodroid/web-app-sample/blob/main/app/src/main/java/com/mohdroid/webapplication/MainActivity.java) load in-app content using              [WebViewAssetLoader](https://developer.android.com/reference/androidx/webkit/WebViewAssetLoader) without access or consume bandwidth.
  
  In [DisplayWebActivity](https://github.com/mohodroid/web-app-sample/blob/main/app/src/main/java/com/mohdroid/webapplication/DisplayWebActivity.java) load and fetch [web page](https://www.jazzradio.com/#popular)
  
  ## Pre-requisites
  Android SDK 29
  
  ## Getting Started
  This sample uses the Gradle build system. To build this project, use the "gradlew build" command or use "Import Project" in Android Studio.

  ## Adding a WebView to your app
  To add a WebView to your app, you can include the <WebView> element in your activity layout
  ```
    <WebView
      android:id="@+id/webview"
      android:layout_width="match_parent"
      android:layout_height="match_parent"
    />

  ```
  Then load the page with:
  ```
    myWebView.loadUrl("https://www.example.com");

  ```
  Before this works, app need access to the internet:
  ```
  <manifest ... >
    <uses-permission android:name="android.permission.INTERNET" />
    ...
  </manifest>
  
  ```
  ## Binding JavaScript code to Android code
  
  ### Call JavaScript code from Android
  Fist: enable JavaScript for your WebView
  ```
  WebView myWebView = (WebView) findViewById(R.id.webview);
  WebSettings webSettings = myWebView.getSettings();
  webSettings.setJavaScriptEnabled(true);
  
  ```
  Second: Create a class in Android app
  ```
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
  }
  
  ```
  Third: Bind this class to the JavaScript, this will create interface for js in webView and js has access to it
  ```
  webView.addJavascriptInterface(new MyWebInterface(this), "MyWebInterface");
  
  ```
  and then call from js and html:
  ```
  <button type="button" onClick="window.MyWebInterface.showToast('hello from html')" >Js Call Java</button>
  
  ```
  
  ### Call JavaScript from Android 
  
  First: write a function in JavaScript
  
  ```
   <script type="text/javascript">
       function javaCallJs(message){
           alert(message);
       }
   </script>
  
  ```
  
  Second: Call from Android
  
  ```
   public void onclick(View view) {
        webView.loadUrl("javascript:javaCallJs(" + "'Message From Java'" + ")");
    }
  
  ```
  Note: The object that is bound to your JavaScript runs in another thread and not in the thread in which it was constructed
  
  ## Handling page navigation
  Full control over links user click.
  when override this, webView automatically accumulates a history of visited web pages.
  ```
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
  }
  
  ```
  and then add an instance of this new WebViewClient for the WebView
  ```
    webView.setWebViewClient(new MyWebViewClient(this));
  ```
  navigate backward and forward through the history with goBack() and goForward()
  ```
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
  
  ```
  NOTE: Be aware of handling configuration changes that destory and creates new WebView object, means loast your state in webView
  
  ## DeepLink
  With deeplink can open app with an intent filter declared in manifest file.
  in Activity getting data and decide which page open
  NOTE: Android offer App link that is base on the domain for web
    Work in Android 6 or later
    No dialog to choose between app and browser
      
  The general syntax for testing an intent filter URI with adb is:
  ```
    adb shell am start -W -a android.intent.action.VIEW -d       
  ```
  for example to open DisplayWebActivity and load https://www.jazzradio.com/apps url
  ```
    adb shell am start -W -a android.intent.action.VIEW -d "app://apps" 
  ```
  or open https://www.jazzradio.com/#popular 
  ```
    adb shell am start -W -a android.intent.action.VIEW -d "app://popular"
  ```  
  ## Permissions 
  ### web content can requesting permission to access the specified resources.
  to notify the host application to access permission from web page should override onPermissionRequest(PermissionRequest request) in WebChromeClient. 
  when js ask to access any permission this method call in host application, if don't override means deny access.
  
  Example code to access camera form web page
  JavaScript code:
  ```
    <div id="container">
        <video id="videoElement"></video>
    </div>
    <script>
        var constraints = { video: { width: 20, height: 20 } };
        navigator.mediaDevices.getUserMedia(constraints)
        .then(function(mediaStream) {
          var video = document.querySelector('video');
          video.srcObject = mediaStream;
          video.onloadedmetadata = function(e) {
             video.play();
            };
        })
        .catch(function(err) { console.log(err.name + ": " + err.message); });
    </script>

  ``` 
  Android code:
  ```
   @Override
   public void onPermissionRequest(PermissionRequest request) {
       Log.d(TAG, "onPermissionRequest()");
       //get Permission
       Permissions permissions = new Permissions(MainActivity.this);
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
           int i = permissions.readCameraPermission();
           if (i == 1) means access permission
               request.grant(request.getResources());
           else {
               request.deny()
           }
       }
   }
  ```
  NOTE: For more information on how to handle permission in real app, Look at [Permissions](https://github.com/mohodroid/web-app-sample/blob/main/app/src/main/java/com/mohdroid/webapplication/Permissions.kt) and [MainActivity](https://github.com/mohodroid/web-app-sample/blob/main/app/src/main/java/com/mohdroid/webapplication/MainActivity.java) class.
  
  For any request to the Geolocation API should override corresponding onGeolocationPermissionsShowPrompt() method in WebChromeClient.
  ```
    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
        Log.d(TAG, "onGeolocationPermissionsShowPrompt()");
        callback.invoke(origin, true, false);
        //get Permission
        Permissions permissions = new Permissions(MainActivity.this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissions.readGeoLocationPermission();
            callback.invoke(origin, true, true);
        }
    } 
  ```
   
  
