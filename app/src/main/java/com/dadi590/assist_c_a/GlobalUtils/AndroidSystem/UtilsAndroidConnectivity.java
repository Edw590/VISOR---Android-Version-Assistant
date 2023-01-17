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

package com.dadi590.assist_c_a.GlobalUtils.AndroidSystem;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.GlobalUtils.UtilsPermsAuths;
import com.dadi590.assist_c_a.GlobalUtils.UtilsReflection;
import com.dadi590.assist_c_a.GlobalUtils.UtilsShell;
import com.dadi590.assist_c_a.GlobalUtils.UtilsSysApp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * <p>Connectivity related utilities.</p>
 * <p>For example Wi-Fi or Bluetooth.</p>
 */
public final class UtilsAndroidConnectivity {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsAndroidConnectivity() {
	}

	// todo The error codes are all wrong here. All mixed up. Create checkCmdErrorCode() again, put it in UtilsAndroid,
	//  and say WHY it exists --> TO NOT MIX THE ERROR CODES WITH SDK CONSTANTS

	// todo Toggling the airplane mode freezes the app.... --> another thread

	/**
	 * <p>Toggles the Wi-Fi state.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link UtilsAndroid#NO_ERR} --> for the returning value: operation executed successfully</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param enabled true to turn on, false to turn off
	 *
	 * @return same as in {@link WifiManager#setWifiEnabled(boolean)} if the Wi-Fi state is the same as requested or is
	 * alreaedy being changed to the requested state (which means {@link WifiManager#WIFI_STATE_UNKNOWN} will not be
	 * returned), or a SH shell exit code other than {@link UtilsShell#NO_ERR}
	 */
	public static int setWifiEnabled(final boolean enabled) {
		final Context context = UtilsGeneral.getContext();
		final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

		final int wifi_state = wifiManager.getWifiState();
		if ((enabled && wifi_state == WifiManager.WIFI_STATE_ENABLED || wifi_state == WifiManager.WIFI_STATE_ENABLING) ||
				(!enabled && wifi_state == WifiManager.WIFI_STATE_DISABLED || wifi_state == WifiManager.WIFI_STATE_DISABLING)) {
			return wifi_state;
		}

		// To understand the meaning of the if statement, check the doc of the setWifiEnabled method.
		if (context.getApplicationInfo().targetSdkVersion < Build.VERSION_CODES.Q) {
			if (wifiManager.setWifiEnabled(enabled)) {
				return UtilsAndroid.NO_ERR;
			}
		}

		final String command = "svc wifi " + (enabled ? "enabled" : "disabled");
		final int error_code = UtilsShell.executeShellCmd(command, false, true).error_code;

		return UtilsShell.NO_ERR == error_code ? UtilsAndroid.NO_ERR : error_code;
	}

