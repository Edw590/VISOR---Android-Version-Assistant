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

import android.content.Context;
import android.media.AudioManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.edw590.visor_c_a.GlobalUtils.UtilsContext;
import com.edw590.visor_c_a.Modules.SpeechRecognitionCtrl.UtilsSpeechRecognizersBC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import UtilsSWA.UtilsSWA;

/**
 * <p>Speech API v2 related utilities.</p>
 */
public final class UtilsSpeech2 {

	/** Simple runnable that calls {@link UtilsSpeechRecognizersBC#startCommandsRecognition()}. */
	public static final Runnable CALL_COMMANDS_RECOG = new Runnable() {
		@Override
		public void run() {
			UtilsSpeechRecognizersBC.startCommandsRecognition();
		}
	};

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsSpeech2() {
	}

	/**
	 * <p>Use this right after calling {@link UtilsSpeech2BC#speak(String, int, int, boolean, Runnable)} to know if VISOR
	 * <em>might</em> actually speak (means the ringer mode is in NORMAL - that's what is checked here).</p>
	 * <p>"Might" because if he'll speak or not is checked right before the speech takes place, this is just an
	 * approximation (and that's why it should be called right after the broadcast function - to give a bit of time).</p>
	 *
	 * @return true if he might speak, false if he might not
	 */
	public static boolean mightSpeak() {
		final AudioManager audioManager = (AudioManager) UtilsContext.getSystemService(Context.AUDIO_SERVICE);
		if (audioManager == null) {
			return false;
		}

		return audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL;
	}

	/**
	 * <p>Generates a prefix for the speech utterance ID based on the speech priority.</p>
	 *
	 * @param priority same as in {@link Speech2#speak(String, int, int)}
	 *
	 * @return the prefix to add to the beginning of the utterance ID
	 */
	@NonNull
	private static String getUtteranceIdPrefix(final int priority) {
		return CONSTS_Speech.UTTERANCE_ID_PREFIX.replace("X", String.valueOf(priority));
	}

	/**
	 * <p>Get the priority of a speech through its utterance ID.</p>
	 *
	 * @param utteranceId the utterance ID of the speech to be analyzed
	 *
	 * @return one of the {@code priority} parameters of {@link Speech2#speak(String, int, int)}.
	 * In case an empty string is given, the lowest priority will be returned.
	 */
	static int getSpeechPriority(@NonNull final String utteranceId) {
		if (utteranceId.isEmpty()) {
			// If no speech was being spoken, we'll supposed it has the minimum priority.
			return Speech2.PRIORITY_LOW;
		}
		final int index_hyphen = utteranceId.indexOf((int) CONSTS_Speech.UTTERANCE_ID_PREFIX_UNIQUE_CHAR);

		return Integer.parseInt(utteranceId.substring(index_hyphen-1, index_hyphen));
	}

