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

package com.dadi590.assist_c_a.BroadcastRecvs.DeviceAdmin;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.dadi590.assist_c_a.GlobalUtils.UtilsServices;
import com.dadi590.assist_c_a.Modules.ProtectedLockScr.UtilsProtectedLockScr;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.Modules.Speech.UtilsSpeech2BC;

/**
 * <p>The Device Administration Receiver, which handles all received administration actions.</p>
 */
public class DeviceAdminRecv extends DeviceAdminReceiver {

	static final int PRIORITY_ADMIN_ENABLED = Speech2.PRIORITY_MEDIUM;

	@Override
	public final void onEnabled(@android.annotation.NonNull final Context context,
								@android.annotation.NonNull final Intent intent) {
		super.onEnabled(context, intent);

		// todo Why doesn't this work...?
		/*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			final ComponentName mAdminName = new ComponentName(context, DeviceAdminRecv.class);
			final DevicePolicyManager devicePolicyManager = (DevicePolicyManager) UtilsGeneral.getContext()
					.getSystemService(Context.DEVICE_POLICY_SERVICE);
			devicePolicyManager.setUninstallBlocked(mAdminName, UtilsGeneral.getContext().getPackageName(), true);
		}*/

		UtilsServices.startMainService();

		UtilsSpeech2BC.speak(CONSTS.SPEAK_ENABLED, null, PRIORITY_ADMIN_ENABLED, null);
	}

	@NonNull
	@Override
	public final CharSequence onDisableRequested(@android.annotation.NonNull final Context context,
												 @android.annotation.NonNull final Intent intent) {
		super.onDisableRequested(context, intent);

		UtilsServices.startMainService();

		UtilsProtectedLockScr.lockAndShowPLS(UtilsProtectedLockScr.getPLSIntent());

		UtilsSpeech2BC.speak(CONSTS.SPEAK_DISABLE_REQUESTED, null, Speech2.PRIORITY_HIGH, null);
		// Why PRIORITY_CRITICAL? Because onDisabled() also has it, so they have the same priority. And onDisabled()
		// skips this speech in case it's being spoken, so it's all good.
		// EDIT: it's on HIGH now. Why CRITICAL... Critical thing is when it's disabled. If the user is just
		// checking something, they don't need to have the phone screaming. If CRITICAL is to be set again, don't
		// forget of skipping this speech because onDisabled() has top priority since the app might shut down.

		return CONSTS.RET_STR_DISABLE_REQUESTED;
	}

	@Override
	public final void onDisabled(@android.annotation.NonNull final Context context,
								 @android.annotation.NonNull final Intent intent) {
		//super.onDisabled(context, intent); - the less things here the better (Why? Refer to CONSTS.SPEAK_DISABLED.)

		UtilsServices.startMainService();

		UtilsProtectedLockScr.lockAndShowPLS(UtilsProtectedLockScr.getPLSIntent());

		UtilsSpeech2BC.speak(CONSTS.SPEAK_DISABLED, Speech2.NO_ADDITIONAL_COMMANDS, Speech2.PRIORITY_CRITICAL, null);
		// Why PRIORITY_CRITICAL? Refer to CONSTS.SPEAK_DISABLED.
		// todo HE'LL SPEAK AND LEAVE THE PHONE WITH THE DO NOT DISTURB AND THE MAX VOLUME IF IT'S STOPPED IN
		//  THE MIDDLE!!!!!! How do you fix that.....? You don't, right? xD Cool. No idea.
		// GET THE SECONDARY APP RESETTING IT!!! (The one which will restart this one...)

		// This below is in case the administrator mode was enabled, but was disabled right after. The assistant
		// would still say the administrator mode is enabled after saying it was disabled --> wtf. This fixes that.
		UtilsSpeech2BC.removeSpeechByStr(CONSTS.SPEAK_ENABLED, PRIORITY_ADMIN_ENABLED, true);
		// todo This is not removing the speech, I think.....
	}
}
