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

package com.edw590.visor_c_a.Modules.SpeechRecognitionCtrl;

import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.edw590.visor_c_a.GlobalInterfaces.IModuleInst;
import com.edw590.visor_c_a.GlobalUtils.UtilsAudio;
import com.edw590.visor_c_a.GlobalUtils.UtilsContext;
import com.edw590.visor_c_a.GlobalUtils.UtilsGeneral;
import com.edw590.visor_c_a.Modules.Speech.Speech2;
import com.edw590.visor_c_a.Modules.Speech.UtilsSpeech2BC;
import com.edw590.visor_c_a.ModulesList;
import com.edw590.visor_c_a.Registry.RegistryKeys;
import com.edw590.visor_c_a.Registry.UtilsRegistry;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx1.Assets;
import edu.cmu.pocketsphinx1.SpeechRecognizer;
import edu.cmu.pocketsphinx1.SpeechRecognizerSetup;

/**
 * <p>This class activates PocketSphinx's speech recognition and automatically starts the commands recognizer if the
 * assistant's name is spoken.</p>
 */
public final class PocketSphinxRecognition implements IModuleInst {

	private final int element_index = ModulesList.getElementIndex(PocketSphinxRecognition.class);
	private final HandlerThread main_handlerThread =
			new HandlerThread((String) ModulesList.getElementValue(element_index, ModulesList.ELEMENT_NAME));
	final Handler main_handler;

	boolean preparing = false;
	static SpeechRecognizer recognizer = null;
	//@Nullable static SpeechRecognizerSetup speechRecognizerSetup = null;

	private static final String KEYWORD_WAKEUP = "WAKEUP";
	private static final String NGRAM_SEARCH = "SEARCH";

	static boolean is_listening = false;

	///////////////////////////////////////////////////////////////
	// IModuleInst stuff
	private boolean is_module_destroyed = false;
	@Override
	public boolean isFullyWorking() {
		if (is_module_destroyed) {
			return false;
		}

		// The recognizer must be working. If it's not, at least must be being prepared. Not any? Problem happening.
		return (recognizer != null || !preparing) && UtilsGeneral.isThreadWorking(main_handlerThread);
	}
	@Override
	public void destroy() {
		UtilsGeneral.quitHandlerThread(main_handlerThread);

		shutdownRecognizer();

		is_module_destroyed = true;
	}
	@Override
	public int wrongIsSupported() {return 0;}
	/**.
	 * @return read all here {@link IModuleInst#wrongIsSupported()} */
	public static boolean isSupported() {
		return SpeechRecognitionCtrl.isSupported() && UtilsSpeechRecognizers.isPocketSphinxLibAvailable();
	}
	// IModuleInst stuff
	///////////////////////////////////////////////////////////////

	/**
	 * <p>Main class constructor.</p>
	 */
	public PocketSphinxRecognition() {
		main_handlerThread.start();
		main_handler = new Handler(main_handlerThread.getLooper());

		// To be sure the recognizer is always shut down when the module starts (it's a static variable, so can remain
		// active even if the module has an issue and crashes somewhere other than with the recognizer object). Must be
		// a complete module restart, like what happens with the other modules.
		shutdownRecognizer();

		prepareRecognizer();
	}

	/**
	 * <p>Prepare the recognizer instance.</p>
	 *
	 * @return true if the recognizer is ready to work, false if an error occurred
	 */
	boolean prepareRecognizer() {
		if (recognizer != null) {
			return true;
		}

		preparing = true;

		final File assetsDir;
		try {
			assetsDir = new Assets(UtilsContext.getContext()).syncAssets();

			// The recognizer can be configured to perform multiple searches
			// of different kind and switch between them

			recognizer = SpeechRecognizerSetup.defaultSetup()
					// 250 MB seems a good value. Below than that, take 55 MB out might be too much. Also go for the
					// original model (the smallest of the 3) if the device reports it's running on low memory.
					//.setAcousticModel(new File(assetsDir, UtilsGeneral.isDeviceLowOnMemory() ? "en-us-ptm" :
					//		UtilsGeneral.getAvailableRAM() > 250 ? "en-us-5.2" : "en-us-ptm-5.2"))
					// EDIT: changed to the maximum quality one. Easier. No need to reload from time to time.
					.setAcousticModel(new File(assetsDir, "en-us-5.2"))
					.setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
					.setSampleRate(16000) // As per the used files (not the "8k" option, so they're the default 16 kHz)
					//.setString("-logfn","/dev/null") - doesn't work on Android... Supposedly turns off logging, but a
					// "Cannot redirect log output" error appears instead.

					// There's an exception here, but this module is only started if there's permission to capture
					// audio, so this won't throw errors.
					.getRecognizer(main_handler);

			recognizer.addListener(recognitionListener);

			// Create keyword-activation search.
			recognizer.addKeywordSearch(KEYWORD_WAKEUP, new File(assetsDir, "visor_keywords.gram"));
			//recognizer.addNgramSearch(NGRAM_SEARCH, new File(assetsDir, "en-70k-0.2.lm.bin"));
		} catch (final IOException e) {
			e.printStackTrace();

			return false;
		}

		preparing = false;

		return true;
	}

