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

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;

/**
 * <p>Does various checks related to app special permissions and with system apps.</p>
 * <br>
 * <p>Here an explanation about permissions, which is useful to understand the terms used in this class.</p>
 * <p>Below KitKat 4.4, all apps in /system/app were given privileged permissions. Even the Calculator app had them.
 * That could be a security breach. So they were separated between ordinary and privileged system apps and ordinary ones
 * don't have privileged permissions above KitKat 4.4.</p>
 * <p>So these utilities have that in mind. They also have in mind the following designations:</p>
 * <p>- Platform-signed app: any app that is signed with the platform/system key (so they have system signature
 * permissions), whether it is installed on the system partitions or not.</p>
 * <p>- System app: any app that is installed on the system partitions.</p>
 * <p>- Updated system app: any system app that was updated (meaning now it is also installed on /data/app).</p>
 * <p>- Privileged system app: below KitKat 4.4, any app installed on /system/app; from KitKat 4.4 onwards,
 * only the apps installed on /system/priv-app (I really mean only /system). These apps have privileged permissions.</p>
 * <p>- Ordinary system app: only as of KitKat 4.4, those without privileged permissions, even though they're still
 * system apps. Below KitKat 4.4, they're non-existent.</p>
 * <p>System partition notes: until Oreo 8.1, there was only one: /system. As of Pie (9), there is also /vendor and
 * /product.</p>
 */
public final class UtilsSysApp {

	///////////////////////////////////
	// Removed flags with doc copied from source
	/**
	 * <p>From AOSP source:</p>
	 * <p>"Value for {@link ApplicationInfo#flags}: set to {@code true} if the application
	 * is permitted to hold privileged permissions."</p>
	 * <br>
	 * <p>NOTE: Only on API 19 through API 22.</p>
	 */
	private static final int FLAG_PRIVILEGED = 1 << 30;

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsSysApp() {
	}

	/**
	 * <p>Wrapper for {@link #mainFunction(String, int)} but that checks this app itself.</p>
	 *
	 * @param types_to_check same as in {@link #mainFunction(String, int)}
	 *
	 * @return same as in {@link #mainFunction(String, int)}
	 */
	public static boolean mainFunctionSelf(final int types_to_check) {
		return mainFunction(UtilsContext.getContext().getPackageName(), types_to_check);
	}

	public static final int IS_PLATFORM_SIGNED_APP = 1;
	public static final int IS_SYSTEM_APP = 1 << 1;
	public static final int IS_UPDATED_SYSTEM_APP = 1 << 2;
	public static final int IS_ORDINARY_SYSTEM_APP = 1 << 3;
	public static final int IS_PRIVILEGED_SYSTEM_APP = 1 << 4;
	/**
	 * <p>Checks if an app is of one or more of the given types.</p>
	 * <p>For clarification about any of the terms below, check the class doc.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #IS_PLATFORM_SIGNED_APP} --> for {@code types_to_check}: check if the app is signed with the
	 * platform/system key</p>
	 * <p>- {@link #IS_SYSTEM_APP} --> for {@code types_to_check}: check if the app is installed on the system
	 * partitions</p>
	 * <p>- {@link #IS_UPDATED_SYSTEM_APP} --> for {@code types_to_check}: check if the app, installed on the system
	 * partitions, was updated</p>
	 * <p>- {@link #IS_ORDINARY_SYSTEM_APP} --> for {@code types_to_check}: check if the app is an ordinary system app</p>
	 * <p>- {@link #IS_PRIVILEGED_SYSTEM_APP} --> for {@code types_to_check}: check if the app is a privileged system
	 * app</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 *
	 * @param package_name the package to check, or null to check this app (same as manually inserting
	 *                     {@link Context#getPackageName()}
	 * @param types_to_check a bitwise OR operation of the types needed to check (the constants)
	 *
	 * @return true if all the given types are true for the given app or if no types are provided, false otherwise
	 */
	public static boolean mainFunction(@Nullable final String package_name, final int types_to_check) {
		final Context context = UtilsContext.getContext();
		final String package_name_to_use;
		if (null == package_name) {
			package_name_to_use = context.getPackageName();
		} else {
			package_name_to_use = package_name;
		}
		final ApplicationInfo applicationInfo;
		try {
			applicationInfo = context.getPackageManager().getApplicationInfo(package_name_to_use, 0);
		} catch (final PackageManager.NameNotFoundException ignored) {
			return false;
		}

		boolean ret_value = true;
		if ((types_to_check & IS_PLATFORM_SIGNED_APP) != 0) {
			ret_value = hasSystemSignaturePermissions(applicationInfo);
		}
		if ((types_to_check & IS_SYSTEM_APP) != 0) {
			ret_value = ret_value && isSystemApp(applicationInfo);
		}
		if ((types_to_check & IS_UPDATED_SYSTEM_APP) != 0) {
			ret_value = ret_value && isUpdatedSystemApp(applicationInfo);
		}
		if ((types_to_check & IS_ORDINARY_SYSTEM_APP) != 0) {
			ret_value = ret_value && isOrdinarySystemApp(applicationInfo);
		}
		if ((types_to_check & IS_PRIVILEGED_SYSTEM_APP) != 0) {
			ret_value = ret_value && hasPrivilegedPermissions(applicationInfo);
		}

		return ret_value;
	}

