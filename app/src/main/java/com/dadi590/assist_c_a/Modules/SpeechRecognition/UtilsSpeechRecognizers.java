/*
 * Copyright 2022 DADi590
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

package com.dadi590.assist_c_a.Modules.SpeechRecognition;

import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Build;

import com.dadi590.assist_c_a.GlobalUtils.UtilsApp;
import com.dadi590.assist_c_a.GlobalUtils.UtilsCryptoHashing;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.GlobalUtils.UtilsNativeLibs;
import com.dadi590.assist_c_a.GlobalUtils.UtilsProcesses;
import com.dadi590.assist_c_a.GlobalUtils.UtilsServices;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.Modules.Speech.UtilsSpeech2BC;
import com.dadi590.assist_c_a.ModulesList;
import com.dadi590.assist_c_a.ValuesStorage.CONSTS_ValueStorage;
import com.dadi590.assist_c_a.ValuesStorage.ValuesStorage;

/**
 * <p>Utilities for use with Google and PocketSphinx speech recognizers.</p>
 */
public final class UtilsSpeechRecognizers {

	static final String google_app_pkg_name = "com.google.android.googlequicksearchbox";

	private static final String[] hashes_PocketSphinx_lib_files = {
			// All SHA-512
			"11020BE3E4B715E95636C82A9A58EE5F069B367207DC633D7FCA8E4B705E9E90A5FA11C9E5F57D085B1AD9FB4A8AFA96DBCB49F9067A8BBB693E3C91F3EA2B16", // x86_64
			"6CD41CE2CD335C8ADABCC461B081EB401C36B2B6F0412BF5D39053444F3A7C714B7438A32CAC3FBE0E11D65DB87ED138C9E34C0E7B331104A9E1DEAB2E0CB5A9", // x86
			"4FDC0E4A229597D1F8C5E0D41B22E16F6AD636D8936E1EEED3BE65D09F369B6471444B73DFC7013E937D6C83E97098D1520D013F6AB60DFA619A9803B7A6DB03", // armeabi-v7a
			"2EABF3A079C428A12F246B8FC0E097E3A0F7EC23B02F3D074A3AB04F40BDF0788909A891B081E2A3F116D55637B434AB9DD9AA49454410036E18AA66A54133E6", // arm64-v8a
	};

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsSpeechRecognizers() {
	}

	/**
	 * <p>Checks if the Google app is installed and enabled for the Google speech recognition.</p>
	 *
	 * @return true if it is the Google app is installed and enabled, false otherwise
	 */
	static boolean isGoogleAppEnabled() {
		return UtilsApp.APP_ENABLED == UtilsApp.appEnabledStatus(google_app_pkg_name);
	}

	/**
	 * <p>Checks if the correct PocketSphinx speech recognition library file is available on the device and on a correct
	 * folder.</p>
	 *
	 * @return true if the library is available, false otherwise
	 */
	static boolean isPocketSphinxLibAvailable() {
		if (!UtilsNativeLibs.isPrimaryNativeLibAvailable(UtilsNativeLibs.POCKETSPHINX_LIB_NAME)) {
			return false;
		}

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			return UtilsCryptoHashing.fileMatchesHash(UtilsNativeLibs.getPrimaryNativeLibsPath() + "/" +
					UtilsNativeLibs.POCKETSPHINX_LIB_NAME, hashes_PocketSphinx_lib_files, UtilsCryptoHashing.IDX_SHA512);
		} else {
			// This assumes the APK was not modified to include a modified library (no idea how to check the file inside
			// the APK file).

			return true;
		}
	}

	/**
	 * <p>Start Google's speech recognition, first calling automatically, controller is running.</p>
	 */
	static void startGoogleRecognition() {
		System.out.println("WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW");
		if (!ModulesList.isElementRunning(ModulesList.getElementIndex(SpeechRecognitionCtrl.class))) {
			return;
		}

		try {
			// Wait just a bit before checking if the microphone is available or not (has proven useful on BV9500).
			Thread.sleep(500);
		} catch (final InterruptedException ignored) {
			Thread.currentThread().interrupt();
		}

		if (UtilsGeneral.isAudioSourceAvailable(MediaRecorder.AudioSource.MIC)) {
			final Intent intent = new Intent(UtilsGeneral.getContext(), GoogleRecognition.class);
			intent.putExtra(CONSTS_SpeechRecog.EXTRA_TIME_START, System.currentTimeMillis());
			UtilsServices.startService(GoogleRecognition.class, intent, false, true);
		} else {
			final String speak = "Resources are busy";
			UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, null);
		}
	}

	/**
	 * <p>Start PocketSphinx's speech recognition, first calling automatically, if the controller is running.</p>
	 */
	static void startPocketSphinxRecognition() {
		System.out.println("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
		if (!ModulesList.isElementRunning(ModulesList.getElementIndex(SpeechRecognitionCtrl.class))) {
			return;
		}

		final boolean pocketsphinx_recog_available = isPocketSphinxLibAvailable();
		if (pocketsphinx_recog_available && UtilsGeneral.isAudioSourceAvailable(MediaRecorder.AudioSource.MIC)) {
			final Intent intent = new Intent(UtilsGeneral.getContext(), PocketSphinxRecognition.class);
			intent.putExtra(CONSTS_SpeechRecog.EXTRA_TIME_START, System.currentTimeMillis());
			UtilsServices.startService(PocketSphinxRecognition.class, intent, false, true);
		} else {
			final Boolean recog_available = (Boolean) ValuesStorage.getValue(CONSTS_ValueStorage.pocketsphinx_recog_available);
			if (null == recog_available || recog_available) { // Warn only once when it was there and stopped being.
				final String speak = "Attention - Background speech recognition is not available. Either the microphone" +
						"is being used already or PocketSphinx's correct library file was not found.";
				UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, null);
			}
		}

		// Update the Values Storage
		ValuesStorage.updateValue(CONSTS_ValueStorage.pocketsphinx_recog_available,
				String.valueOf(pocketsphinx_recog_available));
	}

	/**
	 * <p>Terminate the PID of the PocketSphinx and Google speech recognizers' processes, if they're running.</p>
	 */
	static void terminateSpeechRecognizers() {
		System.out.println("YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
		UtilsProcesses.terminatePID(UtilsProcesses.getRunningServicePID(PocketSphinxRecognition.class));
		UtilsProcesses.terminatePID(UtilsProcesses.getRunningServicePID(GoogleRecognition.class));
	}
}
