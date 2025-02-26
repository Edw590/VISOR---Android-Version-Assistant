/*
 * Copyright 2021-2024 Edw590
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

import android.app.UiModeManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;

import com.edw590.visor_c_a.BroadcastRecvs.DeviceAdmin.DeviceAdminRecv;

/**
 * <p>Global app-related utilities.</p>
 */
public final class UtilsApp {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsApp() {
	}

	/**
	 * <p>Gets the app ready to be shut down. For example might save (NOT stop) some important processing as fast as
	 * possible for the app to be shut down.</p>
	 * <br>
	 * <p>This method should be useful if a shut down or reboot is detected or if the user clicks Force Stop on the
	 * Settings app (if it's available).</p>
	 * <br>
	 * <p>NOTE: this does NOT shut down the app. The only supposed way to stop the app is the system by shutting down
	 * the phone, and nothing else.</p>
	 * <br>
	 * <p>This method does not stop ongoing tasks because any system app can send the shut down broadcast or something,
	 * and that could be useful in a malicious way. Not stopping anything and only saving <em>in case</em> the app is
	 * shut down prevents that.</p>
	 * <br>
	 * <p><u><i><b>CURRENTLY THIS METHOD DOES NOTHING</b></i></u></p>
	 */
	public static void prepareShutdown() {
		// todo PUT THE APP RESETTING THE NORMAL CHARGING MODE ON SHUT DOWN AND UNINSTALLATION OR IT'S DEATH!!!!!!
		// (in case the stop charging is implemented)
	}

	/**
	 * <p>Gets the app ready to be uninstalled. For example might reset important things it changed on the system - an
	 * example could be reset the charging in case it has been disabled because the battery is at 80%.</p>
	 * <br>
	 * <p>This method should be useful if it's requested to the assistant that the app should be uninstalled, or in case
	 * the user clicks the Uninstall button on the Settings app (if it's available).</p>
	 * <br>
	 * <p>NOTE: this does NOT uninstall the app. The only supposed way to uninstall the app is by telling the assistant
	 * to do it, and nothing else.</p>
	 * <br>
	 * <p><u><i><b>CURRENTLY THIS METHOD DOES NOTHING</b></i></u></p>
	 */
	public static void prepareUninstall() {
		// todo PUT THE APP RESETTING THE NORMAL CHARGING MODE ON SHUT DOWN AND UNINSTALLATION OR IT'S DEATH!!!!!!
		// (in case the stop charging is implemented)

		// todo See if the method used to detect the uninstallation is 100% secure, as opposite to detecting a shut down
		// If it's not that much secure, reset for a period of time and then put the settings/files back, or something.
		// Try to think of anything better than that, as that seems like a security hole.

		// If it's to be uninstalled, will be shut down too, so get ready for that too, but in the end.
		prepareShutdown();
	}

	/**
	 * Deletes the app private cache folder.
	 */
	public static void deleteAppCache() {
		UtilsFilesDirs.removePath(new GPath(true, UtilsContext.getContext().getCacheDir().getAbsolutePath()), true);
	}

	/**
	 * <p>Checks if an app is installed on the device or not.</p>
	 *
	 * @param package_name The name of the package of the app to be checked
	 *
	 * @return true if the app is installed, false otherwise
	 */
	public static boolean isAppInstalled(@NonNull final String package_name) {
		final PackageManager packageManager = UtilsContext.getContext().getPackageManager();
		try {
			packageManager.getPackageInfo(package_name, 0);

			return true;
		} catch (final PackageManager.NameNotFoundException ignored) {
			return false;
		}
	}

