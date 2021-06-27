::WIN BATCH SCRIPT

:: CHANGE THESE
set app_package=com.dadi590.assist_c_a
set dir_app_name=Assist_C_A
::set MAIN_ACTIVITY=MainAct.MainAct

set ADB="adb.exe"
::ADB_SH="%ADB% shell" # this script assumes using `adb root`. for `adb su`

set path_sysapp=/system/priv-app
set apk_host=.\app\build\outputs\apk\debug\app-debug.apk
set apk_name=%dir_app_name%.apk
set apk_target_dir=%path_sysapp%/%dir_app_name%
set apk_target_sys=%apk_target_dir%/%apk_name%

:: Delete previous APK
::del %apk_host% --> Não pode ser apagado se o Android Studio já gerou um novo (ler abaixo o que comentei)

:: Compile the APK: you can adapt this for production build, flavors, etc.
::call gradlew assembleDebug --> I let Android Studio do this with Gradle-aware Make

set ADB_SH=%ADB% shell su -c

:: Install APK: using adb su
%ADB_SH% mount -o rw,remount /system
%ADB_SH% chmod 777 /system/lib/
%ADB_SH% mkdir -p /sdcard/tmp
%ADB_SH% mkdir -p %apk_target_dir%
%ADB% push %apk_host% /sdcard/tmp/%apk_name%
%ADB_SH% mv /sdcard/tmp/%apk_name% %apk_target_sys%
%ADB_SH% rmdir /sdcard/tmp

:: Give permissions
%ADB_SH% chmod 755 %apk_target_dir%
%ADB_SH% chmod 644 %apk_target_sys%

::Unmount system
%ADB_SH% mount -o remount,ro /

:: Stop the app
%ADB% shell am force-stop %app_package%

:: Re execute the app - no need, let Android Studio do it by itself (easier, no changing anything here)
::%ADB% shell am start -n \"%app_package%/.%MAIN_ACTIVITY%\" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER --> I let Android Studio launch the app
