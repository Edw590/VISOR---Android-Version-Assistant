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

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.edw590.visor_c_a.GlobalInterfaces.IModuleInst;
import com.edw590.visor_c_a.GlobalInterfaces.IModuleSrv;
import com.edw590.visor_c_a.GlobalUtils.AndroidSystem.UtilsAndroidPower;
import com.edw590.visor_c_a.GlobalUtils.GL_CONSTS;
import com.edw590.visor_c_a.GlobalUtils.ObjectClasses;
import com.edw590.visor_c_a.GlobalUtils.UtilsApp;
import com.edw590.visor_c_a.GlobalUtils.UtilsAudio;
import com.edw590.visor_c_a.GlobalUtils.UtilsContext;
import com.edw590.visor_c_a.GlobalUtils.UtilsGeneral;
import com.edw590.visor_c_a.GlobalUtils.UtilsNotifications;
import com.edw590.visor_c_a.GlobalUtils.UtilsProcesses;
import com.edw590.visor_c_a.GlobalUtils.UtilsShell;
import com.edw590.visor_c_a.Modules.CmdsExecutor.UtilsCmdsExecutorBC;
import com.edw590.visor_c_a.Modules.Speech.CONSTS_BC_Speech;
import com.edw590.visor_c_a.Modules.Speech.Speech2;
import com.edw590.visor_c_a.Modules.Speech.UtilsSpeech2;
import com.edw590.visor_c_a.Modules.Speech.UtilsSpeech2BC;
import com.edw590.visor_c_a.ModulesList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <p>This class activates the available commands speech recognizer and broadcasts the results.</p>
 * <p>The results can be either final results or partial results, and both will be processed (NOT YET - NOT READY).</p>
 * <p>NOTE: the class is public but it's NOT to be used outside its package! It's only public for the service to be
 * instantiated (meaning if it would be put package-private now, no error would appear on the entire project).</p>
 */
public final class CommandsRecognition extends Service implements IModuleSrv {

	private final int element_index = ModulesList.getElementIndex(this.getClass());
	private final HandlerThread main_handlerThread = new HandlerThread((String) ModulesList.getElementValue(element_index,
			ModulesList.ELEMENT_NAME));
	private Handler main_handler;

	@Nullable private SpeechRecognizer recognizer = null;

	boolean is_listening = false;
	boolean is_working = false;
	boolean partial_results = false;

	String listening_speech_id = "";
	boolean visor_spoke = false;
	boolean wait = true;

	private FrozenMethodsChecker frozen_methods_checker = null;

	//String last_processed_speech = "";
	//int partial_results_last_index = 0;
	//long partial_results_last_time = 0;
	//String process_speech_string = "";
	//String total_processed_speech = "";

	private static final int ON_START_COMMAND_STR = 0;
	private static final int ON_READY_FOR_SPEECH_STR = 1;
	private static final int ON_BEGINNING_OF_SPEECH_STR = 2;
	private static final int ON_END_OF_SPEECH_STR = 3;
	int last_method_called = -1;
	long last_method_called_when = 0;
	static final Map<Integer, Long> last_methods_called_map = new LinkedHashMap<Integer, Long>() {
		private static final long serialVersionUID = 2268708824566655410L;
		@NonNull
		@Override public LinkedHashMap<Integer, Long> clone() throws AssertionError {
			throw new AssertionError();
		}

		{
			// Don't forget the sleep time of the thread is 1 second, so the waiting time here must be a multiple of
			// that (or not, but 1.5 seconds == 2 seconds in this case of 1 second sleep).
			// PS: these times are now also including user action and method action delay, not just probable good frozen
			// timings.
			put(ON_START_COMMAND_STR, 3_000L); // 3 seconds to call onReadyForSpeech() after starting the service
			put(ON_READY_FOR_SPEECH_STR, 5_000L); // Waits for user speech to begin - give people some time (this is
			// also decided by the recognizer itself - if it sees there's no speech, it will call onEndOfSpeech() (or
			// onError(), I think, not sure anymore))
			put(ON_BEGINNING_OF_SPEECH_STR, Long.MAX_VALUE); // Speech duration - as long as the user wants. If there's
			// no one actually talking, let the recognizer decide that. I hasn't froze so far on onBeginningOfSpeech().
			put(ON_END_OF_SPEECH_STR, 3_000L); // Time since speech ending until results are gotten - Internet
			// connection doesn't take more than 3 seconds...
		}
	};

