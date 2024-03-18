/*
 * Copyright 2023 DADi590
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.edw590.visor_c_a.GlobalUtils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.HandlerThread;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * <p>Global app-related utilities.</p>
 */
public final class UtilsGeneral {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsGeneral() {
	}

	private static final String[] hashes_APU_lib_files = {
			// All SHA-512
			"BF3EFA298679FEAC228A3AA3EB3D1ED75F433C1B3CEA09734B41D381FAA3590B0E5506AC1F1464F73E0039F8DD1F923944758EB59173A1B8E52151E9CFB54CE3", // x86_64
			"BBEEC2B13E697BE92E96E0DDCAD4BCE9FF818CA379B697E42478F29FFFD62245A97DEE7A2D0AA6545BC095D4FED52E98EAB483C2AFDA9554E4327A9DAF494680", // x86
			"75FE7F3A6490767C013D1EBD77048ECBC3E38A21834C89C4EEE904EBD14A3EECB7547CBA93DF356154F77F50D9F2E5F54090A487D074825BB0AA55F84540129A", // armeabi-v7a
			"8BBF14C19BD72EAF5F9E8E48780F3D1778323B60BA5C95455FFB2935E1AA87533D4B8862F01E3A445EADD2EFBFC13FD9DCCA18D2001674098EA30DD948DCD374", // arm64-v8a
	};

	/**
	 * <p>Checks if the Advanced Commands Detection module is available.</p>
	 * <p>It does so by checking if the ACD library file is available on the device.</p>
	 *
	 * @return true if it is available for use (ACD library file on the device and on a correct folder), false otherwise
	 */
	public static boolean isACDAvailable() {
		if (!UtilsNativeLibs.isPrimaryNativeLibAvailable(UtilsNativeLibs.ACD_LIB_NAME)) {
			return false;
		}

		return UtilsCryptoHashing.fileMatchesHash(UtilsNativeLibs.getPrimaryNativeLibsPath() + "/" +
				UtilsNativeLibs.ACD_LIB_NAME, hashes_APU_lib_files);
	}

	/**
	 * <p>Check if an accessory with speakers (like earphones, headphones, headsets...) are connected.</p>
	 *
	 * @return true if an accessory with speakers is connected, false otherwise
	 */
	public static boolean areExtSpeakersOn() {
		final AudioManager audioManager = (AudioManager) UtilsContext.getSystemService(Context.AUDIO_SERVICE);
		if (null == audioManager) {
			return false;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			final AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);

			for (final AudioDeviceInfo device : devices) {
				if (device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADSET
						|| device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
						|| device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
						|| device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
						|| device.getType() == AudioDeviceInfo.TYPE_USB_HEADSET) {
					return true;
				}
			}
		} else {
			return audioManager.isWiredHeadsetOn() || audioManager.isBluetoothScoOn()
					|| audioManager.isBluetoothA2dpOn();
		}

		return false;
	}

	/**
	 * <p>Checks if the device is currently running on low memory.</p>
	 *
	 * @return true if running on low memory, false otherwise
	 */
	public static boolean isDeviceRunningOnLowMemory() {
		final ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		final ActivityManager activityManager = (ActivityManager) UtilsContext.getSystemService(Context.ACTIVITY_SERVICE);
		if (null == activityManager) {
			return false;
		}

		activityManager.getMemoryInfo(memoryInfo);

		return memoryInfo.lowMemory;
	}

	/**
	 * <p>Vibrate the device once for the amount of time given.</p>
	 *
	 * @param duration milliseconds to vibrate
	 *
	 * @return true if the device will vibrate, false if there's no vibrator service
	 */
	public static boolean vibrateDeviceOnce(final long duration) {
		final Vibrator vibrator = (Vibrator) UtilsContext.getSystemService(Context.VIBRATOR_SERVICE);
		if (null == vibrator) {
			return false;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.MAX_AMPLITUDE));
		} else {
			vibrator.vibrate(duration);
		}

		return true;
	}

	/**
	 * <p>Broadcast a key press (key down and key up).</p>
	 *
	 * @param key_code one of the {@link KeyEvent}#KEYCODE_-started constants
	 * @param permission the permission the receivers must hold to receive this broadcast, or null to don't require this
	 *                   restriction
	 */
	public static void broadcastKeyEvent(final int key_code, @Nullable final String permission) {
		final Context context = UtilsContext.getContext();
		final Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, key_code));
		context.sendBroadcast(intent, permission);
		final Intent intent1 = new Intent(Intent.ACTION_MEDIA_BUTTON);
		intent1.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, key_code));
		context.sendBroadcast(intent1, permission);
	}

	/**
	 * <p>Checks if an intent is recognized on the device (that includes any packages, not just system ones).</p>
	 * <p>For example the intent of sending a message is not supposed to be available on a device which cannot send
	 * SMS messages, and would be a way to check such thing.</p>
	 *
	 * @param intent the intent to check
	 * @param flags same as in {@link PackageManager#queryIntentActivities(Intent, int)}
	 *
	 * @return true if the intent is available, false otherwise
	 */
	public static boolean isIntentActionAvailable(@NonNull final Intent intent, final int flags) {
		final List<ResolveInfo> list = UtilsContext.getContext().getPackageManager().
				queryIntentActivities(intent, flags);

		return !list.isEmpty();
	}

	/**
	 * Get available system RAM memory in MiB (not absolute - doesn't count with Kernel memory).
	 *
	 * @return the available RAM memory
	 */
	public static long getAvailableRAM() {
		final ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		final ActivityManager activityManager = (ActivityManager) UtilsContext.getSystemService(Context.ACTIVITY_SERVICE);
		if (null == activityManager) {
			return Long.MAX_VALUE;
		}

		activityManager.getMemoryInfo(memoryInfo);

		return memoryInfo.availMem / 1048576L;
	}

	/**
	 * Checks if the device reports that is running on low memory.
	 *
	 * @return true if it's running on low memory, false otherwise
	 */
	public static boolean isDeviceLowOnMemory() {
		final ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		final ActivityManager activityManager = (ActivityManager) UtilsContext.getSystemService(Context.ACTIVITY_SERVICE);
		if (null == activityManager) {
			return false;
		}

		return memoryInfo.lowMemory;
	}

	/**
	 * <p>Checks if a Thread state is {@link Thread.State#RUNNABLE}.</p>
	 *
	 * @param thread the thread
	 *
	 * @return true if working, false if stopped
	 */
	public static boolean isThreadWorking(@NonNull final Thread thread) {
		final Thread.State thread_state = thread.getState();
		return !((Thread.State.NEW == thread_state) || (Thread.State.TERMINATED == thread_state));
	}

	/**
	 * <p>Interrupt and quit a {@link HandlerThread} safely if on API level 18+, or forcibly if below.</p>
	 *
	 * @param handlerThread the handler thread to quit from
	 */
	public static void quitHandlerThread(@NonNull final HandlerThread handlerThread) {
		handlerThread.interrupt();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			handlerThread.quitSafely();
		} else {
			handlerThread.quit();
		}
	}

	/**
	 * <p>Get the default app for an intent.</p>
	 *
	 * @param intent the intent to check
	 *
	 * @return the default app package name, or an empty string if none is found
	 */
	@NonNull
	public static String getDefaultAppForIntent(@NonNull final Intent intent) {
		final PackageManager packageManager = UtilsContext.getContext().getPackageManager();
		final ResolveInfo resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
		if (null == resolveInfo) {
			return "";
		}

		return resolveInfo.activityInfo.packageName;
	}
}
