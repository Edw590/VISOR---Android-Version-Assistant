package com.dadi590.assist_c_a.Modules.Speech;

import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;

import java.util.ArrayList;
import java.util.List;

import static com.dadi590.assist_c_a.Modules.Speech.CONSTS.LENGTH_UTTERANCE_ID;
import static com.dadi590.assist_c_a.Modules.Speech.CONSTS.UTTERANCE_ID_PREFIX;
import static com.dadi590.assist_c_a.Modules.Speech.CONSTS.UTTERANCE_ID_PREFIX_UNIQUE_CHAR;
import static com.dadi590.assist_c_a.Modules.Speech.Speech2.PRIORITY_LOW;

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
	 * @param priority same as in {@link Speech2#speak(String, int, int, Runnable)}
	 *
	 * @return the prefix to add to the beginning of the utterance ID
	 */
	private static String getUtteranceIdPrefix(final int priority) {
		// The generation of a random string MUST NOT have the following characters on it: "_" and "-", as they're used
		// as prefix to differentiate different priorities of speeches.
		final String utterance_id_prefix = UTTERANCE_ID_PREFIX.replace("X", String.valueOf(priority));

		return utterance_id_prefix;
	}

	/**
	 * <p>Get the priority of a speech through its utterance ID.</p>
	 *
	 * @param utteranceId the utterance ID of the speech to be analyzed
	 *
	 * @return one of the {@code priority} parameters of {@link Speech2#speak(String, int, int, Runnable)}.
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
	 * @param priority one of the {@code priority} parameters of {@link Speech2#speak(String, int, int, Runnable)}
	 *
	 * @return the generated utterance ID
	 */
	static String generateUtteranceId(final int priority) {
		final String utterance_id_prefix = getUtteranceIdPrefix(priority);
		return utterance_id_prefix + UtilsGeneral.generateRandomString(LENGTH_UTTERANCE_ID - utterance_id_prefix.length());
	}

	/**
	 * <p>Gets the speech ID from the array of the given priority through its speech string.</p>
	 * <br>
	 * <p>This method will return the ID of the <em>first</em> occurrence only in case there are multiple speeches with
	 * the same string.</p>
	 *
	 * @param priority same as in {@link Speech2#speak(String, int, int, Runnable)}
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
			if (array_speech_objs.get(i).speech.equals(speech)) {
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
}
