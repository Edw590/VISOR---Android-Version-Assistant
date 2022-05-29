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
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IPowerManager;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;

import androidx.annotation.RequiresApi;

import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.GlobalUtils.UtilsPermissions;
import com.dadi590.assist_c_a.GlobalUtils.UtilsRoot;
import com.dadi590.assist_c_a.GlobalUtils.UtilsShell;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Power-related utilities.</p>
 * <p>For example reboot or change Battery Saver Mode.</p>
 */
public final class UtilsAndroidPower {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsAndroidPower() {
	}

	// Don't broadcast the shutdown and reboot. The broadcasts will freeze the app (at least on BV9500 with Android 8.1).
	// If you do, put them after the shutdown and reboot function calls. Maybe that way they don't freeze (if those
	// functions wouldn't freeze themselves, that is).

	// todo Make the shutdown and reboot functions return with shell commands... --> EXECUTE ON ANOTHER THREAD!!!!!

	/**
	 * <p>Shut down the device.</p>
	 * <p>Note: the app needs either to hold the {@link Manifest.permission#REBOOT} permission or to have root user
	 * permissions.</p>
	 * <p>Attention: in case the shutdown is issued with a shell command (mentioned permission not granted), this
	 * function will NOT return (no idea how to change that, currently).</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link UtilsAndroid#NO_ERRORS} --> for the returning value: if the operation completed successfully</p>
	 * <p>- {@link UtilsAndroid#ERROR} --> for the returning value: if an error occurred and the operation did not
	 * succeed</p>
	 * <p>- {@link UtilsAndroid#NO_ROOT} --> for the returning value: if root user rights are not available but are
	 * required for the operation</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @return one of the constants
	 */
	public static int shutDownDevice() {
		// The REBOOT permission is the one required here, not SHUTDOWN (tested and got an error saying it).
		// No idea what's the use of SHUTDOWN. But I'll keep it in the Manifest in case it's needed to broadcast the
		// ACTION_SHUTDOWN or something.
		if (UtilsPermissions.checkSelfPermission(Manifest.permission.REBOOT) &&
				Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			// [I believe the function below behaves as written on Intent.ACTION_REQUEST_SHUTDOWN.]
			// Also, shutdown() only exists from API 17 onwards. Hopefully the root way works.

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				final PowerManager powerManager = (PowerManager) UtilsGeneral.getContext().
						getSystemService(Context.POWER_SERVICE);
				try {
					powerManager.shutdown(false, PowerManager.SHUTDOWN_USER_REQUESTED, false);

					return UtilsAndroid.NO_ERRORS;
				} catch (final Exception ignored) {
				}
			} else {
				final IPowerManager iPowerManager = IPowerManager.Stub.asInterface(ServiceManager.
						getService(Context.POWER_SERVICE));

				try {
					final Method method = IPowerManager.class.getDeclaredMethod("shutdown", boolean.class,
							boolean.class);
					method.setAccessible(true);

					final Boolean ret_method = (Boolean) method.invoke(iPowerManager, false, false);
					assert ret_method != null; // Which will never be... (but the warning is gone now)

					if (ret_method) {
						return UtilsAndroid.NO_ERRORS;
					}
				} catch (final NoSuchMethodException ignored) {
				} catch (final IllegalAccessException ignored) {
				} catch (final InvocationTargetException ignored) {
				}
			}
		}

