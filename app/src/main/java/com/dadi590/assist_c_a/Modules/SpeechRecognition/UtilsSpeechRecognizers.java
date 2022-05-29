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

package com.dadi590.assist_c_a.Modules.SpeechRecognition;

import android.content.Intent;
import android.media.MediaRecorder;

import com.dadi590.assist_c_a.GlobalUtils.UtilsApp;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.GlobalUtils.UtilsProcesses;
import com.dadi590.assist_c_a.GlobalUtils.UtilsServices;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.Modules.Speech.UtilsSpeech2BC;
import com.dadi590.assist_c_a.ValuesStorage.CONSTS_ValueStorage;
import com.dadi590.assist_c_a.ValuesStorage.ValuesStorage;

import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

/**
 * <p>Utilities for use with Google and PocketSphinx speech recognizers.</p>
 */
public final class UtilsSpeechRecognizers {

	static final String google_app_pkg_name = "com.google.android.googlequicksearchbox";

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsSpeechRecognizers() {
	}

	/**
	 * <p>Checks if the Google speech recognition is available.</p>
	 * <p>It does so by checking if the Google app is installed.</p>
	 *
	 * @return true if it is available for use (Google app installed and enabled), false otherwise
	 */
	public static boolean isGoogleRecogAvailable() {
		return UtilsApp.APP_ENABLED == UtilsApp.appEnabledStatus(google_app_pkg_name);
	}

	/**
	 * <p>Checks if the PocketSphinx speech recognition is available.</p>
	 * <p>It does so by checking if the PocketSphinx library file is available on the device.</p>
	 * <p>Ignore Android Studio saying this always returns true.</p>
	 *
	 * @return true if it is available for use (PocketSphinx library file on the device and on a correct folder), false
	 * otherwise
	 */
	public static boolean isPocketSphinxRecogAvailable() {
		Class<?> temp = SpeechRecognizerSetup.class;
		try {
			temp = SpeechRecognizerSetup.class;

			return true;
		} catch (final UnsatisfiedLinkError ignored) {
			return false;
		}
	}

	/**
	 * <p>Start Google's speech recognition, first calling automatically {@link #terminateSpeechRecognizers()}.</p>
	 */
	static void startGoogleRecognition() {
		System.out.println("WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW");

		terminateSpeechRecognizers();

		final boolean google_recog_available = isGoogleRecogAvailable();
		if (google_recog_available && UtilsGeneral.isAudioSourceAvailable(MediaRecorder.AudioSource.MIC)) {
			final Intent intent = new Intent(UtilsGeneral.getContext(), GoogleRecognition.class);
			intent.putExtra(CONSTS_SpeechRecog.EXTRA_TIME_START, System.currentTimeMillis());
			UtilsServices.startService(GoogleRecognition.class, intent, false);
		} else {
			final Boolean recog_available = (Boolean) ValuesStorage.getValue(CONSTS_ValueStorage.google_recog_available);
			if (null != recog_available && !recog_available) {
				final String speak = "WARNING - The Google App is not enabled or installed!!! Speech recognition will " +
						"not work!!! Please put PocketSphinx recognizing in this case!!!";
				UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, null);
			}
		}

		ValuesStorage.updateValue(CONSTS_ValueStorage.google_recog_available, String.valueOf(google_recog_available));
	}

	/**
	 * <p>Start PocketSphinx's speech recognition, first calling automatically {@link #terminateSpeechRecognizers()}.</p>
	 */
	static void startPocketSphinxRecognition() {
		System.out.println("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");

		terminateSpeechRecognizers();

		final boolean pocketsphinx_recog_available = isPocketSphinxRecogAvailable();
		System.out.println(pocketsphinx_recog_available);
		if (pocketsphinx_recog_available && UtilsGeneral.isAudioSourceAvailable(MediaRecorder.AudioSource.MIC)) {
			final Intent intent = new Intent(UtilsGeneral.getContext(), PocketSphinxRecognition.class);
			intent.putExtra(CONSTS_SpeechRecog.EXTRA_TIME_START, System.currentTimeMillis());
			UtilsServices.startService(PocketSphinxRecognition.class, intent, false);
		} else {
			final Boolean recog_available = (Boolean) ValuesStorage.getValue(CONSTS_ValueStorage.pocketsphinx_recog_available);
			if (null != recog_available && !recog_available) {
				final String speak = "Attention - Background speech recognition is not available. PocketSphinx's " +
						"library file was not detected.";
				UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, null);
			}
		}

		ValuesStorage.updateValue(CONSTS_ValueStorage.pocketsphinx_recog_available,
				String.valueOf(pocketsphinx_recog_available));
	}

	/**
	 * <p>Terminate the PID of the PocketSphinx and Google speech recognizers' processes.</p>
	 */
	static void terminateSpeechRecognizers() {
		System.out.println("YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
		UtilsProcesses.terminatePID(UtilsProcesses.getRunningServicePID(PocketSphinxRecognition.class));
		UtilsProcesses.terminatePID(UtilsProcesses.getRunningServicePID(GoogleRecognition.class));
	}
}
