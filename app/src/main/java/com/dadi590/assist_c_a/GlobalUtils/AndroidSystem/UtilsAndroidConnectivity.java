/*
 * Copyright 2021 DADi590
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
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.GlobalUtils.UtilsPermissions;
import com.dadi590.assist_c_a.GlobalUtils.UtilsShell;
import com.dadi590.assist_c_a.GlobalUtils.UtilsSysApp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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

	/**
	 * <p>Toggles the Wi-Fi state.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link UtilsAndroid#NO_ERRORS} --> for the returning value: if the operation completed successfully</p>
	 * <p>- {@link UtilsAndroid#ERROR} --> for the returning value: if an error occurred and the operation did not succeed</p>
	 * <p>- {@link UtilsAndroid#NO_ROOT} --> for the returning value: if root user rights are not available but are required for the
	 * operation</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param enabled true to turn on, false to turn off
	 *
	 * @return same as in {@link WifiManager#setWifiEnabled(boolean)} if the Wi-Fi state is the same as requested or is
	 * alreaedy being changed to the requested state (which means {@link WifiManager#WIFI_STATE_UNKNOWN} will not be
	 * returned), or one of the constants
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
		if (context.getApplicationInfo().targetSdkVersion < Build.VERSION_CODES.Q ||
				Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {

			return wifiManager.setWifiEnabled(enabled) ? UtilsAndroid.NO_ERRORS : UtilsAndroid.ERROR;
		} else {
			final List<String> commands = new ArrayList<>(3);
			commands.add("su");
			commands.add("svc wifi " + (enabled ? "enabled" : "disabled"));
			commands.add("exit");
			final UtilsShell.CmdOutputObj cmdOutputObj = UtilsShell.executeShellCmd(commands, true);

			final int ret_var = UtilsAndroid.checkCmdOutputObjErrCode(cmdOutputObj.error_code);
			if (UtilsAndroid.NO_ERRORS == ret_var) {
				final Intent intent = new Intent(WifiManager.WIFI_STATE_CHANGED_ACTION);
				intent.putExtra(WifiManager.EXTRA_PREVIOUS_WIFI_STATE, wifi_state);
				intent.putExtra(WifiManager.EXTRA_WIFI_STATE,
						enabled ? WifiManager.WIFI_STATE_ENABLED : WifiManager.WIFI_STATE_DISABLED);
				context.sendBroadcast(intent);
			}

			return ret_var;
		}
	}

	public static final int NO_BLUETOOTH_ADAPTER = -55;
	/**
	 * <p>Toggles the Bluetooth state.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link UtilsAndroid#NO_ERRORS} --> for the returning value: if the operation completed successfully</p>
	 * <p>- {@link UtilsAndroid#ERROR} --> for the returning value: if an error occurred and the operation did not succeed</p>
	 * <p>- {@link #NO_BLUETOOTH_ADAPTER} --> for the returning value: if the device has no Bluetooth adapter</p>
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
			return NO_BLUETOOTH_ADAPTER;
		} else {
			final int bluetooth_state = bluetoothAdapter.getState();
			if ((enabled && bluetooth_state == BluetoothAdapter.STATE_ON || bluetooth_state == BluetoothAdapter.STATE_TURNING_ON) ||
					(!enabled && bluetooth_state == BluetoothAdapter.STATE_OFF || bluetooth_state == BluetoothAdapter.STATE_TURNING_OFF)) {
				return bluetooth_state;
			}

			if (enabled) {
				return bluetoothAdapter.enable() ? UtilsAndroid.NO_ERRORS : UtilsAndroid.ERROR;
			} else {
				return bluetoothAdapter.disable() ? UtilsAndroid.NO_ERRORS : UtilsAndroid.ERROR;
			}
		}
	}

	public static final int ALREADY_ENABLED = -53;
	public static final int ALREADY_DISABLED = -54;
	/**
	 * <p>Toggles the Airplane Mode state.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link UtilsAndroid#NO_ERRORS} --> for the returning value: if the operation completed successfully</p>
	 * <p>- {@link UtilsAndroid#ERROR} --> for the returning value: if an error occurred and the operation did not
	 * succeed</p>
	 * <p>- {@link UtilsAndroid#NO_ROOT} --> for the returning value: if root user rights are not available but are
	 * required for the operation</p>
	 * <p>- {@link #ALREADY_ENABLED} --> for the returning value: if the airplane mode was already enabled</p>
	 * <p>- {@link #ALREADY_DISABLED} --> for the returning value: if the airplane mode was already disabled</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param enabled true to turn on, false to turn off
	 *
	 * @return one of the constants
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
			return ALREADY_ENABLED;
		} else if (!enabled && !airplane_mode_active) {
			return ALREADY_DISABLED;
		}

		final String broadcast_string = "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state " +
				(enabled ? "true" : "false");
		boolean operation_finished = false;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
			// Below API 17
			if (UtilsPermissions.checkSelfPermission(Manifest.permission.WRITE_SETTINGS)) {
				operation_finished = Settings.System.putString(context.getContentResolver(),
						Settings.System.AIRPLANE_MODE_ON, enabled ? "1" : "0");
			}
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
			// Between API 19 and API 28
			if (UtilsPermissions.checkSelfPermission(Manifest.permission.CONNECTIVITY_INTERNAL)) {
				final ConnectivityManager connectivityManager = (ConnectivityManager) context.
						getSystemService(Context.CONNECTIVITY_SERVICE);
				connectivityManager.setAirplaneMode(enabled);

				return UtilsAndroid.NO_ERRORS;
			} else if (UtilsPermissions.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS)) {
				operation_finished = Settings.Global.putString(context.getContentResolver(),
						Settings.Global.AIRPLANE_MODE_ON, enabled ? "1" : "0");
			}
		} else {
			// Anything else (on API 17 and 18, and above API 29)
			if (UtilsPermissions.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS)) {
				operation_finished = Settings.Global.putString(context.getContentResolver(),
						Settings.Global.AIRPLANE_MODE_ON, enabled ? "1" : "0");
				// Note: on API 29 and above, NETWORK_SETTINGS, NETWORK_SETUP_WIZARD, or NETWORK_STACK is needed to call
				// the function on ConnectivityManager --> signature-only permissions, which means, forget about it.
				// Except NETWORK_SETUP_WIZARD, which is also a 'setup' permission, but no idea (yet?) how to make the
				// app be of that type.
			}
		}
		if (operation_finished) {
			if (UtilsSysApp.mainFunction(context.getPackageName(), UtilsSysApp.IS_SYSTEM_APP)) {
				// WRITE_SETTINGS is for system apps (or AppOps, but doesn't matter). WRITE_SECURE_SETTINGS is for
				// privileged apps.
				// ACTION_AIRPLANE_MODE_CHANGED is sent by the system, so any system app, I guess.
				// Therefore, this check is only to ensure the app is not a normal app with permission granted
				// by AppOps and it's yes a system app one.
				context.sendBroadcast(new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED).putExtra("state",
						(enabled ? "true" : "false")));
			} else {
				// Broadcast
				final List<String> commands = new ArrayList<>(3);
				commands.add("su");
				commands.add(broadcast_string);
				commands.add("exit");
				UtilsShell.executeShellCmd(commands, true);
				// Hopefully it's not too bad if the broadcast is not sent in this case. Because part will have
				// been done by this point (setting set), and it's a hassle to check for both operations.
			}

			return UtilsAndroid.NO_ERRORS;
		}

		// Forget the idea below. The function requires at least one of 3 permissions, which are only granted to
		// system-signed apps, or one of them to "setup" apps (no idea what that is).
		//final ConnectivityManager connectivityManager = (ConnectivityManager) context.
		//		getSystemService(Context.CONNECTIVITY_SERVICE);
		//connectivityManager.setAirplaneMode(enabled);

		final List<String> commands = new ArrayList<>(4);
		commands.add("su");
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
			commands.add("settings put system " + Settings.System.AIRPLANE_MODE_ON + " " + (enabled ? "1" : "0"));
		} else {
			commands.add("settings put global " + Settings.Global.AIRPLANE_MODE_ON + " " + (enabled ? "1" : "0"));
		}
		commands.add("exit");
		final UtilsShell.CmdOutputObj cmdOutputObj = UtilsShell.executeShellCmd(commands, true);

		return UtilsAndroid.checkCmdOutputObjErrCode(cmdOutputObj.error_code);
	}

	/**
	 * <p>Toggles the Mobile Data connection state.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link UtilsAndroid#NO_ERRORS} --> for the returning value: if the operation completed successfully</p>
	 * <p>- {@link UtilsAndroid#ERROR} --> for the returning value: if an error occurred and the operation did not
	 * succeed</p>
	 * <p>- {@link UtilsAndroid#NO_ROOT} --> for the returning value: if root user rights are not available but are
	 * required for the operation</p>
	 * <p>- {@link #ALREADY_ENABLED} --> for the returning value: if the airplane mode was Mobile Data connection
	 * enabled</p>
	 * <p>- {@link #ALREADY_DISABLED} --> for the returning value: if the airplane mode was Mobile Data connection
	 * disabled</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param enabled true to enable, false to disable
	 *
	 * @return one of the constants
	 */
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
			mobile_data_active = connectivityManager.getMobileDataEnabled();
		} else { // L <= SDK_INT < O
			// Deprecated as of Oreo 8.0
			mobile_data_active = telephonyManager.getDataEnabled();
		}

		if (enabled && mobile_data_active) {
			return ALREADY_ENABLED;
		} else if (!enabled && !mobile_data_active) {
			return ALREADY_DISABLED;
		}

		if (UtilsPermissions.checkSelfPermission(Manifest.permission.MODIFY_PHONE_STATE)) {
			// With the magical permission, we can use official methods.
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.L) {
				// Ignore the warning. The function exists since Lollipop (check the AOSP source), not since Oreo.
				telephonyManager.setDataEnabled(enabled);

				return UtilsAndroid.NO_ERRORS;
			} else {
				// Deprecated as of Lollipop 5.0 (returns "void").
				try {
					final Method setMobileDataEnabled = ConnectivityManager.class.
							getDeclaredMethod("setMobileDataEnabled", boolean.class);
					setMobileDataEnabled.setAccessible(true);
					setMobileDataEnabled.invoke(connectivityManager, enabled);

					return UtilsAndroid.NO_ERRORS;
				} catch (final NoSuchMethodException ignored) {
				} catch (final IllegalAccessException ignored) {
				} catch (final InvocationTargetException ignored) {
				}

				// Won't happen.
				return 23456;
			}
		} else {
			// Without the magical permission, plan B: shell commands.
			final List<String> commands = new ArrayList<>(3);
			commands.add("su");
			commands.add("svc data " + (enabled ? "enable" : "disable"));
			commands.add("exit");
			final UtilsShell.CmdOutputObj cmdOutputObj = UtilsShell.executeShellCmd(commands, true);

			return UtilsAndroid.checkCmdOutputObjErrCode(cmdOutputObj.error_code);
		}
	}
}
