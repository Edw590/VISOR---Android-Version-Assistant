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

package com.edw590.visor_c_a.Modules.Speech;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.edw590.visor_c_a.GlobalUtils.PERSONAL_CONSTS_EOG;
import com.edw590.visor_c_a.GlobalUtils.UtilsApp;
import com.edw590.visor_c_a.TasksList;

import GPTComm.GPTComm;
import SpeechQueue.SpeechQueue;
import UtilsSWA.UtilsSWA;

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
	 * <p>Broadcasts a request - more info on {@link CONSTS_BC_Speech#ACTION_CALL_SPEAK}, but
	 * {@code bypass_no_sound = true}.</p>
	 *
	 * @param auto_gpt true to send the text to the GPTComm library in case the speech priority is less than or equal to
	 * {@link Speech2#PRIORITY_USER_ACTION}, {@code after_speaking} is null and the VISOR's communicator is connected
	 *
	 * @return the speech ID or an empty string if the VISOR's communicator is connected and the text was sent to the
	 * GPTComm library to be changed by LLaMA
	 */
	@NonNull
	public static String speak(@NonNull final String txt_to_speak, final int speech_priority, final int mode,
							   final boolean auto_gpt, @Nullable final Runnable after_speaking) {
		if (auto_gpt && speech_priority <= Speech2.PRIORITY_USER_ACTION && after_speaking == null &&
				UtilsSWA.isCommunicatorConnectedSERVER() && GPTComm.sendText("", false)) {
			String text = "Rephrase the following to maintain its meaning but change its wording: \"" +	txt_to_speak +
					"\". Current device: user's " + PERSONAL_CONSTS_EOG.DEVICE_TYPE + ".";
			GPTComm.sendText(text, false);

			return "";
		}

		return speakInternal(txt_to_speak, speech_priority, mode, after_speaking);
	}

	private static String speakInternal(@NonNull final String txt_to_speak, final int speech_priority, final int mode,
										@Nullable final Runnable after_speaking) {
		final Intent broadcast_intent = new Intent(CONSTS_BC_Speech.ACTION_CALL_SPEAK);
		broadcast_intent.putExtra(CONSTS_BC_Speech.EXTRA_CALL_SPEAK_1, txt_to_speak);
		broadcast_intent.putExtra(CONSTS_BC_Speech.EXTRA_CALL_SPEAK_2, mode);
		broadcast_intent.putExtra(CONSTS_BC_Speech.EXTRA_CALL_SPEAK_3, speech_priority);
		if (after_speaking != null) {
			broadcast_intent.putExtra(CONSTS_BC_Speech.EXTRA_CALL_SPEAK_4, TasksList.addTask(after_speaking));
		}
		final String speech_id = SpeechQueue.generateSpeechID();
		broadcast_intent.putExtra(CONSTS_BC_Speech.EXTRA_CALL_SPEAK_5, speech_id);

		UtilsApp.sendInternalBroadcast(broadcast_intent);

		return speech_id;
	}

	/**
	 * <p>Broadcasts a request - more info on {@link CONSTS_BC_Speech#ACTION_SKIP_SPEECH}.</p>
	 */
	public static void skipCurrentSpeech() {
		final Intent broadcast_intent = new Intent(CONSTS_BC_Speech.ACTION_SKIP_SPEECH);

		UtilsApp.sendInternalBroadcast(broadcast_intent);
	}

	/**
	 * <p>Broadcasts a request - more info on {@link CONSTS_BC_Speech#ACTION_REMOVE_SPEECH}.</p>
	 *
	 * @param speech_id read the action's documentation
	 */
	public static void removeSpeechById(@NonNull final String speech_id) {
		final Intent broadcast_intent = new Intent(CONSTS_BC_Speech.ACTION_REMOVE_SPEECH);
		broadcast_intent.putExtra(CONSTS_BC_Speech.EXTRA_REMOVE_SPEECH_1, speech_id);

		UtilsApp.sendInternalBroadcast(broadcast_intent);
	}

	/**
	 * <p>Broadcasts a request - more info on {@link CONSTS_BC_Speech#ACTION_SAY_AGAIN}.</p>
	 */
	public static void sayAgain() {
		final Intent broadcast_intent = new Intent(CONSTS_BC_Speech.ACTION_SAY_AGAIN);

		UtilsApp.sendInternalBroadcast(broadcast_intent);
	}
}