		final List<String> commands = new ArrayList<>(6);
		commands.add("su");
		// This one below is for older devices (covers more anyway, in case the others below don't work on them).
		commands.add("am start -a " + Intent.ACTION_REQUEST_SHUTDOWN + " --ez KEY_CONFIRM false --ez " +
				"USER_REQUESTED_SHUTDOWN --activity-clear-task");
		commands.add("am start -n android/com.android.internal.app.ShutdownActivity --ez KEY_CONFIRM false");
		// To know more about the command just below, check libcutils/android_reboot.cpp on the Android source.
		// Also, cmds/svc/src/com/android/commands/svc/PowerCommand.java, on run().
		// And another note: the shutdown will be done gracefully, unlike with the reboot binary.
		// Update: Don't use "setprop sys.powerctl" (the command I mention above), as that doesn't seem to shut down
		// gracefully (weird).
		// No idea how to set the 3 PowerManager.shutdown() parameters with a shell command... I knew with the above
		// command, but I can't use it, so yeah.
		commands.add("svc power shutdown");
		commands.add("am broadcast -a " + Intent.ACTION_SHUTDOWN);
		commands.add("exit");
		final UtilsShell.CmdOutputObj cmdOutputObj = UtilsShell.executeShellCmd(commands, true);

		// As said in the function doc, the above command does not return if it's successful. So, if it gets here,
		// it's because there's no root access on the device and it did return (with some error in this case).
		return UtilsAndroid.checkCmdOutputObjErrCode(cmdOutputObj.error_code);
	}

	public static final int MODE_NORMAL = 0;
	public static final int MODE_SAFE = 1;
	public static final int MODE_RECOVERY = 2;
	//public static final int MODE_FAST = 3;
	public static final int ERR_UNSUPPORTED_MODE = 0;
	/**
	 * <p>Reboot the device.</p>
	 * <p>Note: the app needs either to be granted the {@link Manifest.permission#REBOOT} permission or to have root
	 * user permissions.</p>
	 * <p>Attention: in case the reboot is issued with a shell command (mentioned permission not granted), this function
	 * will NOT return (no idea how to change that, currently).</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link UtilsAndroid#NO_ERRORS} --> for the returning value: if the operation completed successfully</p>
	 * <p>- {@link UtilsAndroid#ERROR} --> for the returning value: if an error occurred and the operation did not
	 * succeed</p>
	 * <p>- {@link UtilsAndroid#NO_ROOT} --> for the returning value: if root user rights are not available but are
	 * required for the operation</p>
	 * <p>- {@link #ERR_UNSUPPORTED_MODE} --> for the returning value: if the chosen mode is unsupported on the device.</p>
	 * <br>
	 * <p>- {@link #MODE_NORMAL} --> for {@code mode}: reboot device normally</p>
	 * <p>- {@link #MODE_SAFE} --> for {@code mode}: reboot device into Safe Mode</p>
	 * <p>- {@link #MODE_RECOVERY} --> for {@code mode}: reboot device into the Recovery</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param mode one of the constants
	 *
	 * @return one of the constants
	 */
	public static int rebootDevice(final int mode) {
		final String reason;
		if (MODE_RECOVERY == mode) {
			// "recovery" exists at minimum since API 15. This constant doesn't, but what matters is the string,
			// which is always the same, so it's fully supported, even if the constant didn't exist at the beginning.
			reason = PowerManager.REBOOT_RECOVERY;
		} else if (MODE_SAFE == mode) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				reason = PowerManager.REBOOT_SAFE_MODE;
			} else {
				return ERR_UNSUPPORTED_MODE;
			}
		} else {
			// This constant below only exists from Nougat onwards. Before that, it will be an unrecognized constant by
			// the kernel, probably, and will just restart normally, I suppose. So let it be here, to simplify things,
			// and don't check the version to put an empty string (equivalent of a normal reboot).
			reason = PowerManager.REBOOT_REQUESTED_BY_DEVICE_OWNER;
		}

		if (UtilsPermissions.checkSelfPermission(Manifest.permission.REBOOT)) {
			final IPowerManager iPowerManager = IPowerManager.Stub.asInterface(ServiceManager.
					getService(Context.POWER_SERVICE));
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				// This below is the implementation of the PowerManager.reboot() function. But I need to set 'wait' to
				// false, so here is a re-implementation.
				try {
					iPowerManager.reboot(false, reason, false);

					return UtilsAndroid.NO_ERRORS;
				} catch (final Exception ignored) {
				}
			} else {
				try {
					final Method method = IPowerManager.class.getDeclaredMethod("reboot", String.class);
					method.setAccessible(true);

					final Boolean ret_method = (Boolean) method.invoke(iPowerManager, reason);
					assert ret_method != null; // Which will never be... (but the warning is gone now)

					if (ret_method) {
						return UtilsAndroid.NO_ERRORS;
					}
				} catch (final NoSuchMethodException ignored) {
				} catch (final IllegalAccessException ignored) {
				} catch (final InvocationTargetException ignored) {
				}
			}

			// Don't use the broadcast below. If the system broadcasts it with calling the function above, they'll be 2
			// broadcasts. Not a good idea, I guess. If the system doesn't broadcast it, well, system reasons for not
			// doing it or something, and so neither should the app.
			// Though, below it won't be broadcast, as it's a shell command (right? svc power doesn't broadcast,
			// hopefully...). So I broadcast it manually.
			//UtilsGeneral.getContext().sendBroadcast(new Intent(Intent.ACTION_REBOOT));
		}

		// Note: the reboot will be done gracefully, unlike with the reboot binary.
		final String reboot_command = "svc power reboot";
		final List<String> commands = new ArrayList<>(4);
		commands.add("su");
		commands.add(reboot_command + " " + reason);
		commands.add("am broadcast -a " + Intent.ACTION_REBOOT);
		commands.add("exit");
		final UtilsShell.CmdOutputObj cmdOutputObj = UtilsShell.executeShellCmd(commands, true);

		// As with the shutdown, execution will only get here if there was some error - but it can get here.
		return UtilsAndroid.checkCmdOutputObjErrCode(cmdOutputObj.error_code);
	}

	/**
	 * <p>Toggles Android's Battery Saver mode from Android 5.0 onwards (below that it doesn't exist).</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link UtilsAndroid#NO_ERRORS} --> for the returning value: if the operation completed successfully</p>
	 * <p>- {@link UtilsAndroid#ERROR} --> for the returning value: if an error occurred and the operation did not succeed</p>
	 * <p>- {@link UtilsAndroid#NO_ROOT} --> for the returning value: if root user rights are not available but are required for the
	 * operation</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param enabled true to enable, false to disable
	 *
	 * @return one of the constants
	 */
	@RequiresApi(api = Build.VERSION_CODES.L)
	public static int setBatterySaverModeEnabled(final boolean enabled) {
		final boolean operation_finished;
		if (UtilsPermissions.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS)) {
			operation_finished = Settings.Global.putString(UtilsGeneral.getContext().getContentResolver(),
					Settings.Global.LOW_POWER_MODE, enabled ? "1" : "0");

			if (operation_finished) {
				return UtilsAndroid.NO_ERRORS;
			} // Else, try with the root commands below.
		}

		final List<String> commands = new ArrayList<>(5);
		commands.add("su");
		commands.add("settings put global " + Settings.Global.LOW_POWER_MODE + " " + (enabled ? "1" : "0"));
		// todo This broadcast below is supposed to be only for registered receivers, at least on Nougat (see on other
		//  API levels --> then get this one to do that and not for everyone as it is now, I guess.
		commands.add("am broadcast -a " + PowerManager.ACTION_POWER_SAVE_MODE_CHANGED + " --ez mode " +
				(enabled ? "true" : "false"));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			commands.add("am broadcast -a " + PowerManager.ACTION_POWER_SAVE_MODE_CHANGED_INTERNAL + " --ez mode " +
					(enabled ? "true" : "false"));
		}
		commands.add("exit");
		final UtilsShell.CmdOutputObj cmdOutputObj = UtilsShell.executeShellCmd(commands, true);

		return UtilsAndroid.checkCmdOutputObjErrCode(cmdOutputObj.error_code);
	}
}