	/**
	 * <p>Checks if an app is installed on the system partitions and was updated.</p>
	 *
	 * @param applicationInfo an instance of {@link ApplicationInfo} for the package to be checked
	 *
	 * @return true if it is, false otherwise
	 */
	private static boolean isUpdatedSystemApp(@NonNull final ApplicationInfo applicationInfo) {
		return (applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
	}

	/**
	 * <p>Checks if an app is installed in the system partitions (ordinary app or privileged app, doesn't matter).</p>
	 *
	 * @param applicationInfo an instance of {@link ApplicationInfo} for the package to be checked
	 *
	 * @return true if it is, false otherwise
	 */
	private static boolean isSystemApp(@NonNull final ApplicationInfo applicationInfo) {
		// Below Android Pie (9), all system apps were in /system. As of Pie, they can ALSO be in /vendor and /product.
		boolean ret_value = (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			// FLAG_SYSTEM checks if it's on the system image, which means /system. So to check for /vendor and
			// /product, here are 2 special flags.
			ret_value = ret_value || (applicationInfo.privateFlags & ApplicationInfo.PRIVATE_FLAG_VENDOR) != 0;
			ret_value = ret_value || (applicationInfo.privateFlags & ApplicationInfo.PRIVATE_FLAG_PRODUCT) != 0;
		}

		return ret_value;
	}

	/**
	 * <p>Checks if an app is an ordinary system app (installed on the system partitions, but no privileged or signature
	 * permissions granted to it).</p>
	 * <p>Note: will return false for any app on KitKat 4.4W and below.</p>
	 *
	 * @param applicationInfo an instance of {@link ApplicationInfo} for the package to be checked
	 *
	 * @return true if it is, false otherwise
	 */
	private static boolean isOrdinarySystemApp(@NonNull final ApplicationInfo applicationInfo) {
		// It's an ordinary system app if it doesn't have any special permission privileges (it's not a Privileged app
		// nor is it signed with the system key).
		boolean ret_value = isSystemApp(applicationInfo) && !hasPrivilegedPermissions(applicationInfo);
		final boolean signed_system_key = hasSystemSignaturePermissions(applicationInfo);
		ret_value = ret_value && !signed_system_key;

		return ret_value;
	}

	/**
	 * <p>Checks if an app has signature permissions - checks if it's signed with the platform/system certificate by
	 * comparing it to the "android" package.</p>
	 * <br>
	 * <p>ATTENTION: if the chosen app was signed multiple times and the system is running below Android Pie, this check
	 * may return false wrongly, since it checks if ALL the signatures from the "android" package and the chosen
	 * application match. If at least one doesn't match in both, this will return false. So use with caution in case of
	 * multiple signers. With only one signer, it's all right.</p>
	 *
	 * @param applicationInfo an instance of {@link ApplicationInfo} for the package to be checked
	 *
	 * @return true if it is, false otherwise
	 */
	private static boolean hasSystemSignaturePermissions(@NonNull final ApplicationInfo applicationInfo) {
		// If on Pie or above, check with a private flag (appeared on Pie only).
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			return (applicationInfo.privateFlags & ApplicationInfo.PRIVATE_FLAG_SIGNED_WITH_PLATFORM_KEY) != 0;
		}

		// Else, check by comparing signatures of a platform-signed app and the chosen app.
		return UtilsContext.getContext().getPackageManager().checkSignatures(applicationInfo.packageName, "android")
				== PackageManager.SIGNATURE_MATCH;
	}

