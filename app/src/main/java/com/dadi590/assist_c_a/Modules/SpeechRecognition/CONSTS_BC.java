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

/**
 * <p>Actions, extras, and classes to use to send a broadcast to this module.</p>
 * <br>
 * <p>Check the doc on the action string to know what to do.</p>
 */
final class CONSTS_BC {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private CONSTS_BC() {
	}

	/**
	 * <p>Explanation: starts Google speech recognizer.</p>
	 * <p>Main executed function: {@link UtilsSpeechRecognizers#startGoogleRecognition()}.</p>
	 * <p>Is broadcast by the class(es): {@link UtilsSpeechRecognizersBC}.</p>
	 * <p>To be received only by the class(es): {@link SpeechRecognitionCtrl}.</p>
	 * <p>Extras: none.</p>
	 */
	static final String ACTION_START_GOOGLE = "SPEECH_RECOG_ACTION_START_GOOGLE";

	/**
	 * <p>Explanation: starts PocketSphinx speech recognizer.</p>
	 * <p>Main executed function: {@link UtilsSpeechRecognizers#startPocketSphinxRecognition()}.</p>
	 * <p>Is broadcast by the class(es): {@link UtilsSpeechRecognizersBC}.</p>
	 * <p>To be received only by the class(es): {@link SpeechRecognitionCtrl}.</p>
	 * <p>Extras: none.</p>
	 */
	static final String ACTION_START_POCKET_SPHINX = "SPEECH_RECOG_ACTION_START_POCKET_SPHINX";

	/**
	 * <p>Explanation: stops all speech recognizers until there is a specific request to enable them back by their
	 * respective functions (calling any of them will restart the speech recognition system).</p>
	 * <p>Main executed function: {@link UtilsSpeechRecognizers#terminateSpeechRecognizers()}.</p>
	 * <p>Is broadcast by the class(es): {@link UtilsSpeechRecognizersBC}.</p>
	 * <p>To be received only by the class(es): {@link SpeechRecognitionCtrl}.</p>
	 * <p>Extras: none.</p>
	 */
	static final String ACTION_STOP_RECOGNITION = "SPEECH_RECOG_ACTION_STOP_RECOGNITION";

	/**
	 * <p>Explanation: informs that Google's speech recognition has began.</p>
	 * <p>Is broadcast by the class(es): {@link GoogleRecognition}.</p>
	 * <p>To be received only by the class(es): any chosen class.</p>
	 * <p>Extras: none.</p>
	 */
	public static final String ACTION_GOOGLE_RECOG_STARTED = "ACTION_GOOGLE_RECOG_STARTED";

	/**
	 * <p>Explanation: informs that PocketSphinx's speech recognition has began.</p>
	 * <p>Is broadcast by the class(es): {@link PocketSphinxRecognition}.</p>
	 * <p>To be received only by the class(es): any chosen class.</p>
	 * <p>Extras: none.</p>
	 */
	public static final String ACTION_POCKETSPHINX_RECOG_STARTED = "ACTION_POCKETSPHINX_RECOG_STARTED";
}