	public static final int APP_ENABLED = 0;
	public static final int APP_DISABLED = 1;
	public static final int APP_NOT_INSTALLED = 1;
	/**
	 * <p>Checks if an installed app is enabled or not.</p>
	 * <br>
	 * <p>Note: one of the states a package may have is the DEFAULT one, which is defined on the manifest, and we can't
	 * know if that means enabled or disabled. So this function supposes all packages have the default as enabled in
	 * the manifest. Hence, the result is an OR operation between STATE_DEFAULT and STATE_ENABLED.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #APP_ENABLED} --> for the returning value: the app is enabled</p>
	 * <p>- {@link #APP_DISABLED} --> for the returning value: the app is disabled</p>
	 * <p>- {@link #APP_NOT_INSTALLED} --> for the returning value: the app is not installed</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param package_name the package name of the app to be checked
	 *
	 * @return one of the constants
	 */
	public static int appEnabledStatus(@NonNull final String package_name) {
		final PackageManager packageManager = UtilsContext.getContext().getPackageManager();
		final int app_enabled_setting;
		try {
			app_enabled_setting = packageManager.getApplicationEnabledSetting(package_name);
		} catch (final IllegalArgumentException ignored) {
			return APP_NOT_INSTALLED;
		}

		return (app_enabled_setting == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT ||
				app_enabled_setting == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) ? APP_ENABLED : APP_DISABLED;
	}

	public static final int NON_PRIVILEGED = 0;
	public static final int PRIVILEGED_WITHOUT_UPDATES = 1;
	public static final int PRIVILEGED_WITH_UPDATES = 2;
	/**
	 * <p>Checks if the app is installed as a privileged app (with or without updates) or not.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #NON_PRIVILEGED} --> for the returning value: the app is not a privileged app (it has any other
	 * type)</p>
	 * <p>- {@link #PRIVILEGED_WITHOUT_UPDATES} --> for the returning value: the app is a system app without updates</p>
	 * <p>- {@link #PRIVILEGED_WITH_UPDATES} --> for the returning value: the app is a system app with updates</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @return one of the constants
	 */
	public static int appInstallationType() {
		final String package_name = UtilsContext.getContext().getPackageName();
		if (UtilsSysApp.mainFunction(package_name, UtilsSysApp.IS_PRIVILEGED_SYSTEM_APP)) {
			return UtilsSysApp.mainFunction(package_name, UtilsSysApp.IS_UPDATED_SYSTEM_APP) ?
					PRIVILEGED_WITH_UPDATES : PRIVILEGED_WITHOUT_UPDATES;
		} else {
			return NON_PRIVILEGED;
		}
	}

	/**
	 * <p>Checks if the app is a Device Administrator.</p>
	 *
	 * @return true if it's a Device Administrator, false otherwise or if there is no Device Administration available on
	 * the device
	 */
	public static boolean isDeviceAdmin() {
		final Context context = UtilsContext.getContext();
		final DevicePolicyManager mDPM = (DevicePolicyManager) UtilsContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
		if (mDPM == null) {
			return false;
		}

		final ComponentName mAdminName = new ComponentName(context, DeviceAdminRecv.class);

		return mDPM.isAdminActive(mAdminName);
	}

	/**
	 * <p>Sends a broadcast that can only be received by components inside this application (which means, an
	 * app-internal broadcast).</p>
	 * <p>To do this, this method sets {@link Intent#setPackage(String)} to this package's name automatically.</p>
	 *
	 * @param intent the intent to use with the broadcast
	 */
	public static void sendInternalBroadcast(@NonNull final Intent intent) {
		final Context context = UtilsContext.getContext();
		intent.setPackage(context.getPackageName());
		// Don't add setComponent() here. If it's an internal broadcast receiver to a class (a registered one), it
		// will complicate things. Just make sure the broadcast action is only available for the class you want or
		// something.

		context.sendBroadcast(intent);
	}

	/**
	 * <p>Check if the device is running on a TV.</p>
	 *
	 * @return true if running on a TV, false otherwise
	 */
	public static boolean isRunningOnTV() {
		Context context = UtilsContext.getContext();

		UiModeManager uiModeManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
		if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
			return true;
		}

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LEANBACK)) {
				return true;
			}
		}

		if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEVISION)) {
			return true;
		}

		if (context.getResources().getDisplayMetrics().densityDpi == DisplayMetrics.DENSITY_TV) {
			return true;
		}

		return false;
	}

	/**
	 * <p>Check if the device is running on a watch.</p>
	 *
	 * @return true if running on a watch, false otherwise
	 */
	public static boolean isRunningOnWatch() {
		Context context = UtilsContext.getContext();

		if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WATCH)) {
			return true;
		}

		Configuration config = context.getResources().getConfiguration();
		if ((config.uiMode & Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_WATCH) {
			return true;
		}

		return false;
	}
}
