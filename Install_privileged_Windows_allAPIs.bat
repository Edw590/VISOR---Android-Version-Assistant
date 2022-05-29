:: Privileged system app installer, ready for use with Android Studio

:: ARGUMENTS
:: 1st --> 0 for API18-; 1 for API19-20; 2 for API21+
set API_type=%1

if [%API_type%] LSS [0] (
	:: This doesn't seem to work for negative numbers... Just don't be dumb - use 0, 1, or 2 and that's it.
	echo ERROR - Invalid API type argument
	exit /B 1
) else (
	if [%API_type%] GTR [2] (
		echo ERROR - Invalid API type argument
		exit /B 1
	)
)

:: When executing this, Android Studio must have already generated the APK through Gradle-aware Make to the path below.
set apk_host=.\app\build\outputs\apk\debug\app-debug.apk

set ADB="adb.exe"
set ADB_SH=%ADB% shell su -c

set app_package=com.dadi590.assist_c_a
set dir_app_name=%app_package%
set apk_name=%dir_app_name%.apk

:: Choose either /system/app or /system/priv-app
if %API_type% == 0 (
	set path_privapp=/system/app
) else (
	set path_privapp=/system/priv-app
)

:: Choose either /system/priv-app/APK.apk or /system/priv-app/APK/APK.apk
if %API_type% == 1 (
	set apk_target_dir=%path_privapp%
) else (
	set apk_target_dir=%path_privapp%/%dir_app_name%
)
set apk_target_sys=%apk_target_dir%/%apk_name%

:: Push the file to the device's sdcard
%ADB_SH% mkdir -p /sdcard/tmp
%ADB% push %apk_host% /sdcard/tmp/%apk_name%

:: Remount /system as RW and copy APK file to the final directory
%ADB_SH% mount -o rw,remount /system
%ADB_SH% mkdir -p %apk_target_dir%
:: Use cp and rm instead of mv because of a possible error (happened on miTab Advance): "failed on (...) - Cross-device
:: link", happening because of moving across devices, it seems.
%ADB_SH% cp /sdcard/tmp/%apk_name% %apk_target_sys%

:: Give permissions to the APK and APK folder
%ADB_SH% chmod 755 %apk_target_dir%
%ADB_SH% chmod 644 %apk_target_sys%

:: Remount /system as RO
%ADB_SH% mount -o ro,remount /system

:: Cleanup
%ADB_SH% rm /sdcard/tmp/%apk_name%
:: Don't force rmdir because it could have more files than the APK added to it (system stuff by chance or something).
%ADB_SH% rmdir /sdcard/tmp

:: Stop the app
%ADB% shell am force-stop %app_package%

ECHO Press ENTER to wipe Dalvik/ART caches and reboot
PAUSE

:: Wipe Dalvik/ART caches and reboot
%ADB_SH% rm -rf /data/dalvik-cache
%ADB_SH% rm -rf /data/art-cache

:: Reboot system (normally and gracefully, not like adb reboot which kills the device instantly)
:: %ADB_SH% am broadcast -a android.intent.action.REBOOT - gets stuck here, leave it disabled
%ADB_SH% svc power reboot