	/**
	 * <p>Shuts down the recognizer instance and a call to {@link #prepareRecognizer()} will be needed again.</p>
	 */
	private static void shutdownRecognizer() {
		is_listening = false;

		if (recognizer != null) {
			recognizer.shutdown();
			recognizer = null;
		}
	}

	/**
	 * <p>Get the listening state of the recognizer.</p>
	 *
	 * @return true if listening, false if stopped
	 */
	static synchronized boolean isListening() {
		return is_listening;
	}

	static synchronized int getAudioSource() {
		if (recognizer == null) {
			return MediaRecorder.AudioSource.AUDIO_SOURCE_INVALID;
		}

		return recognizer.audio_source;
	}

	/**
	 * <p>Starts listening in the background, and does nothing if it's already listening.</p>
	 *
	 * @return true if the recognition started or was already started, false also if the recognizer is not ready and you
	 * need to call {@link #prepareRecognizer} or if the audio source is busy or if it's still stopping (if it was
	 * ordered to stop before calling this function) and you just need to call this function again
	 */
	static synchronized boolean startListening() {
		if (recognizer == null) {
			return false;
		} else if (is_listening) {
			return true;
		} else if (!UtilsAudio.isAudioSourceAvailable(recognizer.audio_source)) {
			return false;
		}

		is_listening = true;

		return recognizer.startListening(KEYWORD_WAKEUP);
	}

	/**
	 * <p>Cancels listening.</p>
	 */
	static synchronized void stopListening() {
		if (!is_listening) {
			return;
		}

		is_listening = false;

		recognizer.cancel();
	}

	static final RecognitionListener recognitionListener = new RecognitionListener() {

		@Override
		public void onBeginningOfSpeech() {
			// No need to implement.
		}

		/**
		 * We stop recognizer here to get a final result.
		 */
		@Override
		public void onEndOfSpeech() {
			// No need to implement.
		}

		/**
		 * In partial result we get quick updates about current hypothesis. In
		 * keyword spotting mode we can react here, in other modes we need to wait
		 * for final result in onResult.
		 */
		@Override
		public void onPartialResult(@NonNull final Hypothesis hypothesis) {
			// recognizer == null to be sure it doesn't use other results after supposedly shutting down. This is a
			// replacement to terminating the PID, which might make the controller restart this recognition.
			// Also !is_listening to be sure it doesn't try to analyze more results after stopListening() is called.
			if (!is_listening) {
				return;
			}

			// getBestScore() and getProb() both are always returning 0. Don't use them.
			final String hypothesis_str = hypothesis.getHypstr();
			final String[] hypothesis_list = hypothesis_str.split(" {2}"); // The string is separated by 2 spaces


			final String[][] options;
			// If it's to stop listening in the background, don't stop completely to be able to listen when to start
			// listening again. If it's not to stop, listen to the hot-words.
			if ((boolean) UtilsRegistry.getData(RegistryKeys.K_POCKETSPHINX_REQUEST_STOP, true)) {
				options = new String[][]{
						{"visor", "come", "back"},
						{"visor", "listen", "again"},
						{"visor", "recognize", "again"},
				};
			} else {

				options = new String[][]{
						{"ok", "visor"},
						{"hey", "visor"},
						{"visor", "you", "there"},
						{"visor", "come", "in"},
						{"visor", "wake", "up"},
						{"visor", "talk", "me"},
				};
			}

			boolean match = false;
			for_hypos: for (int i = 0; i < 2; ++i) { // Check on the 1st and 2nd hypothesis
				if (hypothesis_list.length <= i) {
					break;
				}
				for (final String[] option : options) {
					match = true;
					for (final String word : option) {
						if (!hypothesis_list[i].contains(word)) {
							match = false;

							break;
						}
					}

					if (match) {
						break for_hypos;
					}
				}
			}
			if (!match) {
				return;
			}

			if ((boolean) UtilsRegistry.getData(RegistryKeys.K_POCKETSPHINX_REQUEST_STOP, true)) {
				UtilsRegistry.setData(RegistryKeys.K_POCKETSPHINX_REQUEST_STOP, false, false);
				UtilsSpeech2BC.speak("Listening in the background again...", Speech2.PRIORITY_USER_ACTION,
						Speech2.MODE2_BYPASS_NO_SND, UtilsSpeech2BC.SESSION_TYPE_NONE, false, null);
			}

			stopListening(); // To ensure this is not called multiple times in a row (happened)
			UtilsSpeechRecognizersBC.startCommandsRecognition();
		}

		/**
		 * This callback is called when we stop() the recognizer.
		 */
		@Override
		public void onResult(@Nullable final Hypothesis hypothesis) {
			// Ignore the result here. Continuous speech is dealt with by onPartialResult(). This is called in the end
			// of the speech for a final result - which doesn't happen, because... continuous. Would only be called if
			// stop() was called on onPartialResult(), because else it will always be on partial results (it's
			// continuously recognizing, not finishing ever - no timeout set either).
			is_listening = false;
			startListening();
		}

		@Override
		public void onError(final Exception e) {
			e.printStackTrace();

			is_listening = false;
			startListening();
		}

		@Override
		public void onTimeout() {
			is_listening = false;
			startListening();
		}
	};
}