	/**
	 * <p>Toggles the Bluetooth state.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link UtilsAndroid#NO_ERR} --> for the returning value: if the operation completed successfully</p>
	 * <p>- {@link UtilsAndroid#PERM_DENIED} --> for the returning value: no root access available</p>
	 * <p>- {@link UtilsAndroid#GEN_ERR} --> for the returning value: if an error occurred and the operation did not
	 * succeed</p>
	 * <p>- {@link UtilsAndroid#NO_BLUETOOTH_ADAPTER} --> for the returning value: no bluetooth adapter available</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param enabled true to turn on, false to turn off
	 *
	 * @return same as in {@link BluetoothAdapter#getState()} if the Wi-Fi state is the same as requested or is already
	 * being changed to the requested state (which means {@link WifiManager#WIFI_STATE_UNKNOWN} will not be returned),
	 * or one of the constants
	 */
	public static int setBluetoothEnabled(final boolean enabled) {
		final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			return UtilsAndroid.NO_BLUETOOTH_ADAPTER;
		} else {
			final int bluetooth_state = bluetoothAdapter.getState();
			if ((enabled && bluetooth_state == BluetoothAdapter.STATE_ON || bluetooth_state == BluetoothAdapter.STATE_TURNING_ON) ||
					(!enabled && bluetooth_state == BluetoothAdapter.STATE_OFF || bluetooth_state == BluetoothAdapter.STATE_TURNING_OFF)) {
				return bluetooth_state;
			}

			if (enabled) {
				return bluetoothAdapter.enable() ? UtilsAndroid.NO_ERR : UtilsAndroid.GEN_ERR;
			} else {
				return bluetoothAdapter.disable() ? UtilsAndroid.NO_ERR : UtilsAndroid.GEN_ERR;
			}
		}
	}

	/**
	 * <p>Toggles the Airplane Mode state.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link UtilsAndroid#NO_ERR} --> for the returning value: operation executed successfully</p>
	 * <p>- {@link UtilsAndroid#PERM_DENIED} --> for the returning value: no root access available</p>
	 * <p>- {@link UtilsAndroid#ALREADY_ENABLED} --> for the returning value: if the airplane mode was already enabled</p>
	 * <p>- {@link UtilsAndroid#ALREADY_DISABLED} --> for the returning value: if the airplane mode was already disabled</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param enabled true to turn on, false to turn off
	 **
	 * @return one of the constants or a SH shell exit code
	 */
	public static int setAirplaneModeEnabled(final boolean enabled) {
		final Context context = UtilsGeneral.getContext();

		final boolean airplane_mode_active;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			airplane_mode_active = Settings.Global.getInt(context.getContentResolver(),
					Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
		} else {
			airplane_mode_active = Settings.System.getInt(context.getContentResolver(),
					Settings.System.AIRPLANE_MODE_ON, 0) != 0;
		}

		if (enabled && airplane_mode_active) {
			return UtilsAndroid.ALREADY_ENABLED;
		} else if (!enabled && !airplane_mode_active) {
			return UtilsAndroid.ALREADY_DISABLED;
		}

		boolean needs_broadcast = false;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
			// Below API 17
			needs_broadcast = Settings.System.putString(context.getContentResolver(),
					Settings.System.AIRPLANE_MODE_ON, enabled ? "1" : "0");
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
			// Between API 19 and API 28
			if (UtilsPermsAuths.checkSelfPermission(Manifest.permission.CONNECTIVITY_INTERNAL)) {
				final ConnectivityManager connectivityManager = (ConnectivityManager) context.
						getSystemService(Context.CONNECTIVITY_SERVICE);
				connectivityManager.setAirplaneMode(enabled);

				return UtilsAndroid.NO_ERR;
			} else if (UtilsPermsAuths.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS)) {
				needs_broadcast = Settings.Global.putString(context.getContentResolver(),
						Settings.Global.AIRPLANE_MODE_ON, enabled ? "1" : "0");
			}
		} else {
			// Anything else (on API 17 and 18, and above API 29)
			if (UtilsPermsAuths.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS)) {
				needs_broadcast = Settings.Global.putString(context.getContentResolver(),
						Settings.Global.AIRPLANE_MODE_ON, enabled ? "1" : "0");
				// Note: on API 29 and above, NETWORK_SETTINGS, NETWORK_SETUP_WIZARD, or NETWORK_STACK is needed to call
				// the function on ConnectivityManager --> signature-only permissions, which means, forget about it.
			}
		}
		if (needs_broadcast) {
			if (UtilsSysApp.mainFunction(context.getPackageName(), UtilsSysApp.IS_SYSTEM_APP)) {
				// WRITE_SETTINGS is for system apps (or AppOps, but doesn't matter). WRITE_SECURE_SETTINGS is for
				// privileged apps.
				// ACTION_AIRPLANE_MODE_CHANGED is only sent by system apps, so any system app, I guess.
				// Therefore, this check is only to ensure the app is not a normal app with permission granted
				// by AppOps and it's yes a system app one.
				context.sendBroadcast(new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED).putExtra("state",
						(enabled ? "true" : "false")));

				return UtilsShell.NO_ERR;
			} else {
				// Broadcast
				final String broadcast_string = "am broadcast -a " + Intent.ACTION_AIRPLANE_MODE_CHANGED +
						" --ez state " + (enabled ? "true" : "false");
				// Hopefully it's not too bad if the broadcast is not sent in this case. Because part will have
				// been done by this point (setting set), and it's a hassle to check for both operations.
				return UtilsShell.executeShellCmd(broadcast_string, false, true).error_code;
			}
		}

		return UtilsAndroid.PERM_DENIED;
	}

	/**
	 * <p>Toggles the Mobile Data connection state.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link UtilsAndroid#NO_ERR} --> for the returning value: operation executed successfully</p>
	 * <p>- {@link UtilsAndroid#ALREADY_ENABLED} --> for the returning value: if the airplane mode was Mobile Data
	 * connection enabled</p>
	 * <p>- {@link UtilsAndroid#ALREADY_DISABLED} --> for the returning value: if the airplane mode was Mobile Data
	 * connection disabled</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param enabled true to enable, false to disable
	 *
	 * @return one of the constants or a SH shell exit code
	 */
	@SuppressLint("NewApi")
	public static int setMobileDataEnabled(final boolean enabled) {
		final Context context = UtilsGeneral.getContext();
		final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		final ConnectivityManager connectivityManager = (ConnectivityManager) context.
				getSystemService(Context.CONNECTIVITY_SERVICE);

		final boolean mobile_data_active;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			mobile_data_active = telephonyManager.isDataEnabled();
		} else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.L) {
			// Deprecated as of Lollipop 5.0
			final Method method = UtilsReflection.getMethod(ConnectivityManager.class, "getMobileDataEnabled");
			assert null != method; // Will never happen.
			Boolean method_ret = null;
			try {
				method_ret = (Boolean) method.invoke(connectivityManager, enabled);
			} catch (final IllegalAccessException | InvocationTargetException ignored) {
			}
			// Won't be null
			assert null != method_ret;
			mobile_data_active = method_ret;
		} else { // L <= SDK_INT < O
			// Deprecated as of Oreo 8.0
			mobile_data_active = telephonyManager.getDataEnabled();
		}

		if (enabled && mobile_data_active) {
			return UtilsAndroid.ALREADY_ENABLED;
		} else if (!enabled && !mobile_data_active) {
			return UtilsAndroid.ALREADY_DISABLED;
		}

		if (UtilsPermsAuths.checkSelfPermission(Manifest.permission.MODIFY_PHONE_STATE)) {
			// With the magical permission we can use SDK methods.
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.L) {
				// Ignore the API warning. The function exists since Lollipop on @hide.
				telephonyManager.setDataEnabled(enabled);

				return UtilsShell.NO_ERR;
			} else {
				// Deprecated as of Lollipop 5.0.
				final Method method = UtilsReflection.getMethod(ConnectivityManager.class, "setMobileDataEnabled", boolean.class);
				assert null != method; // Will never happen
				try {
					method.invoke(connectivityManager, enabled);

					return UtilsShell.NO_ERR;
				} catch (final IllegalAccessException ignored) {
				} catch (final InvocationTargetException ignored) {
				}
			}
		}

		// Without the magical permission or in case the newest function stops being available, plan B: shell commands.
		final String command = "svc data " + (enabled ? "enable" : "disable");

		return UtilsShell.executeShellCmd(command, false, true).error_code;
	}
}
