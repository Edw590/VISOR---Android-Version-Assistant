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
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.IPowerManager;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.annotation.RequiresApi;

import com.edw590.visor_c_a.GlobalUtils.UtilsContext;
import com.edw590.visor_c_a.GlobalUtils.UtilsPermsAuths;
import com.edw590.visor_c_a.GlobalUtils.UtilsReflection;
import com.edw590.visor_c_a.GlobalUtils.UtilsShell;

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

	// todo Make the shutdown and reboot functions return and with a code... --> EXECUTE ON ANOTHER THREAD!!!!!

	// todo "CRITICAL APP ERROR" on the tablet on shutdown and with privileged system app permissions
	// Didn't get to see the reason.

	/**
	 * <p>Shut down the device.</p>
	 * <p>Note: the app needs either to hold the {@link Manifest.permission#REBOOT} permission or to have root user
	 * permissions.</p>
	 * <p>Attention: in case the shutdown is issued with a shell command (mentioned permission not granted), this
	 * function will NOT return (no idea how to change that, currently).</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link UtilsAndroid#NOT_AVAILABLE} --> for the returning value: power service module not available</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @return one of the constants or an SH shell exit code
	 */
	public static int shutDownDevice() {
		// The REBOOT permission is the one required here, not SHUTDOWN (tested and got an error saying it).
		// No idea what's the use of SHUTDOWN. But I'll keep it in the Manifest in case it's needed to broadcast the
		// ACTION_SHUTDOWN or something.
		if (UtilsPermsAuths.checkSelfPermission(Manifest.permission.REBOOT) &&
				Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			// [I believe the function below behaves as written on Intent.ACTION_REQUEST_SHUTDOWN.]
			// Also, shutdown() only exists from API 17 onwards. Hopefully the root way works.

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				final PowerManager powerManager = (PowerManager) UtilsContext.getSystemService(Context.POWER_SERVICE);
				if (null == powerManager) {
					return UtilsAndroid.NOT_AVAILABLE;
				}

				try {
					powerManager.shutdown(false, PowerManager.SHUTDOWN_USER_REQUESTED, false);

					return UtilsShell.ErrCodes.NO_ERR;
				} catch (final Exception ignored) {
				}
			} else {
				final IBinder iBinder = UtilsContext.getService(Context.POWER_SERVICE);
				if (null == iBinder) {
					return UtilsAndroid.NOT_AVAILABLE;
				}

				final IPowerManager iPowerManager = IPowerManager.Stub.asInterface(iBinder);

				final Method method = UtilsReflection.getMethod(IPowerManager.class, "shutdown", boolean.class,
						boolean.class);
				assert null != method; // Will never happen.
				// The return won't be null either
				final boolean ret_method = (boolean) UtilsReflection.invokeMethod(method, iPowerManager, false, false).ret_var;

				if (ret_method) {
					return UtilsShell.ErrCodes.NO_ERR;
				}
			}
		}

		final List<String> commands = new ArrayList<>(4);
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

		// As said in the function doc, the above command does not return if it's successful. So, if it gets here,
		// it's because there's no root access on the device and it did return (with some error in this case).
		return UtilsShell.executeShellCmd(true, commands).exit_code;
	}

	/**
	 * <p>Reboot the device.</p>
	 * <p>Note: the app needs either to be granted the {@link Manifest.permission#REBOOT} permission or to have root
	 * user permissions.</p>
	 * <p><strong>ATTENTION:</strong> there is not guarantee from me that each of these work in all devices. I don't
	 * know what works in which API levels. As I don't know, I've put all available in all levels. The user will learn
	 * when they request the mode and see it not working (or working). That also means....</p>
	 * <p><strong>WARNING: DO NOT TRUST THIS FUNCTION TO REBOOT TO THE REQUESTED MODE!!!</strong> Useful to read this
	 * if the function call is essential to security or something very important. <strong>Only trust it to reboot to the
	 * recovery mode and nothing else.</strong></p>
	 * <p>Attention: in case the reboot is issued with a shell command (mentioned permission not granted), this function
	 * will NOT return (no idea how to change that, currently).</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link UtilsAndroid#MODE_NORMAL} --> for {@code mode}: reboot device normally</p>
	 * <p>- {@link UtilsAndroid#MODE_SAFE} --> for {@code mode}: reboot device into Safe Mode</p>
	 * <p>- {@link UtilsAndroid#MODE_RECOVERY} --> for {@code mode}: reboot device into the Recovery</p>
	 * <p>- {@link UtilsAndroid#MODE_BOOTLOADER} --> for {@code mode}: reboot device into the Bootloader (Fastboot)</p>
	 * <p>- {@link UtilsAndroid#MODE_FAST} --> for {@code mode}: fast reboot device (reboot the userspace)</p>
	 * <br>
	 * <p>- {@link UtilsAndroid#NOT_AVAILABLE} --> for the returning value: power service not available</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param mode one of the constants
	 *
	 * @return one of the constants or an SH shell exit code
	 */
	public static int rebootDevice(final int mode) {
		final String reason;
		switch (mode) {
			case (UtilsAndroid.MODE_RECOVERY): {
				// "recovery" exists at minimum since API 15. This constant doesn't, but what matters is the string,
				// which is always the same, so it's fully supported, even if the constant didn't exist at the beginning.
				reason = PowerManager.REBOOT_RECOVERY;

				break;
			}
			case (UtilsAndroid.MODE_SAFE): {
				reason = PowerManager.REBOOT_SAFE_MODE;

				break;
			}
			case (UtilsAndroid.MODE_BOOTLOADER): {
				reason = "bootloader";

				break;
			}
			case (UtilsAndroid.MODE_FAST): {
				reason = PowerManager.REBOOT_USERSPACE;

				break;
			}
			default: {
				// This constant below only exists from Nougat onwards. Before that, it will be an unrecognized constant by
				// the kernel, probably, and will just restart normally, I suppose. So let it be here, to simplify things,
				// and don't check the version to put an empty string (equivalent of a normal reboot).
				reason = PowerManager.REBOOT_REQUESTED_BY_DEVICE_OWNER;

				break;
			}
		}

		if (UtilsPermsAuths.checkSelfPermission(Manifest.permission.REBOOT)) {
			final IBinder iBinder = UtilsContext.getService(Context.POWER_SERVICE);
			if (null == iBinder) {
				return UtilsAndroid.NOT_AVAILABLE;
			}

			final IPowerManager iPowerManager = IPowerManager.Stub.asInterface(iBinder);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				// This below is the implementation of the PowerManager.reboot() function. But I need to set 'wait' to
				// false, so here is a re-implementation.
				try {
					iPowerManager.reboot(false, reason, false);

					return UtilsShell.ErrCodes.NO_ERR;
				} catch (final Exception ignored) {
				}
			} else {
				final Method method = UtilsReflection.getMethod(IPowerManager.class, "reboot", String.class);
				assert null != method; // Will never happen.
				// The return won't be null either.
				final boolean ret_method = (boolean) UtilsReflection.invokeMethod(method, iPowerManager, reason).ret_var;

				if (ret_method) {
					return UtilsShell.ErrCodes.NO_ERR;
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
		final List<String> commands = new ArrayList<>(2);
		commands.add("svc power reboot " + reason);
		commands.add("am broadcast -a " + Intent.ACTION_REBOOT);

		// As with the shutdown, execution will only get here if there was some error - but it can get here.
		return UtilsShell.executeShellCmd(true, commands).exit_code;
	}

	/**
	 * <p>Toggles Android's Battery Saver mode from Android 5.0 onwards (below that it doesn't exist).</p>
	 *
	 * @param enabled true to enable, false to disable
	 *
	 * @return an SH shell exit code
	 */
	@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
	public static int setBatterySaverEnabled(final boolean enabled) {
		final boolean success;
		if (UtilsPermsAuths.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS)) {
			success = Settings.Global.putInt(UtilsContext.getContext().getContentResolver(),
					Settings.Global.LOW_POWER_MODE, enabled ? 1 : 0);

			if (success) {
				final List<String> commands = new ArrayList<>(2);
				commands.add("am broadcast -a " + PowerManager.ACTION_POWER_SAVE_MODE_CHANGED + " --ez mode " +
						enabled + " --receiver-registered-only");
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
					commands.add("am broadcast -a " + PowerManager.ACTION_POWER_SAVE_MODE_CHANGED_INTERNAL +
							" --ez mode " + enabled);
				}

				return UtilsShell.executeShellCmd(true, commands).exit_code;
			}
		}

		return UtilsShell.ErrCodes.PERM_DENIED;
	}

	/**
	 * <p>Check if the Battery Saver mode is enabled or not.</p>
	 *
	 * @return true if enabled, false otherwise or if the power service is not available on the device
	 */
	@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
	public static boolean getBatterySaverEnabled() {
		final PowerManager powerManager = (PowerManager) UtilsContext.getSystemService(Context.POWER_SERVICE);
		if (null == powerManager) {
			return false;
		}

		return powerManager.isPowerSaveMode();
	}

	/**
	 * <p>Turn the screen on.</p>
	 *
	 * @return true if the screen was turned on, false if the power service is not available on the device
	 */
	public static boolean turnScreenOn() {
		final PowerManager powerManager = (PowerManager) UtilsContext.getSystemService(Context.POWER_SERVICE);
		if (null == powerManager) {
			return false;
		}

		final PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK |
				PowerManager.ACQUIRE_CAUSES_WAKEUP |
				PowerManager.ON_AFTER_RELEASE, UtilsContext.getContext().getPackageName()+"::WakeLock");

		// Acquire and release the wakelock so that the screen turns on and the CPU can turn it off whenever it wants
		// because we no longer want it on.
		wakeLock.acquire(1L);

		return true;
	}

	/**
	 * <p>Turn the screen off by pressing the Power key.</p>
	 *
	 * @return true if the screen was turned off, false if there are no root permissions
	 */
	public static boolean turnScreenOff_TEST_THIS() {
		// todo To be tested
		return UtilsShell.noErr(UtilsShell.executeShellCmd(true, "input keyevent 26").exit_code);
	}
}
