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

package com.dadi590.assist_c_a.MainSrvc;

import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.dadi590.assist_c_a.GlobalUtils.UtilsCertificates;
import com.dadi590.assist_c_a.GlobalUtils.UtilsContext;
import com.dadi590.assist_c_a.GlobalUtils.UtilsProcesses;
import com.dadi590.assist_c_a.GlobalUtils.UtilsServices;
import com.dadi590.assist_c_a.Modules.AudioRecorder.UtilsAudioRecorderBC;
import com.dadi590.assist_c_a.Modules.SpeechRecognitionCtrl.UtilsSpeechRecognizersBC;
import com.dadi590.assist_c_a.R;
import com.dadi590.assist_c_a.Modules.PreferencesManager.Registry.ValuesRegistry;
import com.dadi590.assist_c_a.Modules.PreferencesManager.Registry.UtilsRegistry;

/**
 * <p>Main Service related utilities.</p>
 */
public final class UtilsMainSrvc {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsMainSrvc() {
	}

	/**
	 * <p>Specifically starts the main service doing any things required before or after starting it.</p>
	 * <p>What it does:</p>
	 * <p>- Checks if the app is signed by its supposed certificates, and if it's not, it will kill itself silently;</p>
	 * <p>- Starts the Main Service.</p>
	 */
	public static void startMainService() {
		if (UtilsCertificates.isThisAppCorrupt()) {
			// This is just in case it's possible to patch the APK like it is with binary files without needing the
			// source. So in this case, a new APK must be installed, and the current one can't be modified, or the
			// signature will change. Though if it can be patched, maybe this can too be patched. Whatever.
			// It's also in case something changes on the APK because of some corruption. The app won't start.
			android.os.Process.killProcessQuiet(UtilsProcesses.getCurrentPID());

			return;
		}

		UtilsServices.startService(MainSrvc.class, null, true, false);
	}

	public static final int DETECTION_ACTIVATED = 0;
	public static final int UNSUPPORTED_OS_VERSION = 1;
	public static final int UNSUPPORTED_HARDWARE = 2;
	public static final int PERMISSION_DENIED = 3;
	/**
	 * <p>This activates the detection of a long power button press.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #DETECTION_ACTIVATED} --> for the returning value: the detection was activated successfully</p>
	 * <p>- {@link #UNSUPPORTED_OS_VERSION} --> for the returning value: the OS is not in a supported version (up until
	 * API 27)</p>
	 * <p>- {@link #UNSUPPORTED_HARDWARE} --> for the returning value: the hardware does not seem to support the
	 * detection</p>
	 * <p>- {@link #PERMISSION_DENIED} --> for the returning value: the permission to draw a screen overlay was denied</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @return one of the constants
	 */
	public static int startLongPwrBtnDetection() {

		// DOESN'T WORK AS OF ANDROID 9/Pie!!!!!!!!!!!!!!!! (SDK 28)
		// Find another solution - or hold until you get an Android Pie device or something that demands it.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			return UNSUPPORTED_OS_VERSION;
		}

		final LinearLayout linearLayout = new LinearLayout(UtilsContext.getContext()) {

			@Override
			public void onCloseSystemDialogs(final String reason) {
				if ("globalactions".equals(reason)) { // "globalactions" == Power Menu
					if (UtilsRegistry.getValue(ValuesRegistry.Keys.IS_RECORDING_AUDIO_INTERNALLY).getData(false)) {
						// If it's recording audio, it must be stopped. So stop and start the hotword recognizer.
						UtilsAudioRecorderBC.recordAudio(false, -1, true);
					} else {
						// If it's not recording audio, start the commands recognizer.
						UtilsSpeechRecognizersBC.startCommandsRecognition();
					}
				}/* else if ("homekey".equals(reason)) {
					// Here the recognizers are stopped because the user might be wanting to start the cmds recognition
					// from the Google App or wherever else and the microphone would be in use - so this stops it.
					// Update: this just detects a home key PRESS, not hold. So it's of no use and is now disabled.
				} else if ("recentapps".equals(reason)) {
                }*/
			}
		};

		linearLayout.setFocusable(true);

		final View view = LayoutInflater.from(UtilsContext.getContext()).inflate(R.layout.power_long_press_service_layout, linearLayout);
		final WindowManager windowManager = (WindowManager) UtilsContext.
				getSystemService(android.content.Context.WINDOW_SERVICE);

		if (windowManager == null) {
			return UNSUPPORTED_HARDWARE;
		} else {
			final WindowManager.LayoutParams params;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				params = new WindowManager.LayoutParams(
						0,
						0,
						WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
						WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
								| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
								| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
								| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
								| WindowManager.LayoutParams.FLAG_SECURE,
						PixelFormat.TRANSPARENT);
			} else {
				params = new WindowManager.LayoutParams(
						0,
						0,
						WindowManager.LayoutParams.TYPE_PHONE,
						WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
								| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
								| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
								| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
								| WindowManager.LayoutParams.FLAG_SECURE,
						PixelFormat.TRANSPARENT);
			}
			params.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
			try {
				windowManager.addView(view, params);
			} catch (final WindowManager.BadTokenException ignored) {
				return PERMISSION_DENIED;
			}
		}

		System.out.println("EEEEEEEEEEEEEEE");

		return DETECTION_ACTIVATED;
	}
}
