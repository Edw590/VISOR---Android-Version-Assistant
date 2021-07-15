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

package com.dadi590.assist_c_a.Modules.ProtectedLockScr;

import android.app.PendingIntent;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.dadi590.assist_c_a.BroadcastRecvs.DeviceAdmin.UtilsDeviceAdmin;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;

/**
 * <p>Utilities related with the Protected Lock Screen.</p>
 */
public final class UtilsProtectedLockScr {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsProtectedLockScr() {
	}

	/**
	 * <p>Locks the device and starts the Protected Lock Screen activity.</p>
	 *
	 * @param intent same as in {@link #showPLS(Intent)}
	 */
	public static void lockAndShowPLS(@NonNull final Intent intent) {
		// Do NOT remove this from here!!! It makes it so that the activity can start instantly even after pressing Home,
		// whether the app is a Device Administrator or not - Android magic XD.
		UtilsDeviceAdmin.lockDevice();

		showPLS(intent);
	}

	/**
	 * <p>ONLY starts the Protected Lock Screen - do NOT use except to keep restarting the PLS every second (we don't
	 * want to lock the device every second...) to be sure it's always on top --> in ANY other case, use
	 * {@link #lockAndShowPLS(Intent)} as a start for more security, and then because of the reason on the method
	 * descrption.</p>
	 *
	 * @param intent the intent to start the Protected Lock Screen activity, got from {@link #getPLSIntent()}
	 */
	static void showPLS(@NonNull final Intent intent) {
		try {
			// This way below seems to work perfectly to start an activity instantly after pressing Home button.
			// Though, it doesn't work as of API 27, according with testing from a StackOverflow user and behaves exactly
			// like startActivity() in this matter.
			PendingIntent.getActivity(UtilsGeneral.getContext(), 0, intent, 0).send();
		} catch (final Throwable ignored) { // This is very important, so Throwable.
			// In case it didn't work for some reason, use the supposed normal way of starting an activity.
			UtilsGeneral.getContext().startActivity(intent);
		}
	}

	/**
	 * <p>Prepares an intent to be used to launch the Protected Lock Screen with {@link #lockAndShowPLS(Intent)}.</p>
	 * <br>
	 * <p>Let this as a global variable declared in the class you want to call {@link #lockAndShowPLS(Intent)} so you
	 * don't have to call this every time (hence, faster call to the mentioned method).</p>
	 *
	 * @return the intent
	 */
	@NonNull
	public static Intent getPLSIntent() {
		final Intent intentPLS = new Intent(UtilsGeneral.getContext(), ProtectedLockScr.class);
		intentPLS.addFlags(
				Intent.FLAG_ACTIVITY_CLEAR_TOP |
				Intent.FLAG_ACTIVITY_SINGLE_TOP |
				Intent.FLAG_ACTIVITY_NEW_TASK
		);

		return intentPLS;
	}
}
