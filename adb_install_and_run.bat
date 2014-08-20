@echo off

rem gradlew.bat assembleDebug

"\Program Files (x86)\Android\android-studio\sdk\platform-tools\adb.exe" install -r app\build\outputs\apk\app-debug.apk
"\Program Files (x86)\Android\android-studio\sdk\platform-tools\adb.exe" shell ^
    am start -n "no.raiom.tls/no.raiom.tls.MainActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER; ^
    logcat -c
"\Program Files (x86)\Android\android-studio\sdk\platform-tools\adb.exe" logcat |find "Fisken"

