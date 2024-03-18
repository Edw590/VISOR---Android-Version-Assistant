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

package com.edw590.visor_c_a.GlobalUtils.AndroidSystem;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.edw590.visor_c_a.GlobalUtils.UtilsContext;
import com.edw590.visor_c_a.GlobalUtils.UtilsNetwork;
import com.edw590.visor_c_a.GlobalUtils.UtilsPermsAuths;
import com.edw590.visor_c_a.GlobalUtils.UtilsReflection;
import com.edw590.visor_c_a.GlobalUtils.UtilsShell;
import com.edw590.visor_c_a.GlobalUtils.UtilsSysApp;

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

	// todo Toggling the airplane mode freezes the app.... --> another thread

	/**
	 * <p>Toggles the Wi-Fi state.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link UtilsAndroid#ALREADY_ENABLED} --> for the returning value: {@link WifiManager#WIFI_STATE_ENABLED}</p>
	 * <p>- {@link UtilsAndroid#ALREADY_ENABLING} --> for the returning value: {@link WifiManager#WIFI_STATE_ENABLING}</p>
	 * <p>- {@link UtilsAndroid#ALREADY_DISABLED} --> for the returning value: {@link WifiManager#WIFI_STATE_DISABLED}</p>
	 * <p>- {@link UtilsAndroid#ALREADY_DISABLING} --> for the returning value: {@link WifiManager#WIFI_STATE_DISABLING}</p>
	 * <p>- {@link UtilsAndroid#NOT_AVAILABLE} --> for the returning value: Wi-Fi service not available</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param enabled true to turn on, false to turn off
	 *
	 * @return one of the constants or an SH shell exit code
	 */
	public static int setWifiEnabled(final boolean enabled) {
		final WifiManager wifiManager = UtilsNetwork.getWifiManager();

		if (null == wifiManager) {
			return UtilsAndroid.NOT_AVAILABLE;
		}

		final int wifi_state = wifiManager.getWifiState();
		if ((enabled && wifi_state == WifiManager.WIFI_STATE_ENABLED || wifi_state == WifiManager.WIFI_STATE_ENABLING) ||
				(!enabled && wifi_state == WifiManager.WIFI_STATE_DISABLED || wifi_state == WifiManager.WIFI_STATE_DISABLING)) {
			return UtilsAndroid.map_STATEs_to_consts.get(wifi_state);
		}

		// To understand the meaning of the if statement, check the doc of the setWifiEnabled method.
		if (UtilsContext.getContext().getApplicationInfo().targetSdkVersion < Build.VERSION_CODES.Q) {
			return wifiManager.setWifiEnabled(enabled) ? UtilsShell.ErrCodes.NO_ERR : UtilsShell.ErrCodes.GEN_ERR;
		} else {
			final String command = "svc wifi " + (enabled ? "enabled" : "disabled");

			return UtilsShell.executeShellCmd(command, true).exit_code;
		}
	}

	/**
	 * <p>Toggles the Bluetooth state.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link UtilsAndroid#ALREADY_ENABLED} --> for the returning value: {@link BluetoothAdapter#STATE_ON}</p>
	 * <p>- {@link UtilsAndroid#ALREADY_ENABLING} --> for the returning value: {@link BluetoothAdapter#STATE_TURNING_ON}</p>
	 * <p>- {@link UtilsAndroid#ALREADY_DISABLED} --> for the returning value: {@link BluetoothAdapter#STATE_OFF}</p>
	 * <p>- {@link UtilsAndroid#ALREADY_DISABLING} --> for the returning value: {@link BluetoothAdapter#STATE_TURNING_OFF}</p>
	 * <p>- {@link UtilsAndroid#NOT_AVAILABLE} --> for the returning value: no bluetooth adapter available</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param enabled true to turn on, false to turn off
	 *
	 * @return one of the constants or an SH shell exit code
	 */
	public static int setBluetoothEnabled(final boolean enabled) {
		final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			return UtilsAndroid.NOT_AVAILABLE;
		} else {
			final int bluetooth_state = bluetoothAdapter.getState();
			if ((enabled && bluetooth_state == BluetoothAdapter.STATE_ON || bluetooth_state == BluetoothAdapter.STATE_TURNING_ON) ||
					(!enabled && bluetooth_state == BluetoothAdapter.STATE_OFF || bluetooth_state == BluetoothAdapter.STATE_TURNING_OFF)) {
				return UtilsAndroid.map_STATEs_to_consts.get(bluetooth_state);
			}

			if (enabled) {
				return bluetoothAdapter.enable() ? UtilsShell.ErrCodes.NO_ERR : UtilsShell.ErrCodes.GEN_ERR;
			} else {
				return bluetoothAdapter.disable() ? UtilsShell.ErrCodes.NO_ERR : UtilsShell.ErrCodes.GEN_ERR;
			}
		}
	}

	/**
	 * <p>Toggles the Airplane Mode state.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link UtilsAndroid#ALREADY_ENABLED} --> for the returning value: if the airplane mode was already enabled</p>
	 * <p>- {@link UtilsAndroid#ALREADY_DISABLED} --> for the returning value: if the airplane mode was already disabled</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param enabled true to turn on, false to turn off
	 **
	 * @return one of the constants or an SH shell exit code
	 */
	public static int setAirplaneModeEnabled(final boolean enabled) {
		final Context context = UtilsContext.getContext();
		final ContentResolver contentResolver = context.getContentResolver();

		final boolean airplane_mode_active;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			airplane_mode_active = Settings.Global.getInt(contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
		} else {
			airplane_mode_active = Settings.System.getInt(contentResolver, Settings.System.AIRPLANE_MODE_ON, 0) != 0;
		}

		if (enabled && airplane_mode_active) {
			return UtilsAndroid.ALREADY_ENABLED;
		} else if (!enabled && !airplane_mode_active) {
			return UtilsAndroid.ALREADY_DISABLED;
		}

		boolean needs_broadcast = false;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
			// Below API 17
			needs_broadcast = Settings.System.putString(contentResolver,
					Settings.System.AIRPLANE_MODE_ON, enabled ? "1" : "0");
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
			// Between API 19 and API 28
			if (UtilsPermsAuths.checkSelfPermission(Manifest.permission.CONNECTIVITY_INTERNAL)) {
				final ConnectivityManager connectivityManager = (ConnectivityManager) UtilsContext.
						getSystemService(Context.CONNECTIVITY_SERVICE);
				if (null != connectivityManager) {
					connectivityManager.setAirplaneMode(enabled);

					return UtilsShell.ErrCodes.NO_ERR;
				}
			}
			if (UtilsPermsAuths.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS)) {
				needs_broadcast = Settings.Global.putString(contentResolver,
						Settings.Global.AIRPLANE_MODE_ON, enabled ? "1" : "0");
			}
		} else {
			// Anything else (on API 17 and 18, and above API 29)
			if (UtilsPermsAuths.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS)) {
				needs_broadcast = Settings.Global.putString(contentResolver,
						Settings.Global.AIRPLANE_MODE_ON, enabled ? "1" : "0");
				// Note: on API 29 and above, NETWORK_SETTINGS, NETWORK_SETUP_WIZARD, or NETWORK_STACK is needed to call
				// the function on ConnectivityManager --> signature-only permissions, which means, forget about it.
			}
		}
		if (needs_broadcast) {
			if (UtilsSysApp.mainFunction(context.getPackageName(), UtilsSysApp.IS_PRIVILEGED_SYSTEM_APP)) {
				// WRITE_SETTINGS is for system apps (or AppOps, but doesn't matter). WRITE_SECURE_SETTINGS is for
				// privileged apps.
				// ACTION_AIRPLANE_MODE_CHANGED is only sent by system apps, so any system app, I guess.
				// Therefore, this check is only to ensure the app is not a normal app with permission granted
				// by AppOps and it's yes a system app one.
				// EDIT: now I'm checking only for privileged apps. WRITE_SETTINGS is the one needed here on old Android
				// versions, and there it was a system (privileged) permission. Then went to WRITE_SECURE_SETTINGS,
				// which is privileged. So just check for privileged. Still on Oreo 8.1 I can't broadcast it for some
				// reason. So try/catch.
				final Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED).putExtra("state", enabled);
				try {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 &&
							UtilsPermsAuths.checkSelfPermission(Manifest.permission.INTERACT_ACROSS_USERS)) {
						context.sendBroadcastAsUser(intent, UserHandle.ALL);
					} else {
						context.sendBroadcast(intent);
					}

					return UtilsShell.ErrCodes.NO_ERR;
				} catch (final SecurityException ignored) {
					// Means it could not send the broadcast
				}
			}

			// Broadcast
			final String broadcast_string = "am broadcast -a " + Intent.ACTION_AIRPLANE_MODE_CHANGED +
					" --ez state " + enabled;
			// Hopefully it's not too bad if the broadcast is not sent in this case. Because part will have
			// been done by this point (setting set), and it's a hassle to check for both operations.
			return UtilsShell.executeShellCmd(broadcast_string, true).exit_code;
		}

		return UtilsShell.ErrCodes.PERM_DENIED;
	}

	/**
	 * <p>Toggles the Mobile Data connection state.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link UtilsAndroid#ALREADY_ENABLED} --> for the returning value: if the mobile data was already enabled</p>
	 * <p>- {@link UtilsAndroid#ALREADY_DISABLED} --> for the returning value: if the mobile data was already disabled</p>
	 * <p>- {@link UtilsAndroid#NOT_AVAILABLE} --> for the returning value: telephony service not available</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param enabled true to enable, false to disable
	 *
	 * @return one of the constants or an SH shell exit code
	 */
	@SuppressLint("NewApi")
	public static int setMobileDataEnabled(final boolean enabled) {
		final TelephonyManager telephonyManager = (TelephonyManager) UtilsContext.getSystemService(Context.TELEPHONY_SERVICE);
		final ConnectivityManager connectivityManager = (ConnectivityManager) UtilsContext.
				getSystemService(Context.CONNECTIVITY_SERVICE);

		final boolean mobile_data_active;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			if (null == telephonyManager) {
				return UtilsAndroid.NOT_AVAILABLE;
			}

			mobile_data_active = telephonyManager.isDataEnabled();
		} else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			if (null == connectivityManager) {
				return UtilsAndroid.NOT_AVAILABLE;
			}

			// Deprecated as of Lollipop 5.0
			final Method method = UtilsReflection.getMethod(ConnectivityManager.class, "getMobileDataEnabled");
			assert null != method; // Will never happen.
			// The return won't be null either
			mobile_data_active = (boolean) UtilsReflection.invokeMethod(method, connectivityManager, enabled).ret_var;
		} else { // L <= SDK_INT < O
			if (null == telephonyManager) {
				return UtilsAndroid.NOT_AVAILABLE;
			}

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
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				// Ignore the API warning. The function exists since Lollipop on @hide.
				telephonyManager.setDataEnabled(enabled);

				return UtilsShell.ErrCodes.NO_ERR;
			} else {
				// Deprecated as of Lollipop 5.0.
				final Method method = UtilsReflection.getMethod(ConnectivityManager.class, "setMobileDataEnabled",
						boolean.class);
				assert null != method; // Will never happen
				UtilsReflection.invokeMethod(method, connectivityManager, enabled);
				// Will always work

				return UtilsShell.ErrCodes.NO_ERR;
			}
		}

		// Without the magical permission or in case the newest function stops being available, plan B: shell commands.
		final String command = "svc data " + (enabled ? "enable" : "disable");

		return UtilsShell.executeShellCmd(command, true).exit_code;
	}
}
