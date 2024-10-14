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

package com.edw590.visor_c_a.BroadcastRecvs.DeviceAdmin;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.edw590.visor_c_a.MainSrvc.UtilsMainSrvc;
import com.edw590.visor_c_a.Modules.ProtectedLockScr.UtilsProtectedLockScr;
import com.edw590.visor_c_a.Modules.Speech.Speech2;
import com.edw590.visor_c_a.Modules.Speech.UtilsSpeech2BC;

/**
 * <p>The Device Administration Receiver, which handles all received administration actions.</p>
 */
public final class DeviceAdminRecv extends DeviceAdminReceiver {

	static final int PRIORITY_ADMIN_ENABLED = Speech2.PRIORITY_MEDIUM;

	@Override
	public void onEnabled(@android.annotation.NonNull final Context context,
								@android.annotation.NonNull final Intent intent) {
		super.onEnabled(context, intent);

		// todo Why doesn't this work...?
		/*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			final ComponentName mAdminName = new ComponentName(context, DeviceAdminRecv.class);
			final DevicePolicyManager devicePolicyManager = (DevicePolicyManager) UtilsGeneral
					.getSystemService(Context.DEVICE_POLICY_SERVICE);
			devicePolicyManager.setUninstallBlocked(mAdminName, UtilsGeneral.getContext().getPackageName(), true);
		}*/

		// Not used to have this here if the app is to force the authorization from time to time.
		//UtilsSpeech2BC.speak(CONSTS.SPEAK_ENABLED, PRIORITY_ADMIN_ENABLED, null);

		UtilsMainSrvc.startMainService();
	}

	@NonNull
	@Override
	public CharSequence onDisableRequested(@android.annotation.NonNull final Context context,
												 @android.annotation.NonNull final Intent intent) {
		super.onDisableRequested(context, intent);

		UtilsProtectedLockScr.lockAndShowPLS(UtilsProtectedLockScr.getPLSIntent());

		UtilsSpeech2BC.speak(CONSTS.SPEAK_DISABLE_REQUESTED, Speech2.PRIORITY_HIGH, 0, true, null);

		UtilsMainSrvc.startMainService();

		return CONSTS.RET_STR_DISABLE_REQUESTED;
	}

	@Override
	public void onDisabled(@android.annotation.NonNull final Context context,
								 @android.annotation.NonNull final Intent intent) {
		//super.onDisabled(context, intent); - the less things here the better (Why? Refer to CONSTS.SPEAK_DISABLED)

		UtilsProtectedLockScr.lockAndShowPLS(UtilsProtectedLockScr.getPLSIntent());

		// todo This is not bypassing DND and vibration mode......!!!!!!!!!!!!!!!!!!!!
		UtilsSpeech2BC.speak(CONSTS.SPEAK_DISABLED, Speech2.PRIORITY_CRITICAL, 0, false, null);
		// Why PRIORITY_CRITICAL? Refer to CONSTS.SPEAK_DISABLED.
		// todo HE'LL SPEAK AND LEAVE THE PHONE WITH THE DO NOT DISTURB AND THE MAX VOLUME IF IT'S STOPPED IN
		//  THE MIDDLE!!!!!! How do you fix that.....? You don't, right? xD Cool. No idea.
		// GET THE SECONDARY APP RESETTING IT!!! (The one which will restart this one...)

		// This below is in case the administrator mode was enabled, but was disabled right after. The assistant
		// would still say the administrator mode is enabled after saying it was disabled --> wtf. This fixes that.
		UtilsSpeech2BC.removeSpeechByStr(CONSTS.SPEAK_ENABLED, PRIORITY_ADMIN_ENABLED, true);

		UtilsMainSrvc.startMainService();
	}
}
