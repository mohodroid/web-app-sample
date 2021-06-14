# web-app-sample


The general syntax for testing an intent filter URI with adb is:

adb shell am start
        -W -a android.intent.action.VIEW
        -d <URI> <PACKAGE>
for example: 

adb shell am start -W -a android.intent.action.VIEW -d "app://apps"      --> open MainActivity and load https://www.jazzradio.com/apps url

adb shell am start -W -a android.intent.action.VIEW -d "app://popular"   --> open MainActivity and load https://www.jazzradio.com/#popular url
