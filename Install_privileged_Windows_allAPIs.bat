
:WIN BATCH SCRIPT

:: 0 for API18-; 1 for API19-20; 2 for API21+
set API_type=%1
if [%1] == [] (
	echo ERROR - No API type argument
	exit /B 1
)

:: CHANGE THESE
set app_package=com.dadi590.assist_c_a
set dir_app_name=%app_package%
::set MAIN_ACTIVITY=MainAct.MainAct

set ADB="adb.exe"
::ADB_SH="%ADB% shell" # this script assumes using `adb root`. for `adb su`

if %1 == 0 (
	set path_sysapp=/system/app
) else (
	set path_sysapp=/system/priv-app
)
set apk_host=.\app\build\outputs\apk\debug\app-debug.apk
set apk_name=%dir_app_name%.apk
if %1 == 2 (
	set apk_target_dir=%path_sysapp%/%dir_app_name%
) else (
	set apk_target_dir=%path_sysapp%
)
set apk_target_sys=%apk_target_dir%/%apk_name%

:: Delete previous APK
::del %apk_host% --> Can't be deleted if Android Studio has already generated a new one (read below what I commented)

:: Compile the APK: you can adapt this for production build, flavors, etc.
::call gradlew assembleDebug --> I let Android Studio do this with Gradle-aware Make

set ADB_SH=%ADB% shell su -c

:: Install APK: using adb su
%ADB_SH% mount -o rw,remount /system
%ADB_SH% chmod 777 /system/lib/
%ADB_SH% mkdir -p /sdcard/tmp
%ADB_SH% mkdir -p %apk_target_dir%
%ADB% push %apk_host% /sdcard/tmp/%apk_name%
:: Use cp and rm instead of mv because of a possible error (happened on miTab Advance): "failed on (...) - Cross-device
:: link", happening because of moving across devices, it seems.
%ADB_SH% cp /sdcard/tmp/%apk_name% %apk_target_sys%
%ADB_SH% rm /sdcard/tmp/%apk_name%
%ADB_SH% rmdir /sdcard/tmp

:: Give permissions
%ADB_SH% chmod 755 %apk_target_dir%
%ADB_SH% chmod 644 %apk_target_sys%

::Unmount system
%ADB_SH% mount -o remount,ro /

:: Stop the app
%ADB% shell am force-stop %app_package%

:: Re execute the app - no need, let Android Studio do it by itself (easier, no changing anything here)
::%ADB% shell am start -n \"%app_package%/.%MAIN_ACTIVITY%\" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER
