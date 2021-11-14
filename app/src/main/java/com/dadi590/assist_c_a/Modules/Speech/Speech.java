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

import static com.dadi590.assist_c_a.Modules.Speech.CONSTS.EMERGENCY_ID_PREFIX;
import static com.dadi590.assist_c_a.Modules.Speech.CONSTS.LENGTH_UTTERANCE_ID;
import static com.dadi590.assist_c_a.Modules.Speech.CONSTS.WAS_SAYING_PREFIX_1;

import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.Executor;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * <p>The speech module of the assistant, based on {@link TextToSpeech}'s internal queue.</p>
 *
 * @deprecated use the v2 API: {@link Speech2}. I've left this one here (Speech v1) in case it's needed again.
 */
@Deprecated
public class Speech {



	// todo Too many warnings. Most of them are deprecation warnings. Remove @Deprecated and @deprecated from the classes
	//  Speech and TtsUtteranceProgressListener temporarily to see the real warnings.



	TextToSpeech tts;

	private final ArrayList<String> utterance_ids = new ArrayList<>(10);
	private final List<Runnable> runnables = new ArrayList<>(10);
	String current_speech_id = "";
	private String last_thing_said = "";
	private final ArrayList<String> speeches = new ArrayList<>(25);
	private final int speech_audio_stream;

	//////////////////////////////////////
	// Getters
	/**.
	 * @return the variable {@link #last_thing_said}
	 */
	@NonNull
	public final String getLastThingSaid() {
		return last_thing_said;
	}
	/**.
	 * @return the variable {@link #speech_audio_stream}
	 */
	public final int getSpeechAudioStream() {
		return speech_audio_stream;
	}

	//////////////////////////////////////

	//////////////////////////////////////
	// Static methods

	/**
	 * <p>Checks if an utterance ID is from an emergency speech.</p>
	 *
	 * @param utteranceId the utterance ID of the speech to be analyzed
	 *
	 * @return true if it's an emergency speech, false otherwise
	 */
	private static boolean isEmergencySpeech(@NonNull final String utteranceId) {
		return utteranceId.startsWith(EMERGENCY_ID_PREFIX);
	}

	//////////////////////////////////////

