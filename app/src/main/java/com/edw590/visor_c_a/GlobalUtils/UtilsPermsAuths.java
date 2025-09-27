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

import android.app.Activity;
import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.edw590.visor_c_a.AccessibilityService.AccessibilityService;
import com.edw590.visor_c_a.BroadcastRecvs.DeviceAdmin.DeviceAdminRecv;
import com.edw590.visor_c_a.Modules.Speech.Speech2;
import com.edw590.visor_c_a.Modules.Speech.UtilsSpeech2BC;
import com.edw590.visor_c_a.Registry.RegistryKeys;
import com.edw590.visor_c_a.Registry.UtilsRegistry;

import java.util.ArrayList;

import GPTComm.GPTComm;

/**
 * <p>Utilities related to checking and requesting permissions and authorizations.</p>
 */
public final class UtilsPermsAuths {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsPermsAuths() {
	}

	/**
	 * <p>Warns the user about successfully granted permissions or not with standard sentences.</p>
	 *
	 * @param perms_left number of permissions left to authorize
	 * @param warn_success true to warn if all permissions have been successfully granted; false otherwise
	 */
	public static void warnPermissions(final int perms_left, final boolean warn_success) {
		if (perms_left == 0) {
			if (warn_success) {
				final String speak = "No permissions left to grant.";
				UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_USER_ACTION, 0, UtilsSpeech2BC.SESSION_TYPE_NONE, false, null);
			}
		} else {
			final String speak = "Warning - not all permissions have been granted to the application! Number " +
					"of permissions left to authorize: " + perms_left + ".";
			UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, 0, GPTComm.SESSION_TYPE_TEMP, false, null);
		}
	}

	/**
	 * <p>Warns the user about successfully granted authorizations or not with standard sentences.</p>
	 *
	 * @param auths_left number of authorizations left to authorize
	 * @param warn_success true to warn if all authorizations have been successfully granted, false otherwise
	 */
	public static void warnAuthorizations(final int auths_left, final boolean warn_success) {
		if (auths_left == 0) {
			if (warn_success) {
				final String speak = "No authorizations left to grant.";
				UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_USER_ACTION, 0, UtilsSpeech2BC.SESSION_TYPE_NONE, false, null);
			}
		} else {
			final String speak = "Warning - Not all authorizations have been granted to the application! Number of " +
					"authorizations left to grant: " + auths_left + ".";
			UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, 0, UtilsSpeech2BC.SESSION_TYPE_NONE, false, null);
		}
	}

	/**
	 * <p>This method checks and requests permissions located on {@link PERMS_CONSTS#list_of_perms_lists}.</p>
	 * <br>
	 * <p>That lists must be arrays of Strings, in which the first index is the permission itself, and the second is the
	 * minimum SDK version in which the permission exists.</p>
	 * <br>
	 * <p>Go to {@link PERMS_CONSTS} for examples.</p>
	 *
	 * @param activity the activity to ask the permissions from, or null to force the permissions OR in case it's only to
	 *                 check the permissions (and hence, no activity is needed for that)
	 * @param request true to request/force the permissions (depending on the {@code activity} parameter), false to only
	 *                check them
	 *
	 * @return the number of not granted permissions
	 */
	public static int checkRequestPerms(@Nullable final Activity activity, final boolean request) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			return 0;
		}

		final String [][][] list_to_use = PERMS_CONSTS.list_of_perms_lists;
		final int list_to_use_len = list_to_use.length;
		final boolean force_permissions = (request && activity == null);
		final boolean force_all_perms_auths = (boolean) UtilsRegistry.getData(RegistryKeys.K_PERMS_AUTHS_FORCE_ALL, true);
		if (!force_all_perms_auths && force_permissions) {
			return 0;
		}
		int num_not_granted_perms = 0;

		int array_length = 0;
		for (final String[][] permissions_list : list_to_use) {
			array_length += permissions_list.length;
		}
		final ArrayList<String> perms_to_request = new ArrayList<>(array_length);

		if (request) {
			final String partial_command = "pm grant " + UtilsContext.getContext().getPackageName() + " ";

			for (int permission_list = 0; permission_list < list_to_use_len; ++permission_list) {
				for (final String[] permission : list_to_use[permission_list]) {
					final String perm_name = permission[0];

					// If the permission exists on the device API level...
					if (Build.VERSION.SDK_INT >= Integer.parseInt(permission[1])) {
						// If the permission has not been granted already... (keep this here, or forcing all permissions
						// every time is too much work).
						if (!checkSelfPermission(perm_name)) {
							if (force_permissions) {
								UtilsShell.executeShellCmd(true, partial_command + perm_name);

								if (!checkSelfPermission(perm_name) && (permission_list == 0)) {
									// Don't add the permission to the number of not granted permissions if it's not
									// a runtime ("dangerous") permission. Development permissions can be granted with
									// ADB or root, but not with a confirmation UI, so the user can't be warned about
									// them because there's nothing to do unless root permissions are granted to the
									// app, and in that case, the app will have all dangerous and development
									// permissions.
									++num_not_granted_perms;
								}
							} else {
								// Add permission to a list to be requested normally below.
								perms_to_request.add(perm_name);
							}
						}
					}
				}
			}
		}

		// If it's not to force permissions, then either it's to request them normally or to just check them.
		if (!force_permissions) {
			if (request && !perms_to_request.isEmpty()) {
				// Keep 0 in the String initialization, or there will be null values --> can't be.
				ActivityCompat.requestPermissions(activity, perms_to_request.toArray(new String[0]), 0);
			}

			for (int permission_list = 0; permission_list < list_to_use_len; ++permission_list) {
				for (final String[] permission : list_to_use[permission_list]) {
					final String perm_name = permission[0];

					// If the permission exists on the device API level...
					if (Build.VERSION.SDK_INT >= Integer.parseInt(permission[1])) {
						if (!checkSelfPermission(perm_name) && (permission_list == 0)) {
							// Same reason as for the other ++num_not_granted_perms line.
							++num_not_granted_perms;
						}
					}
				}
			}
		}

		return num_not_granted_perms;
	}

	public static final int CHECK_ONLY = 0;
	public static final int ALSO_REQUEST = 1;
	public static final int ALSO_FORCE = 2;
	/**
	 * <p>Checks and requests missing authorizations, like draw system overlays, or Device Administration, or access Do
	 * Not Disturb state, on devices that need the app to request such authorizations.</p>
	 * <p>Note: this function does not wait for the user to accept or deny any authorizations - the function's return is
	 * outdated after the call, in case the user granted any permission. If the authorizations are forced, the return
	 * value is up-to-date.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #CHECK_ONLY} --> {@code what_to_do}: only check the authorizations</p>
	 * <p>- {@link #ALSO_REQUEST} --> {@code what_to_do}: also request them</p>
	 * <p>- {@link #ALSO_FORCE} --> {@code what_to_do}: attempt to force instead of requesting them</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param what_to_do true to also request, false to only check
	 *
	 * @return the number of not granted authorizations
	 */
	public static int checkRequestAuths(int what_to_do) {
		final Context context = UtilsContext.getContext();
		final String package_name = context.getPackageName();
		int missing_authorizations = 0;

		final boolean force_all_perms_auths = (boolean) UtilsRegistry.getData(RegistryKeys.K_PERMS_AUTHS_FORCE_ALL, true);
		if (!force_all_perms_auths && what_to_do == ALSO_FORCE) {
			what_to_do = CHECK_ONLY;
		}

		final String assist_package = Settings.Secure.getString(context.getContentResolver(),
				Settings.Secure.ASSISTANT);
		if (assist_package != null && !assist_package.contains(package_name)) {
			if (what_to_do == ALSO_FORCE) {
				// todo Missing forcing the authorization here
			} else {
				++missing_authorizations;

				if (what_to_do == ALSO_REQUEST) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
						final Intent intent = new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						UtilsContext.startActivity(intent);
					} else {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
							final Intent intent = new Intent(Settings.ACTION_VOICE_INPUT_SETTINGS);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							UtilsContext.startActivity(new Intent(Settings.ACTION_VOICE_INPUT_SETTINGS));
						}
						UtilsContext.startActivity(new Intent(Intent.ACTION_ASSIST));
					}
					UtilsContext.startActivity(new Intent(Intent.ACTION_VOICE_ASSIST));
					UtilsContext.startActivity(new Intent(Intent.ACTION_VOICE_COMMAND));
				}
			}
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			// Check if the DND management policy access has been granted for the app and if not, open the settings
			// screen for the user to grant it.
			final NotificationManager mNotificationManager = UtilsContext.getNotificationManager();
			if (!mNotificationManager.isNotificationPolicyAccessGranted()) {
				if (what_to_do == ALSO_FORCE) {
					final String command = "cmd notification allow_dnd " + package_name;
					UtilsShell.executeShellCmd(true, command);

					missing_authorizations += mNotificationManager.isNotificationPolicyAccessGranted() ? 0 : 1;
				} else {
					++missing_authorizations;

					if (what_to_do == ALSO_REQUEST) {
						final Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						UtilsContext.startActivity(intent);
					}
				}
			}

			// Check if the app can draw system overlays and open the settings screen if not
			if (!Settings.canDrawOverlays(context)) {
				if (what_to_do == ALSO_FORCE) {
					final String command = "appops set " + package_name + " " + AppOpsManager.OP_SYSTEM_ALERT_WINDOW +
							" allow";
					UtilsShell.executeShellCmd(true, command);

					missing_authorizations += Settings.canDrawOverlays(context) ? 0 : 1;
				} else {
					++missing_authorizations;

					if (what_to_do == ALSO_REQUEST) {
						final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
								Uri.parse("package:" + package_name));
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						UtilsContext.startActivity(intent);
					}
				}
			}

			// Check if the app can bypass battery optimizations and request it if not
			final PowerManager powerManager = (PowerManager) UtilsContext.getSystemService(Context.POWER_SERVICE);
			if (powerManager != null && !powerManager.isIgnoringBatteryOptimizations(package_name)) {
				if (what_to_do == ALSO_FORCE) {
					final String command = "dumpsys deviceidle whitelist +" + package_name;
					UtilsShell.executeShellCmd(true, command);

					missing_authorizations += powerManager.isIgnoringBatteryOptimizations(package_name) ? 0 : 1;
				} else {
					++missing_authorizations;

					if (what_to_do == ALSO_REQUEST) {
						final Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
								Uri.parse("package:" + package_name));
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						UtilsContext.startActivity(intent);
					}
				}
			}

			// Check if the app has the WRITE_SETTINGS permission and request it if not
			if (!Settings.System.canWrite(context)) {
				if (what_to_do == ALSO_FORCE) {
					final String command = "appops set " + package_name + " " + AppOpsManager.OP_WRITE_SETTINGS +
							" allow";
					UtilsShell.executeShellCmd(true, command);

					missing_authorizations += Settings.System.canWrite(context) ? 0 : 1;
				} else {
					++missing_authorizations;

					if (what_to_do == ALSO_REQUEST) {
						final Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
								Uri.parse("package:" + package_name));
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						UtilsContext.startActivity(intent);
					}
				}
			}
		}

		if (!UtilsApp.isDeviceAdmin()) {
			if (what_to_do == ALSO_FORCE) {
				forceDeviceAdmin();

				missing_authorizations += UtilsApp.isDeviceAdmin() ? 0 : 1;
			} else {
				++missing_authorizations;

				if (what_to_do == ALSO_REQUEST) {
					final Intent intent = new Intent();
					intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.DeviceAdminSettings"));
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					UtilsContext.startActivity(intent);
				}
			}
		}

		if (!UtilsApp.isAccessibilityServiceEnabled(AccessibilityService.class)) {
			if (what_to_do == ALSO_FORCE) {
				forceAccessibilityService();

				missing_authorizations += UtilsApp.isAccessibilityServiceEnabled(AccessibilityService.class) ? 0 : 1;
			} else {
				++missing_authorizations;

				if (what_to_do == ALSO_REQUEST) {
					final Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					UtilsContext.startActivity(intent);
				}
			}
		}

		return missing_authorizations;
	}

	/**
	 * <p>Forces enabling the Accessibility Service authorization to the app.</p>
	 */
	public static void forceAccessibilityService() {
		String full_accessibility_service_name = new ComponentName(UtilsContext.getContext(),
				AccessibilityService.class).flattenToString();
		UtilsShell.executeShellCmd(true, "settings put secure enabled_accessibility_services " +
				full_accessibility_service_name);
	}

	/**
	 * <p>Forces enabling Device Administrator authorization to the app.</p>
	 */
	public static void forceDeviceAdmin() {
		// Disabled until the PLS is ready
		final boolean force_all_perms_auths = false;//UtilsRegistry.getData(RegistryKeys.K_PERMS_AUTHS_FORCE_ALL, true);
		if (!force_all_perms_auths) {
			return;
		}

		final String full_device_admin_recv_name = new ComponentName(UtilsContext.getContext(),
				DeviceAdminRecv.class).flattenToString();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			// dpm is only available from Lollipop onwards
			final String command = "dpm set-active-admin " + full_device_admin_recv_name;
			UtilsShell.executeShellCmd(true, command);
		} else {


			// todo Have this thing checking if the app is already in the XML or not... Shouldn't add infinite
			//  copies to it, right...?
			// Also DON'T put the check in isDeviceAdmin() - that could be used to check if *right now* the app is a
			// Device Admin - and it's not until the next reboot if it's on KitKat- and the app is forcing it.


			/*final String device_policies_file_path = "/data/system/device_policies.xml";

			// If we can mess with the file, continue, else don't do anything.
			if (UtilsFilesDirs.checkPathExists(device_policies_file_path) != UtilsShell.PERM_DENIED) {
				final String file_end_tag = "</policies>";
				final String string_to_add =
						"<admin name=\"" + full_device_admin_recv_name + "\">\n" +
								"<policies flags=\"8\" />\n" +
								"</admin>\n" +
								file_end_tag;
				final byte[] file_bytes = UtilsFilesDirs.readFileBytes(device_policies_file_path);
				String file_data;
				if (file_bytes == null) {
					file_data = "";
				} else {
					file_data = UtilsDataConv.bytesToPrintable(file_bytes, false);
				}
				// This below already checks if file_bytes is null, which is the same as file_data being an
				// empty string, which is included in checking if it contains a substring or not.
				if (!file_data.contains(file_end_tag)) {
					// The string is based on the one from Android 4.0.3, which is the same on 4.4.2, so all
					// good. It seems only to change on Lollipop, but whatever - DPM is available there.
					file_data = (
							"<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n" +
									"<policies>\n" +
									file_end_tag);
				}
				final String file_new_data = file_data.replace(file_end_tag, string_to_add);
				UtilsFilesDirs.writeSmallFile(device_policies_file_path, file_new_data.getBytes(Charset.defaultCharset()));
				if (file_bytes == null) {
					// If there was no file present, after creating a new one, set its permissions to what
					// they're supposed to be (600, checked on Android 4.4.2).
					UtilsFilesDirs.chmod(device_policies_file_path, 600, false);
				}

				// After this method, a reboot would be required. Whatever. Maybe the file is not changed until
				// after it reboots or something xD.
			}*/
		}
	}

	/**
	 * <p>Same as in {@link ContextCompat#checkSelfPermission(Context, String)}.</p>
	 * <p>It just takes less space on a code line (no need to provide a Context instance nor check an int parameter).</p>
	 *
	 * @param permission same as in {@link ContextCompat#checkSelfPermission(Context, String)}
	 *
	 * @return true if the app has the given permission, false otherwise
	 */
	public static boolean checkSelfPermission(@NonNull final String permission) {
		return ContextCompat.checkSelfPermission(UtilsContext.getContext(), permission) == PackageManager.PERMISSION_GRANTED;
	}

	/**
	 * <p>Checks if the app holds all permissions in each provided group of permissions.</p>
	 *
	 * @param permissions an array with inner arrays of the same as in {@link #checkSelfPermission(String)}
	 *
	 * @return true if the app holds all the permissions in that sub-list, false if at least one is missing
	 */
	public static boolean checkSelfPermissions(@NonNull final String[] permissions) {
		for (final String permission : permissions) {
			if (!checkSelfPermission(permission)) {
				return false;
			}
		}

		return true;
	}
}
