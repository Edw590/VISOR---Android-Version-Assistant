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

package com.edw590.visor_c_a.Modules.SpeechRecognitionCtrl;

import android.content.Intent;
import android.os.Build;
import android.speech.RecognizerIntent;

import com.edw590.visor_c_a.GlobalUtils.GL_CONSTS;
import com.edw590.visor_c_a.GlobalUtils.UtilsContext;
import com.edw590.visor_c_a.GlobalUtils.UtilsCryptoHashing;
import com.edw590.visor_c_a.GlobalUtils.UtilsGeneral;
import com.edw590.visor_c_a.GlobalUtils.UtilsNativeLibs;
import com.edw590.visor_c_a.GlobalUtils.UtilsNotifications;
import com.edw590.visor_c_a.GlobalUtils.UtilsProcesses;
import com.edw590.visor_c_a.GlobalUtils.UtilsServices;

/**
 * <p>Utilities for use with the commands and PocketSphinx speech recognizers.</p>
 */
public final class UtilsSpeechRecognizers {

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
	 * <p>Checks if an app is available for the {@link RecognizerIntent#ACTION_RECOGNIZE_SPEECH} intent.</p>
	 *
	 * @return true if there's one, false otherwise
	 */
	static boolean isCmdsRecogAppAvailable() {
		return UtilsGeneral.isIntentActionAvailable(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
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
					UtilsNativeLibs.POCKETSPHINX_LIB_NAME, hashes_PocketSphinx_lib_files);
		} else {
			// This assumes the APK was not modified to include a modified library (no idea how to check the file inside
			// the APK file).

			return true;
		}
	}

	/**
	 * <p>Start the commands speech recognition asynchronously.</p>
	 */
	static void startCommandsRecognition() {
		// No need to check if the cmds recognition is supported or not because the Controller will only be activated if
		// the recognition is available (checked on isSupported() every CHECK_TIME on the Manager).

		final Intent intent = new Intent(UtilsContext.getContext(), CommandsRecognition.class);
		intent.putExtra(CONSTS_SpeechRecog.EXTRA_TIME_START, System.currentTimeMillis());
		UtilsServices.startService(CommandsRecognition.class, intent, false, false, false);
	}

	/**
	 * <p>Start PocketSphinx's speech recognition synchronously.</p>
	 *
	 * @return true if the recognition started, false if an error occurred
	 */
	static boolean startPocketSphinxRecognition() {


		return true;


		/*final boolean started;

		final boolean pocketsphinx_recog_available = isPocketSphinxLibAvailable();
		if (pocketsphinx_recog_available) {
			started = PocketSphinxRecognition.startListening();
		} else {
			// Set no-value to be equal to true value (so that VISOR warns anyway even before the value being there -
			// would mean it's assuming the recognition was there, or just to warn the user at the app startup).
			final boolean recog_available = UtilsRegistry.getValue(ValuesRegistry.Keys.POCKETSPHINX_RECOG_AVAILABLE).
					getData(true);
			if (recog_available) { // Warn only once when it was there and stopped being.
				final String speak = "Attention - Background speech recognition is not available. PocketSphinx's " +
						"correct library file was not found.";
				UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, 0, null);
			}

			started = false;
		}

		// Update the Values Storage
		UtilsRegistry.setValue(ValuesRegistry.Keys.POCKETSPHINX_RECOG_AVAILABLE, pocketsphinx_recog_available);

		return started;*/
	}

	/**
	 * <p>Stop both speech recognizers synchronously.</p>
	 */
	static void stopSpeechRecognizers() {
		stopPocketSphinxRecognition();
		stopCommandsRecognizer();
	}

	/**
	 * <p>Stop the commands speech recognizer synchronously.</p>
	 */
	static void stopCommandsRecognizer() {
		UtilsProcesses.killPID(UtilsProcesses.getRunningServicePID(CommandsRecognition.class));
		UtilsNotifications.cancelNotification(GL_CONSTS.NOTIF_ID_COMMANDS_RECOG_FOREGROUND);
	}

	/**
	 * <p>Stop PocketSphinx's speech recognizer synchronously.</p>
	 */
	static void stopPocketSphinxRecognition() {
		PocketSphinxRecognition.stopListening();
	}
}
