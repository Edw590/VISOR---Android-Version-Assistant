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

/**
 * <p>Functions to call to communicate with {@link SpeechRecognitionCtrl}, by using broadcasts.</p>
 */
public final class UtilsSpeechRecognizersBC {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsSpeechRecognizersBC() {
	}

	/**
	 * <p>Broadcasts a request to start Google speech recognition.</p>
	 */
	public static void startGoogleRecognition() {
		final Intent broadcast_intent = new Intent(CONSTS_BC.ACTION_START_GOOGLE);

		UtilsApp.sendInternalBroadcast(broadcast_intent);
	}

	/**
	 * <p>Broadcasts a request to start PocketSphinx speech recognition.</p>
	 */
	public static void startPocketSphinxRecognition() {
		final Intent broadcast_intent = new Intent(CONSTS_BC.ACTION_START_POCKET_SPHINX);

		UtilsApp.sendInternalBroadcast(broadcast_intent);
	}

	/**
	 * <p>Broadcasts a request to stop all speech recognizers until there is a specific request to enable them back by
	 * their respective functions (calling any of them will restart the speech recognition system).</p>
	 */
	public static void stopRecognition() {
		final Intent broadcast_intent = new Intent(CONSTS_BC.ACTION_STOP_RECOGNITION);

		UtilsApp.sendInternalBroadcast(broadcast_intent);
	}
}
