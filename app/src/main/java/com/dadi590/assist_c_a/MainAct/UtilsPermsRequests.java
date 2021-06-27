package com.dadi590.assist_c_a.MainAct;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import com.dadi590.assist_c_a.GlobalUtils.UtilsApp;
import com.dadi590.assist_c_a.MainSrv;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;

import org.jetbrains.annotations.NonNls;

import java.util.ArrayList;

/**
 * <p>Utilities related with requesting permissions.</p>
 */
final class UtilsPermsRequests {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsPermsRequests() {
	}

	/**
	 * <p>Warns the user about successfully granted permissions or not with standard sentences.</p>
	 *
	 * @param perms_left number of permissions left to authorize
	 * @param warn_success true to warn if all permissions have been successfully granted; false otherwise
	 */
	static void warnPermissions(final int perms_left, final boolean warn_success) {
		if (perms_left == 0) {
			if (warn_success) {
				final String speak = "All permissions have been successfully granted to the application.";
				if (MainSrv.getSpeech2() != null) {
					MainSrv.getSpeech2().speak(speak, Speech2.NO_ADDITIONAL_COMMANDS, Speech2.PRIORITY_USER_ACTION, null);
				}
			}
		} else {
			@NonNls final String speak = "Warning - not all permissions have been granted to the application! Number " +
					"of permissions left to authorize: " + perms_left + ".";
			if (MainSrv.getSpeech2() != null) {
				MainSrv.getSpeech2().speak(speak, Speech2.NO_ADDITIONAL_COMMANDS, Speech2.PRIORITY_HIGH, null);
			}
		}
	}

	/**
	 * <p>This iterates a list which contains other lists, and in those lists must be arrays of Strings, in which the
	 * first index is the permission itself, and the second is the minimum SDK version in which the permission exists.</p>
	 * <br>
	 * <p>Go to {@link PERMS_CONSTS} for examples.</p>
	 *
	 * @param activity the activity to ask the permissions from or null to force the permissions
	 * @param context a context
	 * @param request true to request the permissions, false to only check them
	 *
	 * @return an array with the total number of permissions that the app requires; the number of not granted permissions
	 * of those that the app requires; the number of permissions that had an error while being forcibly granted, in case
	 * that it was chosen to do so
	 */
	private static int[] checkRequestPerms(@Nullable final Activity activity, final Context context,
										   final boolean request) {
		final int array_length = 50;

		int num_forced_error_perms = 0;
		final ArrayList<String> perms_to_request = new ArrayList<>(array_length);

		if (request) {
			for (final String[][] perms_list : PERMS_CONSTS.list_of_perms_lists) {
				for (final String[] permission : perms_list) {
					@NonNls final String perm_name = permission[0];

					// If the permission exists on the device API level...
					if (Build.VERSION.SDK_INT >= Integer.parseInt(permission[1])) {
						// If the permission has not been granted already...
						if (PermissionChecker.checkSelfPermission(context, perm_name) != PermissionChecker.PERMISSION_GRANTED) {
							if (activity == null) {
								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
									// This below needs the GRANT_RUNTIME_PERMISSIONS permission, which has
									// protection level of "signature|installer|verifier"...

									// System class - of no use, since there is one on the SDK (and both need the same
									// permission, which can't be granted)
									//final PackageManagerInternal packageManagerInternal = (PackageManagerInternal)
									//		context.getSystemService(PackageManagerInternal.class.toString());

									//packageManagerInternal.grantRuntimePermission(context.getPackageName(),
									//		perm_name, android.os.Process.myUserHandle().getIdentifier(), true);

									try {
										// SDK class
										context.getPackageManager().grantRuntimePermission(context.getPackageName(),
												perm_name, android.os.Process.myUserHandle());
									} catch (final SecurityException ignored) {
										num_forced_error_perms++;
									}
								}
							} else {
								// Normal way of requesting permissions
								perms_to_request.add(perm_name);
								System.out.println("IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII");
							}
						}
					}
				}
			}
		}

		if (request && activity != null && !perms_to_request.isEmpty()) {
			ActivityCompat.requestPermissions(activity, perms_to_request.toArray(new String[0]), 0);
		}

		int num_not_granted_perms = 0;
		int total_num_perms = 0;

		for (final String[][] perms_list : PERMS_CONSTS.list_of_perms_lists) {
			for (final String[] permission : perms_list) {
				@NonNls final String perm_name = permission[0];

				// If the permission exists on the device API level...
				if (Build.VERSION.SDK_INT >= Integer.parseInt(permission[1])) {
					if (PermissionChecker.checkSelfPermission(context, perm_name) != PermissionChecker.PERMISSION_GRANTED) {
						num_not_granted_perms++;
					}

					total_num_perms++;
				}
			}
		}

		return new int[]{total_num_perms, num_not_granted_perms, num_forced_error_perms};
	}

	/**
	 * <p>Wrapper for the function {@link #checkRequestPerms(Activity, Context, boolean)} but that only
	 * requests permissions. To only check permissions use the mentioned method.</p>
	 * <br>
	 * <p>It first asks all permissions normally depending on a parameter. In case not all were granted and the
	 * app is installed as a system app, it will try to force the permissions to be granted.</p>
	 *
	 * @param activity same as in {@link #checkRequestPerms(Activity, Context, boolean)}, or null to force the
	 *                 permissions without asking
	 * @param context a context
	 *
	 * @return the number of permissions left to authorize
	 */
	static int wrapperRequestPerms(final Activity activity, final Context context) {
		if (activity != null) {
			checkRequestPerms(activity, context, true);
		}

		final int app_install_type = UtilsApp.appInstallationType(context);
		final boolean attempt_force_perms;
		if (activity == null ||
				(app_install_type == UtilsApp.SYSTEM_WITH_UPDATES || app_install_type == UtilsApp.SYSTEM_WITHOUT_UPDATES)) {
			if (checkRequestPerms(null, context, false)[1] == 0) {
				attempt_force_perms = false;
			} else {
				attempt_force_perms = true;
			}
		} else {
			attempt_force_perms = false;
		}

		if (attempt_force_perms) {
			return checkRequestPerms(null, context, true)[1];
		}

		return checkRequestPerms(null, context, false)[1];
	}
}