	private static final Intent speech_recognizer_intent;
	static final ObjectClasses.NotificationInfo notificationInfo;
	static {
		// Static stuff here to try to get the service to start even if milliseconds sooner, since it takes a bit to
		// start already.

		notificationInfo = new ObjectClasses.NotificationInfo(
				GL_CONSTS.CH_ID_COMMANDS_RECOG_FOREGROUND,
				"Commands recognition notification",
				"",
				NotificationCompat.PRIORITY_LOW,
				"Listening...",
				"",
				null
		);

		speech_recognizer_intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		speech_recognizer_intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
		speech_recognizer_intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		// THESE BELOW MUST BE LONGS EXPLICITLY!!! Or the recognizer will stop working who knows why.
		speech_recognizer_intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 3000L);
		speech_recognizer_intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L);
		speech_recognizer_intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L);
		speech_recognizer_intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
		//speech_recognizer_intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
	}

	///////////////////////////////////////////////////////////////
	// IModuleSrv stuff
	@Override
	public int wrongIsSupported() {return 0;}
	/**.
	 * @return read all here {@link IModuleInst#wrongIsSupported()} */
	public static boolean isSupported() {
		// The Controller already checks if a commands recognizer app is installed, so no need to check here too.
		return SpeechRecognitionCtrl.isSupported();
	}
	// IModuleSrv stuff
	///////////////////////////////////////////////////////////////

	@Override
	public void onCreate() {
		main_handlerThread.start();
		main_handler = new Handler(main_handlerThread.getLooper());

		try {
			// This must be started in another thread to change the 'wait' variable. Else it would run on the main
			// process thread, like the while loop (not very useful...).
			UtilsContext.getContext().registerReceiver(broadcastReceiver,
					new IntentFilter(CONSTS_BC_Speech.ACTION_AFTER_SPEAK_ID), null, main_handler);
		} catch (final IllegalArgumentException ignored) {
		}
	}

	@Override
	public int onStartCommand(@Nullable final Intent intent, final int flags, final int startId) {
        /*
		If the service was killed by its PID and the system restarted it, this might appear in the logs:

        Scheduling restart of crashed service com.edw590.visor_c_a/.SpeechRecognizer.Google in 1000ms
        Scheduling restart of crashed service com.edw590.visor_c_a/.SpeechRecognizer.PocketSphinx in 11000ms
        Start proc 1090:com.edw590.visor_c_a:null/u0a95 for service com.edw590.visor_c_a/.SpeechRecognizer.Google

        This below is supposed to fix that - if there's not EXTRA_TIME_START on the intent with a time that is 1 second
        or less ago relative to the current time, the service will be stopped immediately.
        */
		boolean stop_now = true;
		if (intent != null) {
			if (intent.hasExtra(CONSTS_SpeechRecog.EXTRA_TIME_START)) {
				// Must have been called 1 second ago at most - else it was the system restarting it or something.
				stop_now = intent.getLongExtra(CONSTS_SpeechRecog.EXTRA_TIME_START, 0) + 1000 < System.currentTimeMillis();
			}
			partial_results = intent.getBooleanExtra(CONSTS_SpeechRecog.EXTRA_PARTIAL_RESULTS, false);
		}
		if (stop_now) {
			stopSelf();
			UtilsProcesses.terminatePID(UtilsProcesses.getCurrentPID());

			return START_NOT_STICKY;
		}

		// DON'T WASTE TIME TRYING TO HAVE THIS AS AN INSTANTIATED MODULE
		// The SpeechRecognizer class MUST be ran from the MAIN app thread. Luckily it also works with the main thread
		// of a new process. The infinity_thread of the controller is not the main app thread... So keep it in a
		// separate process.

		boolean wait_mic = !UtilsAudio.isAudioSourceAvailable(MediaRecorder.AudioSource.MIC);

		if (is_working) {
			stopListening(true);
			wait_mic = true;
		}
		if (wait_mic) {
			try {
				// Wait 1 second if the microphone is busy, to see if it stops being.
				Thread.sleep(1000);
			} catch (final InterruptedException ignored) {
				return START_NOT_STICKY;
			}
		}

		if (UtilsAudio.isAudioSourceAvailable(MediaRecorder.AudioSource.MIC)) {
			startListening();
		} else {
			// Else, if the microphone doesn't stop being busy, means it's in use elsewhere (recording, in a call, who
			// knows), so warn about it and don't do anything.
			final String speak = "Resources are busy";
			UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, 0, UtilsSpeech2BC.GPT_DUMB, false, null);

			stopListening(true);
			stopSelf();
			UtilsProcesses.terminatePID(UtilsProcesses.getCurrentPID());
		}

		return START_NOT_STICKY;
	}

	boolean startListening() {
		last_method_called = -1;
		last_method_called_when = 0;

		// This must be done before starting the thread.
		last_method_called = ON_START_COMMAND_STR;

		if (recognizer == null) {
			recognizer = SpeechRecognizer.createSpeechRecognizer(UtilsContext.getContext());
			recognizer.setRecognitionListener(new SpeechRecognitionListener());
		}

		// Start the recognition frozen methods checker (which means if any of the recognition methods froze and now the
		// service won't stop because it's frozen, the thread will take care of that and kill the service.)
		if (frozen_methods_checker != null) {
			// In case the previous one wasn't interrupted.
			frozen_methods_checker.interrupt();
		}
		frozen_methods_checker = new FrozenMethodsChecker();
		frozen_methods_checker.start();

		// After starting the thread - without the thread, this can stay working forever without being working, and if
		// a problem occurred while starting it, the controller will restart t
		UtilsApp.sendInternalBroadcast(new Intent(CONSTS_BC_SpeechRecog.ACTION_CMDS_RECOG_STARTING));

		wait = true;
		// Don't notify about the speech if there was no sound - there's already a notification.
		listening_speech_id = UtilsSpeech2BC.speak("Listening...", Speech2.PRIORITY_USER_ACTION, Speech2.MODE1_NO_NOTIF,
				UtilsSpeech2BC.GPT_NONE, false, null);
		visor_spoke = UtilsSpeech2.mightSpeak();

		// Right before calling startListening() but also before the while true just in case it would get stuck.
		last_method_called_when = System.currentTimeMillis();

		// Don't begin recognizing if VISOR didn't finish speaking (else he will recognize his own speech and end
		// the recognition if there's nothing said right after it).
		// Also no problem if this gets stuck because the Speech module is restarting or something - the checker
		// thread will terminate the recognizer in that case.
		while (wait) {
			try {
				Thread.sleep(100);
			} catch (final InterruptedException ignored) {
				return false;
			}
		}

		// todo Instead of this, have VISOR detect if he's on speakers or headphones. If on speakers, mute the
		//  microphone with AudioManager.setMicrophoneMute() until he stops speaking.

		is_working = true;

		recognizer.startListening(speech_recognizer_intent);

		UtilsContext.getNotificationManager().notify(GL_CONSTS.NOTIF_ID_COMMANDS_RECOG_FOREGROUND,
				UtilsNotifications.getNotification(notificationInfo).setOngoing(true).build());

		return true;
	}

	final Runnable stop_listening = new Runnable() {
		@Override
		public void run() {
			last_method_called = -1;
			last_method_called_when = 0;
			if (recognizer != null) {
				recognizer.cancel();
				recognizer.destroy();
				recognizer = null;
			}
			frozen_methods_checker.interrupt();
			UtilsNotifications.cancelNotification(GL_CONSTS.NOTIF_ID_COMMANDS_RECOG_FOREGROUND);
			UtilsApp.sendInternalBroadcast(new Intent(CONSTS_BC_SpeechRecog.ACTION_CMDS_RECOG_STOPPED));
			is_listening = false;
			is_working = false;
		}
	};

	void stopListening(final boolean main_thread) {
		if (main_thread) {
			stop_listening.run();
		} else {
			new Handler(Looper.getMainLooper()).post(stop_listening);
		}
	}

	/**
	 * <p>{@link RecognitionListener} implementation as required by {@link SpeechRecognizer}.</p>
	 */
	final class SpeechRecognitionListener implements RecognitionListener {

		@Override
		public void onReadyForSpeech(final Bundle params) {
			last_method_called_when = System.currentTimeMillis();
			last_method_called = ON_READY_FOR_SPEECH_STR;

			// Indicate it's ready to listen. Also disable the battery saver temporarily (if it's enabled) to be able to
			// vibrate if VISOR could not speak (must warn it's ready to listen somehow).
			boolean battery_saver_was_enabled = false;
			if (!visor_spoke && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				battery_saver_was_enabled = UtilsAndroidPower.getBatterySaverEnabled();
				if (battery_saver_was_enabled) {
					if (UtilsShell.noErr(UtilsAndroidPower.setBatterySaverEnabled(false))) {
						try {
							// 100ms seemed a good value on BV9500. 50ms still was enough. So I chose 100 for slower devices.
							Thread.sleep(100);
						} catch (final InterruptedException ignored) {
							return;
						}
					} else {
						battery_saver_was_enabled = false;
					}
				}
			}
			final long time_vibrate = 300;
			boolean vibrated = UtilsGeneral.vibrateDeviceOnce(time_vibrate);
			if (battery_saver_was_enabled &&
					Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // Linter is wtf and needs this here
				if (vibrated) {
					try {
						Thread.sleep(time_vibrate);
					} catch (final InterruptedException ignored) {
						return;
					}
				}
				UtilsAndroidPower.setBatterySaverEnabled(true);
			}
			//final PowerManager powerManager = (PowerManager) UtilsGeneral.getSystemService(Context.POWER_SERVICE); API 16-
			//final DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE); API 17+
			//if (powerManager != null && powerManager.screen.displaUtilsRoot.isRootAvailable()) {
			//	UtilsAndroidPower.turnScreenOnTemp();
			//	UtilsAndroidPower.turnScreenOff();
			//}
		}

		@Override
		public void onBeginningOfSpeech() {
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

			stopListening(true);
			stopSelf();
			UtilsProcesses.terminatePID(UtilsProcesses.getCurrentPID());
		}

		@Override
		public void onPartialResults(final Bundle partialResults) {
			//if (partial_results) {
			//	ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
			//	final String match = matches.get(0).toLowerCase(Locale.ENGLISH);

			//	if (!match.equals(last_processed_speech) && match.length() - 1 > partial_results_last_index) {
			//		process_speech_string = match.substring(partial_results_last_index);
			//		partial_results_last_time = System.currentTimeMillis();
			//	}

			//	//Atualiza a Google App e o reconhecimento de voz e mete isto a funcionar decentemente.

			//	// TO DO ISTO FALA PELAS COLUNAS COM OS PHONES LIGADOS SE A TAREFA FOR CHAMADA POR AQUI, MAS SE FOR PELO onResults JÁ É SÓ PELOS PHONES!!!!!!!!!!!!!!!
			//	//  PS: Pela MainAct também vai pelos 2 sítios. Só pelo onResults é que não vai, ao que parece.
			//}
			// todo
			// todo
			// todo
			// todo
			// todo
			// todo
			// todo
			// todo
			// todo
			// todo
			// todo
			// todo Take care of this!!!! This seems to need the other thread (that has been missing since a few years)
		}

		@Override
		public void onResults(final Bundle results) {
			stopListening(true);

			final List<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
			if (matches == null || matches.isEmpty()) {
				return;
			}

			final String first_match = matches.get(0).toLowerCase(Locale.ENGLISH);

			System.out.println("--------------------------");
			System.out.println(matches);
			System.out.println("--------------------------");

			UtilsCmdsExecutorBC.processTask(first_match, false, false, false);



			//if (!matches.isEmpty()) {
			//	if (first_match.length() - 1 > partial_results_last_index && !first_match.equals(total_processed_speech.toString())) {
			//		process_speech_string = first_match.substring(partial_results_last_index);
			//		System.out.println("A------------------------A");
			//		System.out.println(process_speech_string);
			//		System.out.println("A------------------------A");
			//		if (!process_speech_string.equals(last_processed_speech)) {
			//			UtilsCmdsExecutorBC.processTask(first_match.substring(partial_results_last_index), false, false);
			//		}
			//	}
			//}

			stopListening(true);
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
	 * <p>This thread fixes a problem which Google doesn't seem to have fixed yet on its recognizer implementation. Not
	 * the best solution, but I don't have a better one.</p>
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
	final class FrozenMethodsChecker extends Thread {
		@Override
		public void run() {
			while (last_method_called != -1) {
				if (last_method_called_when == 0) {
					continue;
				}

				if (last_methods_called_map.get(last_method_called) != Long.MAX_VALUE &&
						System.currentTimeMillis() >= last_method_called_when + last_methods_called_map.get(last_method_called)) {
					// If the recognizer got frozen, stop listening.
					// Also don't check the time if the method has no wait time (MAX_VALUE).
					stopListening(false);
					stopSelf();
					UtilsProcesses.terminatePID(UtilsProcesses.getCurrentPID());
				}
				try {
					Thread.sleep(1_000);
				} catch (final InterruptedException ignored) {
					return;
				}
			}
		}
	}

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(@Nullable final Context context, @Nullable final Intent intent) {
			if (intent == null || intent.getAction() == null) {
				return;
			}

			System.out.println("PPPPPPPPPPPPPPPPPP-CommandsRecognition - " + intent.getAction());

			switch (intent.getAction()) {
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////

				case (CONSTS_BC_Speech.ACTION_AFTER_SPEAK_ID): {
					if (listening_speech_id.equals(intent.getStringExtra(CONSTS_BC_Speech.EXTRA_AFTER_SPEAK_ID_1))) {
						wait = false;
					}

					break;
				}

				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
			}
		}
	};

	@Nullable
	@Override
	public IBinder onBind(@Nullable final Intent intent) {
		return null;
	}
}
