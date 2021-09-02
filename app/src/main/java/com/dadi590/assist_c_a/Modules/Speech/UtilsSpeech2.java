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

import static com.dadi590.assist_c_a.Modules.Speech.CONSTS.LENGTH_UTTERANCE_ID;
import static com.dadi590.assist_c_a.Modules.Speech.CONSTS.UTTERANCE_ID_PREFIX;
import static com.dadi590.assist_c_a.Modules.Speech.CONSTS.UTTERANCE_ID_PREFIX_UNIQUE_CHAR;
import static com.dadi590.assist_c_a.Modules.Speech.Speech2.PRIORITY_LOW;

import android.content.Intent;

import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.GlobalUtils.GL_BC_CONSTS;
import com.dadi590.assist_c_a.GlobalUtils.UtilsApp;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Speech API v2 related utilities.</p>
 */
final class UtilsSpeech2 {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsSpeech2() {
	}

	/**
	 * <p>Generates a prefix for the speech utterance ID based on the speech priority.</p>
	 *
	 * @param priority same as in {@link Speech2#speak(String, int, int, Integer)}
	 *
	 * @return the prefix to add to the beginning of the utterance ID
	 */
	private static String getUtteranceIdPrefix(final int priority) {
		return UTTERANCE_ID_PREFIX.replace("X", String.valueOf(priority));
	}

	/**
	 * <p>Get the priority of a speech through its utterance ID.</p>
	 *
	 * @param utteranceId the utterance ID of the speech to be analyzed
	 *
	 * @return one of the {@code priority} parameters of {@link Speech2#speak(String, int, int, Integer)}.
	 * In case an empty string is given, the lowest priority will be returned.
	 */
	static int getSpeechPriority(final String utteranceId) {
		if (utteranceId.isEmpty()) {
			// If no speech was being spoken, we'll supposed it has the minimum priority.
			return PRIORITY_LOW;
		}
		final int index_hyphen = utteranceId.indexOf((int) UTTERANCE_ID_PREFIX_UNIQUE_CHAR);

		return Integer.parseInt(utteranceId.substring(index_hyphen-1, index_hyphen));
	}

	/**
	 * <p>Generates a random utterance ID with the prefix for the given priority.</p>
	 *
	 * @param priority one of the {@code priority} parameters of {@link Speech2#speak(String, int, int, Integer)}
	 *
	 * @return the generated utterance ID
	 */
	static String generateUtteranceId(final int priority) {
		// The generation of a random string must NOT have the following characters on it: "_" and "-", as they're used
		// as prefix to differentiate different priorities of speeches.

		final String utterance_id_prefix = getUtteranceIdPrefix(priority);
		return utterance_id_prefix + UtilsGeneral.generateRandomString(LENGTH_UTTERANCE_ID - utterance_id_prefix.length());
	}

	/**
	 * <p>Gets the speech ID from the array of the given priority through its speech string.</p>
	 * <br>
	 * <p>This method will return the ID of the <em>first</em> occurrence only in case there are multiple speeches with
	 * the same string.</p>
	 *
	 * @param priority same as in {@link Speech2#speak(String, int, int, Integer)}
	 * @param speech the speech string to search the array for
	 * @param arrays_speech_objs the {@link Speech2#arrays_speech_objs} instance
	 *
	 * @return the ID of the found speech
	 */
	@Nullable
	static String internalGetSpeechIdBySpeech(final int priority, final String speech,
											  final ArrayList<? extends ArrayList<SpeechObj>> arrays_speech_objs) {
		// This variable below is for, hopefully, faster access than being getting the array inside the loop every time.
		final List<SpeechObj> array_speech_objs = arrays_speech_objs.get(priority);

		final int array_speech_objs_size = array_speech_objs.size();
		for (int i = 0; i < array_speech_objs_size; i++) {
			if (array_speech_objs.get(i).txt_to_speak.equals(speech)) {
				return array_speech_objs.get(i).utterance_id;
			}
		}

		return null;
	}

	/**
	 * <p>Gets the indexes of a speech on the lists through its utterance ID.</p>
	 *
	 * @param utterance_id the utterance ID of the speech to retrieve
	 * @param arrays_speech_objs the {@link Speech2#arrays_speech_objs} instance
	 *
	 * @return an array with the 2 indices of the speech on the {@link Speech2#arrays_speech_objs} or null in case the
	 * ID was not found
	 */
	@Nullable
	static int[] getSpeechIndexesFromId(final String utterance_id,
										final ArrayList<? extends ArrayList<SpeechObj>> arrays_speech_objs) {
		final int priority = UtilsSpeech2.getSpeechPriority(utterance_id);

		// This variable below is for, hopefully, faster access than being getting the array inside the loop every time.
		final List<SpeechObj> correct_sub_array = arrays_speech_objs.get(priority);

		final int correct_sub_array_size = correct_sub_array.size();
		for (int i = 0; i < correct_sub_array_size; i++) {
			if (correct_sub_array.get(i).utterance_id.equals(utterance_id)) {
				return new int[]{priority, i};
			}
		}

		return null;
	}

	/**
	 * <p>Gets the {@link SpeechObj} associated with the given utterance ID.</p>
	 *
	 * @param utterance_id the utterance ID of the wanted {@link SpeechObj} instance
	 * @param arrays_speech_objs the {@link Speech2#arrays_speech_objs} instance
	 *
	 * @return the {@link SpeechObj} instance for the given utterance ID, or null in case the utterance ID is not on the
	 * lists
	 */
	@Nullable
	static SpeechObj getSpeechObjFromId(final String utterance_id,
										final ArrayList<? extends ArrayList<SpeechObj>> arrays_speech_objs) {
		final int[] indexes = getSpeechIndexesFromId(utterance_id, arrays_speech_objs);
		if (indexes == null) {
			return null;
		}

		return arrays_speech_objs.get(indexes[0]).get(indexes[1]);
	}

	/**
	 * <p>Broadcast the {@code after_speaking_code} through {@link GL_BC_CONSTS#ACTION_SPEECH2_AFTER_SPEAK_CODE}.</p>
	 *
	 * @param code the code to broadcast
	 */
	static void broadcastAfterSpeakCode(final int code) {
		final Intent intent = new Intent(GL_BC_CONSTS.ACTION_SPEECH2_AFTER_SPEAK_CODE);
		intent.putExtra(GL_BC_CONSTS.EXTRA_SPEECH2_AFTER_SPEAK_CODE, code);
		UtilsApp.sendInternalBroadcast(intent);
	}
}
