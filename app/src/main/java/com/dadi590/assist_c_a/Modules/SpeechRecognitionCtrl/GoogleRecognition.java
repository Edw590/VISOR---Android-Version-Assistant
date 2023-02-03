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

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.GlobalInterfaces.IModuleInst;
import com.dadi590.assist_c_a.GlobalInterfaces.IModuleSrv;
import com.dadi590.assist_c_a.GlobalUtils.UtilsApp;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.GlobalUtils.UtilsProcesses;
import com.dadi590.assist_c_a.Modules.CmdsExecutor.UtilsCmdsExecutorBC;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <p>This class activates Google's speech recognition and broadcasts the results.</p>
 * <p>The results can be either final results or partial results, and both will be processed.</p>
 * <p>NOTE: the class is public but it's NOT to be used outside its package! It's only public for the service to be
 * instantiated (meaning if it would be put package-private now, no error would appear on the entire project).</p>
 * <br>
 * <p>Only start this service if the Google app is available, else it won't work.</p>
 */
public final class GoogleRecognition extends Service implements IModuleSrv {

	///////////////////////////////////////////////////////////////
	// IModuleSrv stuff
	@Override
	public int wrongIsSupported() {return 0;}
	/**.
	 * @return read all here {@link IModuleInst#wrongIsSupported()} */
	public static boolean isSupported() {
		// The Controller already checks if the Google app is installed, so no need to check here too.
		return SpeechRecognitionCtrl.isSupported();
	}
	// IModuleSrv stuff
	///////////////////////////////////////////////////////////////

	@Nullable private SpeechRecognizer speechRecognizer = null;

	boolean is_listening = false;

	private static final String ON_START_COMMAND_STR = "onStartCommand";
	private static final String ON_READY_FOR_SPEECH_STR = "onReadyForSpeech";
	private static final String ON_BEGINNING_OF_SPEECH_STR = "onBeginningOfSpeech";
	private static final String ON_END_OF_SPEECH_STR = "onEndOfSpeech";
	@Nullable String last_method_called = "";
	long last_method_called_when = 0L;
	static final Map<String, Long> last_methods_called_map = new LinkedHashMap<String, Long>() {
		private static final long serialVersionUID = 2268708824566655410L;
		@NonNull
		@Override public LinkedHashMap<String, Long> clone() throws AssertionError {
			throw new AssertionError();
		}

		{
			// Don't forget the sleep time of the thread is 1 second, so the waiting time here must be a multiple of
			// that (or not, but 1.5 seconds == 2 seconds in this case of 1 second sleep).
			put(ON_START_COMMAND_STR, 5_000L);
			put(ON_READY_FOR_SPEECH_STR, 5_000L);
			put(ON_BEGINNING_OF_SPEECH_STR, 5_000L);
			put(ON_END_OF_SPEECH_STR, 10_000L);
		}
	};

	@Override
	public int onStartCommand(@Nullable final Intent intent, final int flags, final int startId) {
        /*
		If the service was killed by its PID and the system restarted it, this might appear in the logs:

        Scheduling restart of crashed service com.dadi590.assist_c_a/.SpeechRecognizer.Google in 1000ms
        Scheduling restart of crashed service com.dadi590.assist_c_a/.SpeechRecognizer.PocketSphinx in 11000ms
        Start proc 1090:com.dadi590.assist_c_a:null/u0a95 for service com.dadi590.assist_c_a/.SpeechRecognizer.Google

        This below is supposed to fix that - if there's not EXTRA_TIME_START on the intent with a time that is 1 second
        or less ago relative to the current time, the service will be stopped immediately.
        */
		boolean stop_now = true;
		if (intent != null && intent.hasExtra(CONSTS_SpeechRecog.EXTRA_TIME_START)) {
			// Must have been called 1 second ago at most - else it was the system restarting it or something.
			stop_now = intent.getLongExtra(CONSTS_SpeechRecog.EXTRA_TIME_START, 0L) + 1000L < System.currentTimeMillis();
		}
		if (stop_now) {
			System.out.println("1GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG1");
			stopSelf();
			UtilsSpeechRecognizersBC.terminateSpeechRecognizers();

			return START_NOT_STICKY;
		}

		final Intent intent1 = new Intent(CONSTS_BC_SpeechRecog.ACTION_GOOGLE_RECOG_STARTED);
		UtilsApp.sendInternalBroadcast(intent1);

		// Start the recognition frozen methods checker (which means if any of the recognition methods froze and now the
		// service won't stop because it's frozen, the thread will take care of that and kill the service.)
		if (!UtilsGeneral.isThreadWorking(frozen_methods_checker)) {
			// This check here above is because onEndOfSpeech() was just called twice in a row... Wtf. Don't remove this.
			frozen_methods_checker.start();
		}

		last_method_called_when = System.currentTimeMillis();
		last_method_called = ON_START_COMMAND_STR;

		final Intent speech_recognizer_intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		speech_recognizer_intent.setPackage(UtilsSpeechRecognizers.google_app_pkg_name);
		speech_recognizer_intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
		speech_recognizer_intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		speech_recognizer_intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, UtilsGeneral.getContext().getPackageName());
		//speech_recognizer_intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
		/*// None of these 2 below work for some reason. By what I read on the Internet, it seems to be bugs from Google.
		// Keep them disabled because if they were working, code would be changed accordingly and this way nothing is
		// done because it's all commented out.
        //speech_recognizer_intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
		//		Long.valueOf(2147483647L));
        //speech_recognizer_intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS , Long.valueOf(2147483647L));
        //speech_recognizer_intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
		//		Long.valueOf(2147483647L));
		// EXTRA_MAX_RESULTS does nothing... 2 people say that on StackOverflow (last one in 2018), but might start
		// working some day, so keep it here anyways.
		speech_recognizer_intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1); // Doesn't work either.*/

