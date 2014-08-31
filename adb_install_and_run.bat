@echo off

rem gradlew.bat assembleDebug

"\Program Files (x86)\Android\android-studio\sdk\platform-tools\adb.exe" install -r app\build\outputs\apk\app-debug.apk
"\Program Files (x86)\Android\android-studio\sdk\platform-tools\adb.exe" shell ^
    am start -n "no.raiom.tls/no.raiom.tls.MainActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER; ^
    logcat -c
echo "app installed, piping log to file \tmp\logcat.txt"
"\Program Files (x86)\Android\android-studio\sdk\platform-tools\adb.exe" logcat -v time >> \tmp\logcat.txt
"\Program Files (x86)\Android\android-studio\sdk\platform-tools\adb.exe" logcat -c

