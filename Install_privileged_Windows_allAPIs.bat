:: Copyright 2023 DADi590
::
:: Licensed to the Apache Software Foundation (ASF) under one
:: or more contributor license agreements.  See the NOTICE file
:: distributed with this work for additional information
:: regarding copyright ownership.  The ASF licenses this file
:: to you under the Apache License, Version 2.0 (the
:: "License"); you may not use this file except in compliance
:: with the License.  You may obtain a copy of the License at
::
::   http://www.apache.org/licenses/LICENSE-2.0
::
:: Unless required by applicable law or agreed to in writing,
:: software distributed under the License is distributed on an
:: "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
:: KIND, either express or implied.  See the License for the
:: specific language governing permissions and limitations
:: under the License.

:: Privileged system app installer, ready for use with Android Studio

@echo off

:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: --- App-specific details ---
set app_package=com.dadi590.assist_c_a
:: Note: apk_name must not contain weird characters - it's used for the APK file name ()not including the extension).
set apk_name=VISOR
:: --- App-specific details ---

:: When executing this scripts, Android Studio must have already generated the APK through Gradle-aware Make to the path
:: below
set apk_host=.\app\build\outputs\apk\debug\app-debug.apk

set ADB=adb.exe
set ADB_SU=%ADB% shell su -c
set temp_folder_path=/data/local/tmp
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

:: Get device information
adb shell getprop ro.version.release > rloije384uo8.txt
set /p device_release_version=<rloije384uo8.txt

adb shell getprop ro.product.model > rloije384uo8.txt
set /p device_model=<rloije384uo8.txt

adb shell getprop ro.build.version.sdk > rloije384uo8.txt
set /p device_api_level=<rloije384uo8.txt

adb shell getprop ro.product.cpu.abi > rloije384uo8.txt
set /p device_cpu_abi=<rloije384uo8.txt

adb shell getprop ro.product.cpu.abi2 > rloije384uo8.txt
set /p device_cpu_abi2=<rloije384uo8.txt

del rloije384uo8.txt
::for /f "tokens=*" %%g in (adb shell getprop ro.build.version.sdk) do set API_level=%%g - doesn't work, says the
:: system cannot find the file "adb" (???)

echo ^> Device information:
echo ^> - Model: %device_model%
echo ^> - Android version: %device_release_version%
echo ^> - Android API level: %device_api_level%
echo ^> - CPU ABI: %device_cpu_abi%
echo ^> - CPU ABI2: %device_cpu_abi2%
echo:

if %device_api_level% LSS 18 (
	:: API18-
	set API_type=0
) else if %device_api_level% LEQ 20 (
	:: API19-20
	set API_type=1
) else if %device_api_level% GEQ 21 (
	:: API21+
	set API_type=2
)

if %API_type% EQU 1 (
	echo ^> ATTENTION:
	echo ^> Are you sure you want to install the app as privileged system app on this device?
	echo ^> Reason: on miTab Advance with Android 4.4.2, it corrupts the /sdcard partition (Internal Storage)
	:: Leave this below with the if statement. Else the command will run even if the main if statement was false (???)
	if %API_type% EQU 1 pause
)

echo ^> App information:
echo ^> - App package: %app_package%
echo ^> - APK name: %apk_name%
echo:

:: Choose either /system/app or /system/priv-app
if %API_type% EQU 0 (
	set privapp_folder=app
) else (
	set privapp_folder=priv-app
)
set path_privapp=/system/%privapp_folder%

:: Choose either path_privapp/APK/APK.apk, or path_privapp/APK.apk
if %API_type% EQU 2 (
	set apk_target_dir=%path_privapp%/%apk_name%
) else (
	set apk_target_dir=%path_privapp%
)
set apk_target_sys=%apk_target_dir%/%apk_name%.apk
set apk_target_push=%temp_folder_path%/%apk_name%.apk

echo ^> Target APK path: %apk_target_sys%
echo ^> Target APK push path: %apk_target_push%

echo ^> Pushing the file to the device...
:: Push the file to the device on a temporary folder inside the /data partition, accessible by ADB
%ADB_SU% mkdir -p %temp_folder_path%
%ADB% push %apk_host% %apk_target_push%

echo ^> Copying to final ^path and adjusting file(^/folder) permissions...

:: Remount /system as RW and copy APK file to the final directory
%ADB_SU% mount -o rw,remount /system
%ADB_SU% mkdir -p %apk_target_dir%
:: Use cp and rm instead of mv because of a possible error (happened on miTab Advance): "failed on (...) - Cross-device
:: link", happening because of moving across devices, it seems.
%ADB_SU% cp %apk_target_push% %apk_target_sys%

:: Give permissions to the APK and APK folder
if %API_type% EQU 2 (
	%ADB_SU% chmod 755 %apk_target_dir%
)
%ADB_SU% chmod 644 %apk_target_sys%

:: Remount /system as RO
%ADB_SU% mount -o ro,remount /system

:: Cleanup
%ADB_SU% rm %apk_target_push%
:: Don't force rmdir because it could have more files than the APK added to it (system stuff by chance or something).
%ADB_SU% rmdir %temp_folder_path%

echo ^> ^Copy completed and temporary files^/folders deleted

:: Stop the app
%ADB_SU% am force-stop %app_package%

echo ^> App force-stopped

:: Delete the Dalvik/ART cache files for the app
:: From Android 10 onwards this is not necessary at all because of useEmbeddedDex="true" on the Manifest, but I'll still
:: leave it enabled, because 1st, why not, and 2nd, I might set the attribute to false temporarily for testing or
:: something.
:: In case the device has the find command
%ADB_SU% find /data/dalvik-cache -name "*@%apk_name%.apk@*" -delete
%ADB_SU% find /data/art-cache -name "*@%apk_name%.apk@*" -delete
:: And in case it doesn't (my Android 4.4.2 tablet doesn't)
%ADB_SU% rm -rf /data/dalvik-cache/arm/*@%apk_name%.apk@*
%ADB_SU% rm -rf /data/dalvik-cache/arm64/*@%apk_name%.apk@*
%ADB_SU% rm -rf /data/dalvik-cache/x86/*@%apk_name%.apk@*
%ADB_SU% rm -rf /data/dalvik-cache/x86_64/*@%apk_name%.apk@*
%ADB_SU% rm -rf /data/art-cache/arm/*@%apk_name%.apk@*
%ADB_SU% rm -rf /data/art-cache/arm64/*@%apk_name%.apk@*
%ADB_SU% rm -rf /data/art-cache/x86/*@%apk_name%.apk@*
%ADB_SU% rm -rf /data/art-cache/x86_64/*@%apk_name%.apk@*

echo ^> App's ART^/Dalvik cache files deleted

echo ^> Press ENTER to reboot the device...
pause

:: Reboot system, normally and gracefully, not like adb reboot which kills the device instantly
%ADB_SU% svc power reboot deviceowner
%ADB_SU% am broadcast -a android.intent.action.REBOOT

echo ^> Reboot commands sent

echo ^> Script finished