		System.out.println("TTTTTTTTTTTTTT");

		final RecognitionListener speechRecognitionListener = new SpeechRecognitionListener();
		speechRecognizer = SpeechRecognizer.createSpeechRecognizer(UtilsGeneral.getContext());
		speechRecognizer.setRecognitionListener(speechRecognitionListener);
		speechRecognizer.startListening(speech_recognizer_intent);

		return START_NOT_STICKY;
	}

	/**
	 * <p>Stops and destroys the {@link SpeechRecognizer} instance if it's not stopped and destroyed already.</p>
	 */
	void stopRecognizer() {
		if (speechRecognizer != null) {
			speechRecognizer.stopListening();
			speechRecognizer.cancel();
			speechRecognizer.destroy();
			speechRecognizer = null;
		}
	}

	/**
	 * <p>{@link RecognitionListener} implementation as required by {@link SpeechRecognizer}.</p>
	 */
	final class SpeechRecognitionListener implements RecognitionListener {

		@Override
		public void onReadyForSpeech(final Bundle params) {
			System.out.println("QQQQQQQQQQQQQQQ");
			last_method_called_when = System.currentTimeMillis();
			last_method_called = ON_READY_FOR_SPEECH_STR;


			// Vibrate to indicate it's ready to listen.
			UtilsGeneral.vibrateDeviceOnce(300L);
		}

		@Override
		public void onBeginningOfSpeech() {
			System.out.println("UUUUUUUUUUUUUUUUUUUUUUUUUUUUU");
			last_method_called_when = System.currentTimeMillis();
			last_method_called = ON_BEGINNING_OF_SPEECH_STR;
			is_listening = true;
		}

		@Override
		public void onBufferReceived(final byte[] buffer) {
			// No need to implement.
		}

		@Override
		public void onEndOfSpeech() {
			System.out.println("GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG");
			last_method_called_when = System.currentTimeMillis();
			last_method_called = ON_END_OF_SPEECH_STR;
			is_listening = false; // Must be here or this will stay in loop for speech recognition in some cases...
		}

		@Override
		public void onError(final int error) {
            /*
             Copied from StackOverflow:
             // Sometime onError will get called after onResults so we keep a boolean to ignore error also
             if (mSuccess) {
                 RLog.w(this, "Already success, ignoring error");
                 return;
             }

             The above is equivalent to the below because if it stopped listening already, that's because there was
             success - when the results functions are called, they put is_listening to false, so this is ignored in that
             case. If is_listening is still true when it gets here, then there was really an error because the results
             functions were not called.

             So, adapted to this code, !is_listening is equivalent to mSuccess above.
            */
			if (!is_listening) {
				return;
			}
			is_listening = false;

			last_method_called = null;
			last_method_called_when = 0L;

			System.out.println("KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK");
			System.out.println(error);

			stopRecognizer();
			stopSelf();
			UtilsProcesses.terminatePID(UtilsProcesses.getCurrentPID());
		}

		@Override
		public void onPartialResults(final Bundle partialResults) {
			// todo
		}

		@Override
		public void onResults(final Bundle results) {
			last_method_called = null;
			last_method_called_when = 0L;

			is_listening = false;

			final List<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

			System.out.println("--------------------------");
			System.out.println(matches);
			System.out.println("--------------------------");

			UtilsCmdsExecutorBC.processTask(matches.get(0).toLowerCase(Locale.ENGLISH), false, false);

			stopRecognizer();
			stopSelf();
			UtilsProcesses.terminatePID(UtilsProcesses.getCurrentPID());
		}

		@Override
		public void onEvent(final int eventType, final Bundle params) {
			// No need to implement.
		}

		@Override
		public void onRmsChanged(final float rmsdB) {
			// No need to implement.
		}
	}


	/**
	 * <p>This thread fixes a problem which Google doesn't seem to have fixed yet. Not the best solution, but I don't
	 * have a better one.</p>
	 * <p>When the speech recognition ends on this class, it must call the always-listening recognizer - or at least
	 * say the recognition finished. So on some function, after all ends, this sends a signal. Now the problem...</p>
	 * <br>
	 * <p>onEndOfSpeech() is always called when a speech ends. When there are results, it's not the last one to be
	 * called. When there are errors, onError() is called after onEndOfSpeech(). If there is a problem and onError() is
	 * not called (which happens, it's not just a possible problem - it's a real problem), onEndOfSpeech() is the last
	 * one. Though, as there are other methods called after onEndOfSpeech() if there were no errors, I can't use it to
	 * know an error happened.</p>
	 * <br>
	 * <p>When this happens, no signal of finished recognition is sent back, nor the service is stopped - not supposed
	 * to happen. So the idea of this thread is to keep checking if onEndOfSpeech() was the last one to be called with a
	 * timer (currently 10 seconds).</p>
	 * <p>If it goes more than 10 seconds and no other function is called, signal that the recognition ended and kill
	 * the service. Why 10 seconds? Because from onEndOfSpeech() to onResults() or onPartialResults() it takes a bit,
	 * also depending on if there is Internet connection or not, and how slow it is. So 10 seconds seems a good trade-off
	 * between Internet waiting time and actual finished speech (the user will need to wait 10 seconds if the bug
	 * happens - could be worse).</p>
	 * <p>Link for a StackOverflow question of mine about this:
	 * <a href="https://stackoverflow.com/questions/59685909">link</a>.</p>
	 * <br>
	 * <p>-------------------------------------------------------</p>
	 * <p>REPLACE THIS BY ANOTHER METHOD IF THERE IS ONE!!!!!!!!!</p>
	 * <p>-------------------------------------------------------</p>
	 * <br>
	 * <p>UPDATE: now this thread checks if the recognition froze anywhere. It might have frozen on onReadyForSpeech,
	 * or on onBeginningOfSpeech (or on onEndOfSpeech...) - sometimes it happens on various functions, it seems. If it
	 * freezes, the thread terminates the process. So, if it's to replace this, think in a way of replacing the other
	 * checks too.</p>
	 * <p>The thread stops when one of the results functions are called (which will terminate the process when it
	 * finishes) - everything went fine.</p>
	 */
	Thread frozen_methods_checker = new Thread(new Runnable() {
		@Override
		public void run() {
			while (last_method_called != null) {
				System.out.println("---------");
				System.out.println(last_method_called);
				System.out.println(System.currentTimeMillis());
				System.out.println(last_method_called_when + last_methods_called_map.get(last_method_called));
				System.out.println(System.currentTimeMillis() >= last_method_called_when + last_methods_called_map.get(last_method_called));
				if (System.currentTimeMillis() >= last_method_called_when + last_methods_called_map.get(last_method_called)) {
					// If the recognizer got frozen, terminate the process.
					stopSelf();
					UtilsProcesses.terminatePID(UtilsProcesses.getCurrentPID());
				}
				try {
					Thread.sleep(1_000L);
				} catch (final InterruptedException ignored) {
					Thread.currentThread().interrupt();

					return;
				}
			}
		}
	});

	@Override
	public void onDestroy() {
		super.onDestroy();

		stopRecognizer();
		stopSelf();
		UtilsProcesses.terminatePID(UtilsProcesses.getCurrentPID());
	}


	@Override
	@Nullable
	public IBinder onBind(@Nullable final Intent intent) {
		return null;
	}
}
