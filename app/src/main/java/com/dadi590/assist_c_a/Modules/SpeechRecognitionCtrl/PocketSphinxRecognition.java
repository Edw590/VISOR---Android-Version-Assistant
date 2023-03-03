/*
 * Copyright 2023 DADi590
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

package com.dadi590.assist_c_a.Modules.SpeechRecognitionCtrl;

import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.GlobalInterfaces.IModuleInst;
import com.dadi590.assist_c_a.GlobalUtils.UtilsAudio;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.ModulesList;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx1.Assets;
import edu.cmu.pocketsphinx1.SpeechRecognizer;
import edu.cmu.pocketsphinx1.SpeechRecognizerSetup;

/**
 * <p>This class activates PocketSphinx's speech recognition and automatically starts Google's if the assistant's name
 * is spoken.</p>
 */
public final class PocketSphinxRecognition implements IModuleInst {

	private final int element_index = ModulesList.getElementIndex(PocketSphinxRecognition.class);
	private final HandlerThread main_handlerThread =
			new HandlerThread((String) ModulesList.getElementValue(element_index, ModulesList.ELEMENT_NAME));
	final Handler main_handler;

	@Nullable static SpeechRecognizer recognizer = null;
	//@Nullable static SpeechRecognizerSetup speechRecognizerSetup = null;

	private static final String KEYWORD_WAKEUP = "WAKEUP";

	static boolean is_listening = false;

	///////////////////////////////////////////////////////////////
	// IModuleInst stuff
	private boolean is_module_destroyed = false;
	@Override
	public boolean isFullyWorking() {
		if (is_module_destroyed) {
			return false;
		}

		return UtilsGeneral.isThreadWorking(main_handlerThread);
	}
	@Override
	public void destroy() {
		UtilsGeneral.quitHandlerThread(main_handlerThread);

		if (null != recognizer) {
			recognizer.cancel();
			recognizer.shutdown();
			recognizer = null;
		}

		is_listening = false;

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

		is_listening = false;

		prepareRecognizer();
	}

	/**
	 * <p>Prepare the recognizer instance asynchronously.</p>
	 *
	 * @return true if the recognizer is ready to work, false if an error occurred
	 */
	boolean prepareRecognizer() {
		if (null != recognizer) {
			return true;
		}

		try {
			final File assetsDir = new Assets(UtilsGeneral.getContext()).syncAssets();

			// The recognizer can be configured to perform multiple searches
			// of different kind and switch between them

			recognizer = SpeechRecognizerSetup.defaultSetup()
					// 250 MB seems a good value. Below than that, take 55 MB out might be too much. Also go for the
					// original model (the smallest of the 3) if the device reports it's running on low memory.
					//.setAcousticModel(new File(assetsDir, UtilsGeneral.isDeviceLowOnMemory() ? "en-us-ptm" :
					//		UtilsGeneral.getAvailableRAM() > 250L ? "en-us-5.2" : "en-us-ptm-5.2"))
					// EDIT: changed to the maximum quality one. Easier. No need to reload from time to time.
					.setAcousticModel(new File(assetsDir, "en-us-5.2"))
					.setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
					.setSampleRate(16000) // As per the used files (not the "8k" option, so they're the default 16 kHz)
					.setKeywordThreshold(1.0f) // Goes from 1e-45f to 1 (manual testing). Adjust for false positives.
					// The greater, the less false positives - though, more probability to fail to a true match.
					// If the Google Hotword recognizer could be put to work... Maybe it would be better (the normal
					// recognition is very good).
					// But this seems to be enough for now, I guess. 0.25f seems to be enough.
					//.setString("-logfn","/dev/null") - doesn't work on Android... It turns off logging, but "Cannot
					// redirect log output".

					// There's an exception here, but this function is only called when the microphone is available, so
					// this won't throw errors.
					.getRecognizer(main_handler);

			recognizer.addListener(recognitionListener);
			// Create keyword-activation search.
			recognizer.addKeywordSearch(KEYWORD_WAKEUP, new File(assetsDir, "visor_keywords.gram"));
		} catch (final IOException e) {
			e.printStackTrace();

			return false;
		}

		return true;
	}

	static boolean isListening() {
		return is_listening;
	}

	/**
	 * <p>Starts listening in the background, and does nothing if it had already started.</p>
	 *
	 * @return true if the recognition started or was already started, false also if the recognizer is not ready and you
	 * need to call {@link #prepareRecognizer} or if the audio source is busy
	 */
	static boolean startListening() {
		if (null == recognizer) {
			return false;
		} else if (is_listening) {
			return true;
		} else if (!UtilsAudio.isAudioSourceAvailable(recognizer.audio_source)) {
			return false;
		}

		is_listening = true;

		recognizer.startListening(KEYWORD_WAKEUP);

		return true;
	}

	/**
	 * <p>Stops listening, and does nothing if it had already stopped.</p>
	 */
	static void stopListening() {
		if (null == recognizer) {
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
		public void onPartialResult(@Nullable final Hypothesis hypothesis) {
			// null == recognizer to be sure it doesn't use other results after supposedly shutting down. This is a
			// replacement to terminating the PID, which might make the controller restart this recognition.
			// Also !is_listening to be sure it doesn't try to analyze more results after stopListening() is called.
			if (!is_listening || hypothesis == null || null == recognizer) {
				return;
			}
			final String[] hypothesis_list = hypothesis.getHypstr().split(" ");
			if (!hypothesis_list[0].contains("isor")) {
				// Substring "isor" must be in the 1st or 2nd hypothesis. Else, ignore the detection.
				if (hypothesis_list.length > 1) {
					if (!hypothesis_list[1].contains("isor")) {
						return;
					}
				} else {
					return;
				}
			}

			System.out.println("UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU");
			System.out.println(hypothesis.getHypstr());
			stopListening(); // Stop listening or else this might try to start Google recognition various times (happened).
			UtilsSpeechRecognizersBC.startGoogleRecognition();
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