	/**
	 * Main class constructor.
	 */
	public Speech() {
		speech_audio_stream = AudioManager.STREAM_NOTIFICATION;

		tts = new TextToSpeech(UtilsGeneral.getContext(), new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(final int status) {
				System.out.println("GGGGGGGGGGGGGGGGGGGGGGGG");
				if (status == TextToSpeech.SUCCESS) {
					tts.setOnUtteranceProgressListener(new TtsUtteranceProgressListener());

					//AfterTtsReady.afterTtsReady();//context);
				} else {
                    // If he can't talk, won't be too much useful... So exit with an error to indicate something is very
                    // wrong and must be fixed as soon as possible.

					speak("", NO_ADDITIONAL_COMMANDS, false, null);
					// todo Send an email about this and put a notification on the phone!!!
				}
			}
		});
	}

	public static final int NO_ADDITIONAL_COMMANDS = 0;
	public static final int EXECUTOR_SOMETHING_SAID = 1;
	public static final int SKIP_CURRENT_SPEECH = 2;
	public static final int SPEECH_ABORTED_RINGER_MODE = 3234;
	/**
	 * <p>Speaks the given text in a normal speech or in an emergency speech. It's also possible to execute something
	 * after finish speaking through {@code after_speaking_code}, and also give additional commands through
	 * {@code additional_command}.</p>
	 * <br>
	 * <p>If AudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL, exactly nothing will be done except
	 * run {@code after_speaking_code}. So the speech will be aborted, but the given code will still be executed.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #NO_ADDITIONAL_COMMANDS} --> for {@code additional_command}: execute no additional commands</p>
	 * <p>- {@link #EXECUTOR_SOMETHING_SAID} --> for {@code additional_command}: set Executor's
	 * {@link Executor#something_said} variable to true.</p>
	 * <p>- {@link #SKIP_CURRENT_SPEECH} --> for {@code additional_command}: skip the currently speaking speech - in
	 * this case, the {@code txt_to_speak} and {@code emergency_speech} parameters will be ignored</p>
	 * <p>- {@link #SPEECH_ABORTED_RINGER_MODE} --> for the returning value: in case the speech was aborted because
	 * the getRingerMode() != RINGER_MODE_NORMAL.</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param txt_to_speak what to speak
	 * @param additional_command one of the constants
	 * @param emergency_speech true if it's an emergency speech, false if it's a normal speech
	 * @param after_speaking_code code to execute after finishing speaking
	 *
	 * @return return value of {@link TextToSpeech#speak(CharSequence, int, Bundle, String)} //
	 * {@link TextToSpeech#speak(String, int, HashMap)}, or {@link #SPEECH_ABORTED_RINGER_MODE} (its value, 3234, is
	 * never returned by any of the mentioned functions)
	 */
	public final int speak(@NonNull final String txt_to_speak, final int additional_command,
						   final boolean emergency_speech, @Nullable final Runnable after_speaking_code) {
		final AudioManager audioManager = null;//MainSrv.getAudioManager();
		if (audioManager != null && audioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			if (after_speaking_code != null) {
				after_speaking_code.run();
			}

			return SPEECH_ABORTED_RINGER_MODE;
		}

		// todo Put the speeches in a sound file, so you can skip specific ones and give different priorities aside from
		// normal and emergency (incoming call has different priority than stolen device, for example). Also, pause the
		// speech ("wait, wait. [thinking on something...] ok, carry on") --> though, it means I can't disable the
		// recognition while he's speaking...
		// Done. New implementation of the Speech API. No need for files. Can't pause though, but he'll just say it again.

		// todo See why the phone speaks through headphones AND speakers when the speech is requested by
		// onPartialResults() or through MainAct!!!! --> WTF

		// todo Make a way of getting him not to listen what he himself is saying... Or he'll hear himself and process
		// that, which is stupid. For example by cancelling the recognition when he's speaking or, or removing what he
		// said from the string of what he listened or something (2nd one preferable).
		// When this is implemented, don't forget to check if he's speaking on the speakers or on headphones. If
		// it's on headphones, no need to cancel the recognition. If it's on speakers, no sure. If the volume is
		// high enough, he wouldn't hear us anyways. If we lower the volume, he could hear us.

		final String to_speak;
		if (additional_command == SKIP_CURRENT_SPEECH) {
			to_speak = "";
		} else {
			to_speak = txt_to_speak;
		}

		final int speech_mode;
		if (emergency_speech || additional_command == SKIP_CURRENT_SPEECH) {
			// If it's to skip the current speech, put the priority as emergency.
			speech_mode = TextToSpeech.QUEUE_FLUSH;
		} else {
			speech_mode = TextToSpeech.QUEUE_ADD;
		}

		// The utteranceIDs (their indexes in the array) are used by me to identify the corresponding Runnable and speech.

		final String currently_speaking_speech_ID;
		if (speech_mode == TextToSpeech.QUEUE_FLUSH) {
			final int length = LENGTH_UTTERANCE_ID - EMERGENCY_ID_PREFIX.length();
			currently_speaking_speech_ID = EMERGENCY_ID_PREFIX + UtilsGeneral.generateRandomString(length);
		} else {
			currently_speaking_speech_ID = UtilsGeneral.generateRandomString(LENGTH_UTTERANCE_ID);
		}
		utterance_ids.add(currently_speaking_speech_ID);
		speeches.add(to_speak);
		runnables.add(after_speaking_code);

		if (additional_command != NO_ADDITIONAL_COMMANDS) {
			if (additional_command == EXECUTOR_SOMETHING_SAID) {
				// todo MainSrv.getExecutor().something_done_true();
			}
		}

		final TtsParamsObj tts_params = new TtsParamsObj();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			tts_params.bundle.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, speech_audio_stream);
			tts_params.bundle.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0F);
		} else {
			tts_params.hashmap.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(speech_audio_stream));
			tts_params.hashmap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utterance_ids.get(utterance_ids.size()-1));
			tts_params.hashmap.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, String.valueOf(1));
		}

		// todo This doesn't seem to be needed on Blackview BV9500 (Android Oreo 8.1)...
		// If this runs on it, it will say stuff twice or something.
		// Done --> Speech API v2
		if (emergency_speech && additional_command != SKIP_CURRENT_SPEECH) {
			// This fixes the problem in the NOTE in the end of the function.

			// Also, this shall only happen if the skip command has not been given. If it has been given, let the
			// problem happen - will skip the current speech and continue the remaining ones.

			// The below if statement is to only do this if the currently speaking speech is an emergency one.
			if (!current_speech_id.startsWith(EMERGENCY_ID_PREFIX)) {
				// The 2 conditions on the below if statement that seem(?) to do the same are to be sure he's speaking
				// when this occurs - might finish when it gets here, or isSpeaking() could return faster or slower than
				// the currently_speaking_speech_ID variable is updated or vice-versa.
				if (tts.isSpeaking() && !current_speech_id.isEmpty()) {
					for (int i = 0, size = utterance_ids.size(); i < size; i++) {
						if (utterance_ids.get(i).equals(current_speech_id)) {
							utterance_ids.add(i + 1, utterance_ids.get(i));
							speeches.add(i + 1, WAS_SAYING_PREFIX_1 + speeches.get(i));
							runnables.add(i + 1, runnables.get(i));
							break;
						}
					}
				}
			}
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			return tts.speak(to_speak, speech_mode, tts_params.bundle, utterance_ids.get(utterance_ids.size()-1));
		} else {
			return tts.speak(to_speak, speech_mode, tts_params.hashmap);
		}

		// NOTE (no longer happening, but remains here as a reference): Now there's only the problem in which the speech
		// being spoken when the emergency one was requested, won't be spoken again.
		// When the emergency one comes, it first stops the current one, and by stopping, it's removed from its internal
		// list of to-speak-speeches. No idea how to tell it it's not to remove it from the list...
		// PS: this only happens when the speaking speech is a normal speech and the next one is an emergency one.
		// If an emergency one is stopped by an emergency one, both will be spoken.
	}

	/**
	 * <p>The {@link UtteranceProgressListener} to be used for the speech.</p>
	 *
	 * @deprecated same as in {@link Speech} - I'm just putting here too because it's a package-private class
	 */
	@Deprecated
	class TtsUtteranceProgressListener extends UtteranceProgressListener {

		@Override
		public final void onStart(final String utteranceId) {
			current_speech_id = utteranceId;
		}

		@Override
		public final void onDone(final String utteranceId) {
			current_speech_id = "";
			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
			System.out.println(utteranceId);
			//Utils_general.imprimir_ArrayList(utteranceIDs_fala);
			//Utils_general.imprimir_ArrayList(falas_fala);
			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
			speechTreatment(utteranceId);
		}

		// Up to API 20
		@Override
		public final void onError(final String utteranceId) {
			current_speech_id = "";
			speechTreatment(utteranceId);
		}

		// As of API 21
		@Override
		public final void onError(final String utteranceId, final int errorCode) {
			current_speech_id = "";
			speechTreatment(utteranceId);
		}
	}

	/**
	 * <p>Corrects problems found with the {@link TextToSpeech} API.</p>
	 * <br>
	 * <p>This method is to be called from inside {@link TtsUtteranceProgressListener} only.</p>
	 *
	 * @param utteranceId same as in {@link UtteranceProgressListener}
	 */
	final void speechTreatment(final String utteranceId) {
		boolean reset_speeches = true;
		if (isEmergencySpeech(utterance_ids.get(0))) {
			// This is in case it's requested an emergency speech and then a normal one.
			// When it would get here, there were 2 speeches on the list, and one of them had not been spoken yet
			// (the non-emergency one). When it would get to the resetting of the speeches, it would reset the one that
			// had not been spoken --> there would exist a copy of the speech (and with the same ID). This fixes that.
			reset_speeches = false;
		}

		for (int i = 0, size = utterance_ids.size(); i < size; i++) {
			if (utterance_ids.get(i).equals(utteranceId)) {
				System.out.println(speeches.get(i));
				if (runnables.get(i) != null) {
					runnables.get(i).run();
				}
				last_thing_said = speeches.get(i);
				speeches.remove(i);
				utterance_ids.remove(i);
				runnables.remove(i);
				break;
			}
		}

		final TtsParamsObj tts_params = new TtsParamsObj();
		if (isEmergencySpeech(utteranceId) && reset_speeches) {
			for (int i = 0, size = utterance_ids.size(); i < size; i++) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					tts_params.bundle.clear();
					tts_params.bundle.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, speech_audio_stream);
					tts_params.bundle.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0F);
					tts.speak(speeches.get(i), TextToSpeech.QUEUE_ADD, tts_params.bundle,
							utterance_ids.get(i));
				} else {
					tts_params.hashmap.clear();
					tts_params.hashmap.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(speech_audio_stream));
					tts_params.hashmap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utterance_ids.get(i));
					tts_params.hashmap.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, String.valueOf(1));
					tts.speak(speeches.get(i), TextToSpeech.QUEUE_ADD, tts_params.hashmap);
				}
			}
		}
	}
}