	/**
	 * <p>Checks if an app is a Privileged App.</p>
	 * <p>Note: will return true for any system app below KitKat 4.4.</p>
	 *
	 * @param applicationInfo an instance of {@link ApplicationInfo} for the package to be checked
	 *
	 * @return true if it is, false otherwise
	 */
	private static boolean hasPrivilegedPermissions(@NonNull final ApplicationInfo applicationInfo) {
		// Check if it's an app installed in the system partitions. If it is, check with methods that apply only to
		// apps installed on the system partitions.
		if (isSystemApp(applicationInfo)) {
			// If it's below KitKat 4.4 and it's a system app, it's a privileged one automatically.
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
				return true;
			}

			// If on Marshmallow or above, check with a private flag.
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if ((applicationInfo.privateFlags & ApplicationInfo.PRIVATE_FLAG_PRIVILEGED) != 0) {
					return true;
				}
			}

			// If between KitKat 4.4W and Lollipop 5.1, use a deleted flag.
			if ((applicationInfo.flags & FLAG_PRIVILEGED) != 0) {
				return true;
			}
		}

		// In case none returned true above, the app may still be signed with the platform/system's key, which will
		// grant it exactly all permissions there are (which includes privileged permissions - ALL permissions).
		return hasSystemSignaturePermissions(applicationInfo);
	}

	/**
	 * <p>Gets a list of folders a system app might be installed in, depending on the device's Android version.</p>
	 * <p>Note that an updated system app will report as being installed in /data/app. For these locations to be
	 * checked, the app must not have been updated. If it has, it's not possible to tell using the directory, I think.</p>
	 *
	 * @param privileged_app true if it's to return a list for privileged apps, false if it's for ordinary system apps,
	 *                       null if it's to return a list for both types
	 *
	 * @return a list of folders its APK might be in
	 */
	@NonNull
	private static String[] getSysAppPossibleFolders(@Nullable final Boolean privileged_app) {
		final Collection<String> ret_folders = new ArrayList<>(5);

		final String PRIV_APP_FOLDER = "/system/priv-app";
		final String ORD_APP_SYSTEM_FOLDER = "/system/app";
		final String ORD_APP_VENDOR_FOLDER = "/vendor/app";
		final String ORD_APP_PRODUCT_FOLDER = "/product/app";

		if (privileged_app == null) {
			ret_folders.add(PRIV_APP_FOLDER);
			ret_folders.add(ORD_APP_SYSTEM_FOLDER);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
				ret_folders.add(ORD_APP_VENDOR_FOLDER);
				ret_folders.add(ORD_APP_PRODUCT_FOLDER);
			}
		} else if (privileged_app) {
			ret_folders.add(PRIV_APP_FOLDER);
		} else {
			ret_folders.add(ORD_APP_SYSTEM_FOLDER);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
				ret_folders.add(ORD_APP_VENDOR_FOLDER);
				ret_folders.add(ORD_APP_PRODUCT_FOLDER);
			}
		}

		// Leave it in 0 size allocation. Or null values will appear, and I don't want to need to be careful about it.
		return ret_folders.toArray(new String[0]);

		/*
		Use with:

		// If it's an updated system app, its APK will be said to be in /data/app, and the one on the system partitions
		// will become unused. But if it's not updated, it's all fine and the APK path can be used to check if it's
		// a privileged app or not.
		if (!isUpdatedSystemApp(applicationInfo)) {
			for (final String folder : getAppPossibleFolders(false)) {
				if (applicationInfo.sourceDir.startsWith(folder)) {
					return true;
				}
			}
		}
		*/
	}
}
