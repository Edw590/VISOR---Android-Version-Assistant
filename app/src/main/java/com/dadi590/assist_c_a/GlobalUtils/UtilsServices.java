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

package com.dadi590.assist_c_a.GlobalUtils;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * <p>Global {@link Service}s-related utilities.</p>
 */
public final class UtilsServices {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsServices() {
	}

	/**
	 * <p>Restarts a service by either stopping it normally or terminating it's PID (TERMINATING THE WHOLE PROCESS), and
	 * then starts it again normally (unless it was already started, like by the system).</p>
	 *
	 * @param service_class the class of the service to restart
	 * @param intent same as in {@link #startService(Class, Intent, boolean)}
	 * @param force_restart true to force stopping the service, false to stop normally
	 */
	public static void restartService(@NonNull final Class<?> service_class, @Nullable final Intent intent,
									  final boolean force_restart) {
		if (force_restart) {
			UtilsProcesses.terminatePID(UtilsProcesses.getRunningServicePID(service_class));
		} else {
			stopService(service_class);
		}
		startService(service_class, intent, true, true);
	}

	/**
	 * <p>Stops a service.</p>
	 *
	 * @param service_class the class of the service to stop
	 */
	public static void stopService(@NonNull final Class<?> service_class) {
		final Context context = UtilsGeneral.getContext();
		context.stopService(new Intent(context, service_class));
	}

	/**
	 * <p>Starts a service without additional parameters in case it's not already running.</p>
	 *
	 * @param service_class the class of the service to start
	 * @param intent the intent to use to start the service, or null if it's to use the default intent (no extras or
	 *               anything)
	 * @param foreground from Android 8 Oreo onwards, true to start in foreground as of {@link Build.VERSION_CODES#O},
	 *                   false to start in background; below that, this value has no effect as the service is always
	 *                   started in background
	 * @param check_already_running if true, the startService() will only be called if the service is not running; if
	 *                              false, the function will be called anyways (note that that means onStartCommand()
	 *                              will be called again, as it's called every time startService() is called)
	 */
	public static void startService(@NonNull final Class<?> service_class, @Nullable final Intent intent,
									final boolean foreground, final boolean check_already_running) {
		if (check_already_running && isServiceRunning(service_class)) {
			return;
		}

		final Context context = UtilsGeneral.getContext();
		final Intent intent_to_use;
		if (intent == null) {
			intent_to_use = new Intent(context, service_class);
		} else {
			intent_to_use = intent;
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			if (foreground) {
				context.startForegroundService(intent_to_use);
			}
		}
		// Do NOT call this in high frequency. It's said on the docs that it takes various milliseconds to process this
		// call.
		context.startService(intent_to_use);
	}

	/**
	 * <p>Checks if the given service is running.</p>
	 * <br>
	 * <p>Attention - as of {@link Build.VERSION_CODES#O}, this will only work for services internal to the app! (If the
	 * app is a system app though, all the services on the device will continue to be detected.)</p>
	 *
	 * @param service_class the class of the service to check
	 *
	 * @return true if the service is running, false otherwise
	 */
	public static boolean isServiceRunning(@NonNull final Class<?> service_class) {
		// Note: this method is called automatically before starting services, if it's chosen for it to - so don't put
		// it using too much CPU time. Must be as fast as possible.

		final ActivityManager activityManager = (ActivityManager) UtilsGeneral.getContext()
				.getSystemService(Context.ACTIVITY_SERVICE);
		final String srv_class = service_class.getName();

		for (final ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
			if (srv_class.equals(service.service.getClassName())) {
				return true;
			}
		}

		return false;
	}
}
