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

package com.dadi590.assist_c_a.GlobalUtils;

import android.Manifest;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Utilities related to Android system settings, like Wi-Fi state.</p>
 */
public final class UtilsAndroidSettings {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsAndroidSettings() {
	}

	// Start the values from the first value not used on the WIFI_STATE_-started constants on WiFiManager.
	public static final int NO_ERRORS = 5;
	public static final int ERROR = 6;
	public static final int NO_ROOT = 7;
	/**
	 * <p>Toggles the Wi-Fi state.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #NO_ERRORS} --> for the returning value: if the operation completed successfully</p>
	 * <p>- {@link #ERROR} --> for the returning value: if an error occurred and the operation did not succeed</p>
	 * <p>- {@link #NO_ROOT} --> for the returning value: if root user rights are not available but are required for the
	 * operation</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param enabled true to turn on, false to turn off
	 *
	 * @return same as in {@link WifiManager#setWifiEnabled(boolean)} if the Wi-Fi state is the same as requested or is
	 * being changed to the requested state (which means {@link WifiManager#WIFI_STATE_UNKNOWN} will not be returned),
	 * or one of the constants
	 */
	public static int setWifiEnabled(final boolean enabled) {
		final Context context = UtilsGeneral.getContext();
		final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

		final int wifi_state = wifiManager.getWifiState();
		if (enabled && wifi_state == WifiManager.WIFI_STATE_ENABLED || wifi_state == WifiManager.WIFI_STATE_ENABLING) {
			return wifi_state;
		} else if (!enabled && wifi_state == WifiManager.WIFI_STATE_DISABLED ||
				wifi_state == WifiManager.WIFI_STATE_DISABLING) {
			return wifi_state;
		}

		if (context.getApplicationInfo().targetSdkVersion < Build.VERSION_CODES.Q ||
				Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
			return wifiManager.setWifiEnabled(enabled) ? NO_ERRORS : ERROR;
		} else {
			final List<String> commands = new ArrayList<>(3);
			commands.add("su");
			commands.add("svc wifi " + (enabled ? "enabled" : "disabled"));
			commands.add("exit");
			final UtilsShell.CmdOutputObj cmdOutputObj = UtilsShell.executeShellCmd(commands, true);
			if (cmdOutputObj.error_code == null || cmdOutputObj.error_code == 13) {
				// Error 13 is "Permission denied" in UNIX
				return NO_ROOT;
			} else if (cmdOutputObj.error_code != 0) {
				return ERROR;
			} else {
				return NO_ERRORS;
			}
		}
	}

	public static final int ALREADY_ENABLED = 0;
	public static final int ALREADY_DISABLED = 1;
	/**
	 * <p>Toggles the Airplane Mode state.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #NO_ERRORS} --> for the returning value: if the operation completed successfully</p>
	 * <p>- {@link #ERROR} --> for the returning value: if an error occurred and the operation did not succeed</p>
	 * <p>- {@link #NO_ROOT} --> for the returning value: if root user rights are not available but are required for the
	 * operation</p>
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
		} else {
			final boolean operation_finished;
			if (UtilsPermissions.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS)) {
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
					operation_finished = Settings.System.putString(context.getContentResolver(),
							Settings.System.AIRPLANE_MODE_ON, enabled ? "1" : "0");
				} else {
					operation_finished = Settings.Global.putString(context.getContentResolver(),
							Settings.Global.AIRPLANE_MODE_ON, enabled ? "1" : "0");
				}
				if (operation_finished) {
					return NO_ERRORS;
				} // Else, try with root commands below.
			}

			final List<String> commands = new ArrayList<>(4);
			commands.add("su");
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
				commands.add("settings put system " + Settings.System.AIRPLANE_MODE_ON + " " + (enabled ? "1" : "0"));
			} else {
				commands.add("settings put global " + Settings.Global.AIRPLANE_MODE_ON + " " + (enabled ? "1" : "0"));
			}
			commands.add("am broadcast -a android.intent.action.AIRPLANE_MODE --ez state " + (enabled ? "true" : "false"));
			commands.add("exit");
			final UtilsShell.CmdOutputObj cmdOutputObj = UtilsShell.executeShellCmd(commands, true);
			if (cmdOutputObj.error_code == null || cmdOutputObj.error_code == 13) {
				// Error 13 is "Permission denied" in UNIX
				return NO_ROOT;
			} else if (cmdOutputObj.error_code != 0) {
				return ERROR;
			} else {
				return NO_ERRORS;
			}
		}
	}
}
