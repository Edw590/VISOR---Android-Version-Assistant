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

package com.dadi590.assist_c_a.MainSrv;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.BroadcastRecvs.MainBroadcastRecv;
import com.dadi590.assist_c_a.BroadcastRecvs.MainRegBroadcastRecv;
import com.dadi590.assist_c_a.GlobalUtils.GL_BC_CONSTS;
import com.dadi590.assist_c_a.GlobalUtils.GL_CONSTS;
import com.dadi590.assist_c_a.GlobalUtils.ObjectClasses;
import com.dadi590.assist_c_a.GlobalUtils.UtilsApp;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.GlobalUtils.UtilsServices;
import com.dadi590.assist_c_a.Modules.AudioRecorder.AudioRecorder;
import com.dadi590.assist_c_a.Modules.BatteryProcessor.BatteryProcessor;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.Modules.Telephony.PhoneCallsProcessor.PhoneCallsProcessor;

/**
 * The main {@link Service} of the application - MainService.
 */
public class MainSrv extends Service {

	//////////////////////////////////////
	// Class variables

	// Private variables //

	// Not bad, since it's the application context (global for the entire process), not a local one
	//private static Context main_app_context = null; - disabled while using UtilsGeneral.getMainAppContext()

	// Modules instances
	private static final AudioRecorder audioRecorder = new AudioRecorder();
	private static final PhoneCallsProcessor phoneCallsProcessor = new PhoneCallsProcessor();
	private static final MainRegBroadcastRecv mainRegBroadcastRecv = new MainRegBroadcastRecv();
	private static final BatteryProcessor batteryProcessor = new BatteryProcessor();

	// Services to start in order
	private static final Class[] services_to_start = {
			Speech2.class,
	};

	//////////////////////////////////////

	//////////////////////////////////////
	// Getters and setters

	///**.
	// * @return the {@link AudioManager}
	// */
	/*@Nullable
	public static AudioManager getAudioManager() {
		UtilsPermissions.wrapperRequestPerms(null, false);
		UtilsServices.startService(MainSrv.class, true);

		return (AudioManager) main_app_context.getSystemService(AUDIO_SERVICE);
	}*/

	// Global class instances getters //

	// Do NOT put @NonNull on the returning values. If the service has not started OR CRASHED, everything will return
	// null!!!
	// EDIT: actually, since I started using UtilsGeneral.getContext(), all seems to work just fine. With getSpeech(),
	// all works fine as long as I don't return null. If I return the instance, the service starts normally. I'll just
	// leave it on NonNull. This probably also works because I instantiate everything on the declaration now (because
	// of UtilsGeneral.getContext() being always available).

	///**.
	// * @return the main process' Application Context
	// */
	//@Nullable
	//public static Context getMainAppContext() {
	//	UtilsPermissions.wrapperRequestPerms(null, false);
	//	UtilsServices.startService(MainSrv.class, true);
	//
	//	return main_app_context;
	//}
	/**.
	 * @return the global {@link AudioRecorder} instance
	 */
	@NonNull
	public static AudioRecorder getAudioRecorder() {
		UtilsServices.startMainService();

		return audioRecorder;
	}
	/**.
	 * @return the global {@link MainBroadcastRecv} instance
	 */
	@NonNull
	public static MainRegBroadcastRecv getMainRegBroadcastRecv() {
		UtilsServices.startMainService();

		return mainRegBroadcastRecv;
	}
	/**.
	 * @return the global {@link PhoneCallsProcessor} instance
	 */
	@NonNull
	public static PhoneCallsProcessor getPhoneCallsProcessor() {
		UtilsServices.startMainService();

		return phoneCallsProcessor;
	}
	/**.
	 * @return the global {@link BatteryProcessor} instance
	 */
	@NonNull
	public static BatteryProcessor getBatteryProcessor() {
		UtilsServices.startMainService();

		return batteryProcessor;
	}

	//////////////////////////////////////


	@Override
	public final void onCreate() {
		super.onCreate();

		// Do this only once, when the service is created and while it's not destroyed

		final ObjectClasses.NotificationInfo notificationInfo = new ObjectClasses.NotificationInfo(
				GL_CONSTS.CH_ID_MAIN_SRV_FOREGROUND,
				"Main notification",
				"",
				UtilsServices.TYPE_FOREGROUND,
				GL_CONSTS.ASSISTANT_NAME + " Systems running",
				"",
				null
		);
		startForeground(GL_CONSTS.NOTIF_ID_MAIN_SRV_FOREGROUND, UtilsServices.getNotification(notificationInfo));

		// Clear the app cache as soon as it starts not to take unnecessary space
		UtilsApp.deleteAppCache();

		// Before anything else, start the speech module since the assistant must be able to speak.
		// Don't forget inside the speech module there's a function that executes all important things right after
		// the TTS is ready - the second reason this must be in the beginning.
		//speech2 = new Speech2();

		// Put the MainService process' context available statically
		//main_app_context = getApplicationContext();

		// Initialization of all the assistant's modules, after the service was successfully started (means that
		// main_app_context is ready to be used wherever it's needed) along with the speech module.
		//audioRecorder = new AudioRecorder();

		// Register the receiver before the speech module is started
		UtilsGeneral.getContext().registerReceiver(broadcastReceiver, new IntentFilter(GL_BC_CONSTS.ACTION_SPEECH2_READY));

		// Start services in background - no restrictions, since the Main Service is already in foreground
		for (final Class service : services_to_start) {
			UtilsServices.startService(service, false);
		}
	}

	@Override
	public final int onStartCommand(@Nullable final Intent intent, final int flags, final int startId) {
		// Do this below every time the service is started/resumed/whatever

		// Do NOT put ANYTHING here!!!
		// MANY places starting this service don't check if it's already started or not, so this method will be called
		// many times randomly. Put everything on onCreate() which is called only if the service was not running and
		// was just started.

		return START_STICKY;
	}

	/**
	 * <p>The sole purpose of this register is detect when the speech module is ready so the Main Service can start
	 * everything else.</p>
	 */
	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (/*context == null ||*/ intent == null || intent.getAction() == null) {
				return;
			}

			System.out.println("PPPPPPPPPPPPPPPPPP-MainSrv");
			System.out.println(intent.getAction());

			if (intent.getAction().equals(GL_BC_CONSTS.ACTION_SPEECH2_READY)) {
				AfterTtsReady.afterTtsReady();
				UtilsGeneral.getContext().unregisterReceiver(this);
			}
		}
	};

	@Override
	@Nullable
	public final IBinder onBind(@Nullable final Intent intent) {
		return null;
	}
}
