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

package com.dadi590.assist_c_a.Modules.Speech;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.GlobalUtils.UtilsApp;

import java.util.ArrayList;

/**
 * <p>Functions to call to send information to {@link Speech2}, by using broadcasts.</p>
 */
public final class UtilsSpeech2BC {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsSpeech2BC() {
	}

	/**
	 * <p>Broadcasts a request to execute {@link Speech2#speak(String, int, Integer)}.</p>
	 *  @param txt_to_speak same as in {@link Speech2#speak(String, int, Integer)}
	 * @param speech_priority same as in {@link Speech2#speak(String, int, Integer)}
	 * @param after_speaking_code same as in {@link Speech2#speak(String, int, Integer)}
	 */
	public static void speak(@NonNull final String txt_to_speak, final int speech_priority,
							 @Nullable final Integer after_speaking_code) {
		final Intent broadcast_intent = new Intent(CONSTS_BC.ACTION_CALL_SPEAK);
		broadcast_intent.putExtra(CONSTS_BC.EXTRA_CALL_SPEAK_1, txt_to_speak);
		broadcast_intent.putExtra(CONSTS_BC.EXTRA_CALL_SPEAK_3, speech_priority);
		if (after_speaking_code != null) {
			broadcast_intent.putExtra(CONSTS_BC.EXTRA_CALL_SPEAK_4, after_speaking_code);
		}

		UtilsApp.sendInternalBroadcast(broadcast_intent);
	}

	/**
	 * <p>Broadcasts a request to execute {@link Speech2#skipCurrentSpeech()}.</p>
	 */
	public static void skipCurrentSpeech() {
		final Intent broadcast_intent = new Intent(CONSTS_BC.ACTION_SKIP_SPEECH);

		UtilsApp.sendInternalBroadcast(broadcast_intent);
	}

	/**
	 * <p>Broadcasts a request to remove a speech by its speech string.</p>
	 * <p>More info on {@link CONSTS_BC#ACTION_REMOVE_SPEECH}.</p>
	 *
	 * @param speech same as in {@link UtilsSpeech2#getSpeechIdBySpeech(String, int, boolean, ArrayList)}
	 * @param speech_priority same as in {@link UtilsSpeech2#getSpeechIdBySpeech(String, int, boolean, ArrayList)}
	 * @param low_to_high same as in {@link UtilsSpeech2#getSpeechIdBySpeech(String, int, boolean, ArrayList)}
	 */
	public static void removeSpeechByStr(@NonNull final String speech, final int speech_priority,
										 final boolean low_to_high) {
		final Intent broadcast_intent = new Intent(CONSTS_BC.ACTION_REMOVE_SPEECH);
		broadcast_intent.putExtra(CONSTS_BC.EXTRA_REMOVE_SPEECH_1, speech);
		broadcast_intent.putExtra(CONSTS_BC.EXTRA_REMOVE_SPEECH_2, speech_priority);
		broadcast_intent.putExtra(CONSTS_BC.EXTRA_REMOVE_SPEECH_3, low_to_high);

		UtilsApp.sendInternalBroadcast(broadcast_intent);
	}
}
