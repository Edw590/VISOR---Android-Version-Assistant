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

package com.dadi590.assist_c_a.Modules.ProtectedLockScr;

import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.android.internal.widget.LockPatternUtils;
import com.dadi590.assist_c_a.BroadcastRecvs.DeviceAdmin.UtilsDeviceAdmin;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.GlobalUtils.UtilsReflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * <p>Utilities related to the Protected Lock Screen.</p>
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
	 *
	 * @return same as in {@link UtilsDeviceAdmin#lockDevice()}
	 */
	public static boolean lockAndShowPLS(@NonNull final Intent intent) {
		// Do NOT remove this from here!!! It makes it so that the activity can start instantly even after pressing Home,
		// whether the app is a Device Administrator or not - Android magic XD.
		final boolean ret_var = UtilsDeviceAdmin.lockDevice();

		showPLS(intent);

		return ret_var;
	}

	/**
	 * <p>ONLY starts the Protected Lock Screen (does NOT lock the device) - do NOT use except to keep restarting the
	 * PLS every second (we don't want to lock the device every second...) to be sure it's always on top --> in ANY
	 * other case, use {@link #lockAndShowPLS(Intent)}, as a start for more security, and then because of the reason on
	 * the function descrption (it also starts the PLS by itself, so no need to call this function in those cases).</p>
	 *
	 * @param intent the intent to start the Protected Lock Screen activity, got from {@link #getPLSIntent()}
	 */
	static void showPLS(@NonNull final Intent intent) {
		try {
			// This way below seems to work perfectly to start an activity instantly after pressing Home button.
			// Though, it doesn't work as of API 27, according with testing from a StackOverflow user and behaves exactly
			// like startActivity() in this matter.
			PendingIntent.getActivity(UtilsGeneral.getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT).send();
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
		final Intent intentPLS = new Intent(UtilsGeneral.getContext(), ProtectedLockScrAct.class);
		intentPLS.addFlags(
				Intent.FLAG_ACTIVITY_CLEAR_TOP |
				Intent.FLAG_ACTIVITY_SINGLE_TOP |
				Intent.FLAG_ACTIVITY_NEW_TASK |
				Intent.FLAG_ACTIVITY_NO_ANIMATION
		);

		return intentPLS;
	}

	/**
	 * <p>Checks if the lock screen is enabled or not for the current user through
	 * {@link LockPatternUtils#isLockScreenDisabled()}, which means, should check the same as
	 * {@link KeyguardManager#isDeviceSecure()}, but on older API versions than Marshmallow (as of which the {@code int}
	 * parameter was introduced on the first function).</p>
	 * <br>
	 * <p><strong>TO USE ONLY WITH API 22 OR OLDER!!!!!</strong></p>
	 *
	 * @return true if the lock screen is enabled for the current user, false otherwise
	 */
	static boolean isLockScreenEnabled22Older() {
		/* todo Testa isto com a meta-reflection para ver se ficou a funcionar!!!
				Podia ser usado por algum atacante enviar MUITOS broadcasts e a app vai bloquear!
			Arranja isso. Mete numa thread e impede de ser chamada mais que uma vez enquanto estiver a funcionar ou mete
				a forçagem de permissões MEGA rápida --> tal como a função de iniciar o serviço, tenta metê-la ao máximo.*/

		final LockPatternUtils lockPatternUtils = new LockPatternUtils(UtilsGeneral.getContext());
		final Method method = UtilsReflection.getMethod(LockPatternUtils.class, "isLockScreenDisabled");
		if (null != method) {
			try {

				return !Boolean.parseBoolean(String.valueOf(method.invoke(lockPatternUtils)));
			} catch (final InvocationTargetException ignored) {
			} catch (final IllegalAccessException ignored) {
			}
		}

		// Won't happen, at least so soon. It exists from 22 to [something?]
		return false;
	}
}
