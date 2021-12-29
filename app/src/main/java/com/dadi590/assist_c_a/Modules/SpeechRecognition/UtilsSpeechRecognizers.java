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

import com.dadi590.assist_c_a.GlobalUtils.UtilsApp;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.GlobalUtils.UtilsProcesses;
import com.dadi590.assist_c_a.GlobalUtils.UtilsServices;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.Modules.Speech.UtilsSpeech2BC;

/**
 * <p>Utilities for use with Google and PocketSphinx speech recognizers.</p>
 */
final class UtilsSpeechRecognizers {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsSpeechRecognizers() {
	}

	/**
	 * <p>Start Google's speech recognition, first calling automatically {@link #terminateSpeechRecognizers()}.</p>
	 */
	static void startGoogleRecognition() {
		System.out.println("WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW");
		final String google_app_package_name = "com.google.android.googlequicksearchbox";

		if (UtilsApp.appEnabledStatus(google_app_package_name) == UtilsApp.APP_ENABLED) {
			terminateSpeechRecognizers();

			final Intent intent = new Intent(UtilsGeneral.getContext(), GoogleRecognition.class);
			intent.putExtra(CONSTS.EXTRA_TIME_START, System.currentTimeMillis());
			UtilsServices.startService(GoogleRecognition.class, intent, false);
		} else {
			final String speak = "WARNING - The Google App is not enabled or installed!!! Speech recognition will " +
					"not work!!! Please put PocketSphinx recognizing in this case!!!";
			UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_USER_ACTION, null);
		}
	}

	/**
	 * <p>Start PocketSphinx's speech recognition, first calling automatically {@link #terminateSpeechRecognizers()}.</p>
	 */
	static void startPocketSphinxRecognition() {
		System.out.println("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");

		terminateSpeechRecognizers();

		final Intent intent = new Intent(UtilsGeneral.getContext(), PocketSphinxRecognition.class);
		intent.putExtra(CONSTS.EXTRA_TIME_START, System.currentTimeMillis());
		UtilsServices.startService(PocketSphinxRecognition.class, intent, false);
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