	/**
	 * <p>Generates a random utterance ID with the prefix for the given priority, unique within
	 * {@link Speech2#arrays_speech_objs}.</p>
	 *
	 * @param priority one of the {@code priority} parameters of {@link Speech2#speak(String, int, int)}
	 *
	 * @return the generated utterance ID
	 */
	@NonNull
	static synchronized String generateUtteranceId(final int priority) {
		// The generation of a random string must NOT have the following characters on it: "_" and "-", as they're used
		// as prefix to differentiate different priorities of speeches.

		final String utterance_id_prefix = getUtteranceIdPrefix(priority);
		String utterance_id_to_use = "";

		// A while true to create random utterance IDs and ensure they have not been already used on the lists
		while (true) {
			utterance_id_to_use = utterance_id_prefix +
					UtilsSWA.randStringGENERAL(CONSTS_Speech.LENGTH_UTTERANCE_ID - utterance_id_prefix.length());

			boolean match_found = false;
			for (final Iterable<SpeechObj> speech_objs : Speech2.arrays_speech_objs) {
				for (final SpeechObj speech_obj : speech_objs) {
					if (speech_obj.utterance_id.equals(utterance_id_to_use)) {
						match_found = true;
						break;
					}
				}
				if (match_found) {
					break;
				}
			}
			if (!match_found) {
				break;
			}
		}

		return utterance_id_to_use;
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
	static int[] getSpeechIndexesFromId(@NonNull final String utterance_id,
										@NonNull final ArrayList<? extends ArrayList<SpeechObj>> arrays_speech_objs) {
		final int priority = UtilsSpeech2.getSpeechPriority(utterance_id);

		// This variable below is for, hopefully, faster access than being getting the array inside the loop every time.
		final List<SpeechObj> correct_sub_array = arrays_speech_objs.get(priority);

		for (int i = 0, size = correct_sub_array.size(); i < size; ++i) {
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
	static SpeechObj getSpeechObjFromId(@NonNull final String utterance_id,
										@NonNull final ArrayList<? extends ArrayList<SpeechObj>> arrays_speech_objs) {
		final int[] indexes = getSpeechIndexesFromId(utterance_id, arrays_speech_objs);
		if (indexes == null) {
			return null;
		}

		return arrays_speech_objs.get(indexes[0]).get(indexes[1]);
	}

	/**
	 * <p>Get the ArrayLists of speech objects ready for use.</p>
	 *
	 * @param arrays_speech_objs the {@link Speech2#arrays_speech_objs} instance
	 */
	static void readyArrayLists(@NonNull final Collection<? super ArrayList<SpeechObj>> arrays_speech_objs) {
		// Fill each ArrayList with a number of ArrayLists, which correspond to the number of existing priority values.
		// 50 as the initial value because I don't think more than 50 speeches will be on a list... (wtf).
		for (int i = 0; i < CONSTS_Speech.NUMBER_OF_PRIORITIES; ++i) {
			arrays_speech_objs.add(new ArrayList<>(50));
			// Before you think in declaring a new variable to be the array to always add in the for loop, think again
			// and realize it's a reference... So it will be the same array all over the indexes of the main one (copy
			// of the copy of the copy...) - what I mean is below:
			// final ArrayList<SpeechObj> new_array = new ArrayList<>(50);
			// for (...)
			//     arrays_speech_objs.add(new array);
		}
	}

	/**
	 * <p>Removes a speech from the queues through its utterance ID.</p>
	 *
	 * @param utteranceId the utterance ID of the speech
	 * @param arrays_speech_objs the {@link Speech2#arrays_speech_objs} instance
	 *
	 * @return the successfully removed {@link SpeechObj}, or null if the utterance ID was not on the lists
	 */
	@Nullable
	static SpeechObj removeSpeechById(@NonNull final String utteranceId,
									  @NonNull final ArrayList<? extends ArrayList<SpeechObj>> arrays_speech_objs) {
		final int[] indexes = UtilsSpeech2.getSpeechIndexesFromId(utteranceId, arrays_speech_objs);
		if (indexes == null) {
			// Should not happen when called from inside the Speech class if the Speech is well implemented
			return null;
		}

		return arrays_speech_objs.get(indexes[0]).remove(indexes[1]);
	}

	/**
	 * <p>Gets the speech ID from the array of the given priority through its speech string.</p>
	 * <br>
	 * <p>This method will return the ID of the <em>first</em> occurrence only in case there are multiple speeches with
	 * the same string.</p>
	 *
	 * @param priority same as in {@link Speech2#speak(String, int, int)}
	 * @param speech the speech string to search the array for
	 * @param arrays_speech_objs the {@link Speech2#arrays_speech_objs} instance
	 *
	 * @return the ID of the found speech
	 */
	@Nullable
	static String internalGetSpeechIdBySpeech(final int priority, @NonNull final String speech,
											  @NonNull final ArrayList<? extends ArrayList<SpeechObj>> arrays_speech_objs) {
		// This variable below is for, hopefully, faster access than being getting the array inside the loop every time.
		final List<SpeechObj> array_speech_objs = arrays_speech_objs.get(priority);

		for (int i = 0, size = array_speech_objs.size(); i < size; ++i) {
			if (array_speech_objs.get(i).txt_to_speak.equals(speech)) {
				return array_speech_objs.get(i).utterance_id;
			}
		}

		return null;
	}

	static final int UNKNOWN_PRIORITY = -1;
	/**
	 * <p>Gets the speech ID through its speech string.</p>
	 * <br>
	 * <p>This method will return the ID of the <em>first</em> occurrence only, in case there are multiple speeches with
	 * the same string.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #UNKNOWN_PRIORITY} --> for {@code speech_priority}: in case the speech priority is not known -- then the
	 * method will take more time to search the arrays for it, since it will search all arrays until it finds an
	 * occurrence.</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param speech the speech string to search the arrays for
	 * @param speech_priority same as in {@link Speech2#speak(String, int, int)} or the constant. If it's the
	 *                           constant, then the parameter {@code low_to_high} will be completely ignored
	 * @param low_to_high true to search from lower priority arrays to higher ones, false to do the opposite
	 * @param arrays_speech_objs the {@link Speech2#arrays_speech_objs} instance
	 *
	 * @return the ID of the found speech
	 */
	@Nullable
	static String getSpeechIdBySpeech(@NonNull final String speech, final int speech_priority, final boolean low_to_high,
									 @NonNull final ArrayList<? extends ArrayList<SpeechObj>> arrays_speech_objs) {
		if (speech_priority == UNKNOWN_PRIORITY) {
			if (low_to_high) {
				for (int priority = 0, size = arrays_speech_objs.size(); priority < size; ++priority) {
					final String ret = internalGetSpeechIdBySpeech(priority, speech, arrays_speech_objs);
					if (ret != null) {
						return ret;
					}
				}
			} else {
				for (int priority = arrays_speech_objs.size() - 1; priority >= 0; priority--) {
					final String ret = internalGetSpeechIdBySpeech(priority, speech, arrays_speech_objs);
					if (ret != null) {
						return ret;
					}
				}
			}
		} else {
			return internalGetSpeechIdBySpeech(speech_priority, speech, arrays_speech_objs);
		}

		return null;
	}

	/**
	 * <p>Rephrases a speech that was interrupted, to have a prefix, so when it's spoken again, one knows it was the
	 * speech that was trying to be said before the interruption.</p>
	 *
	 * @param utterance_id the utterance ID of the speech to be rephrased
	 * @param arrays_speech_objs the {@link Speech2#arrays_speech_objs} instance
	 */
	static void reSayRephraseSpeech(@NonNull final String utterance_id,
									 @NonNull final ArrayList<? extends ArrayList<SpeechObj>> arrays_speech_objs) {
		final int priority = UtilsSpeech2.getSpeechPriority(utterance_id);

		final List<SpeechObj> correct_sub_array = arrays_speech_objs.get(priority);
		final int size_loop = correct_sub_array.size();
		for (int i = 0; i < size_loop; ++i) {
			if (correct_sub_array.get(i).utterance_id.equals(utterance_id)) {
				final SpeechObj speechObj = correct_sub_array.get(i);

				if (speechObj.txt_to_speak.startsWith(CONSTS_Speech.WAS_SAYING_PREFIX_1)) {
					final String new_speech = speechObj.txt_to_speak.substring(CONSTS_Speech.WAS_SAYING_PREFIX_1.length());
					arrays_speech_objs.get(priority).get(i).txt_to_speak = CONSTS_Speech.WAS_SAYING_PREFIX_2 + new_speech;
				} else if (speechObj.txt_to_speak.startsWith(CONSTS_Speech.WAS_SAYING_PREFIX_2)) {
					final String new_speech = speechObj.txt_to_speak.substring(CONSTS_Speech.WAS_SAYING_PREFIX_2.length());
					arrays_speech_objs.get(priority).get(i).txt_to_speak = CONSTS_Speech.WAS_SAYING_PREFIX_3 + new_speech;
				} else if (!speechObj.txt_to_speak.startsWith(CONSTS_Speech.WAS_SAYING_PREFIX_3)) {
					arrays_speech_objs.get(priority).get(i).txt_to_speak = CONSTS_Speech.WAS_SAYING_PREFIX_1 +
							speechObj.txt_to_speak;
				}
				break;
			}
		}
	}
}
