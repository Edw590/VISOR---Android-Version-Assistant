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

package com.dadi590.assist_c_a.Modules.SpeechRecognition;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.GlobalInterfaces.IModuleInst;
import com.dadi590.assist_c_a.GlobalUtils.UtilsCheckHardwareFeatures;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.GlobalUtils.UtilsPermsAuths;
import com.dadi590.assist_c_a.GlobalUtils.UtilsServices;
import com.dadi590.assist_c_a.ValuesStorage.CONSTS_ValueStorage;
import com.dadi590.assist_c_a.ValuesStorage.ValuesStorage;

/**
 * <p>This is the module which controls the assistant's speech recognition.</p>
 * <p>It may control one or more speech recognizers, like PocketSphinx and/or Google Speech Recognition.</p>
 */
public class SpeechRecognitionCtrl implements IModuleInst {

	static final Class<?> NO_RECOGNIZER = null;
	static final Class<?> POCKETSPHINX_RECOGNIZER = PocketSphinxRecognition.class;
	static final Class<?> GOOGLE_RECOGNIZER = GoogleRecognition.class;

	// In case it's to stop the recognition. For example an audio will be recorded or the microphone is being needed
	// elsewhere.
	boolean stop_speech_recognition = false;

	@Nullable Class<?> current_recognizer = NO_RECOGNIZER;

	private static final long default_wait_time = 5_000L;
	long wait_time = default_wait_time;

	///////////////////////////////////////////////////////////////
	// IModuleInst stuff
	private boolean is_module_destroyed = false;
	@Override
	public final boolean isFullyWorking() {
		if (is_module_destroyed) {
			return false;
		}

		return infinity_thread.isAlive();
	}
	@Override
	public final void destroy() {
		infinity_thread.interrupt();
		try {
			UtilsGeneral.getContext().unregisterReceiver(broadcastReceiver);
		} catch (final IllegalArgumentException ignored) {
		}
		is_module_destroyed = true;
	}
	@Override
	public final int wrongIsSupported() {return 0;}
	/**.
	 * @return read all here {@link IModuleInst#wrongIsSupported()} */
	public static boolean isSupported() {
		final String[][] min_required_permissions = {{
				Manifest.permission.RECORD_AUDIO,
		}};

		final boolean google_recog_available = UtilsSpeechRecognizers.isGoogleAppEnabled();

		// Update the Values Storage
		ValuesStorage.updateValue(CONSTS_ValueStorage.google_recog_available, String.valueOf(google_recog_available));

		return UtilsPermsAuths.checkSelfPermissions(min_required_permissions)[0] &&
				UtilsCheckHardwareFeatures.isMicrophoneSupported() && google_recog_available;
	}
	// IModuleInst stuff
	///////////////////////////////////////////////////////////////

	/**
	 * <p>Main class constructor.</p>
	 */
	public SpeechRecognitionCtrl() {
		try {
			final IntentFilter intentFilter = new IntentFilter();

			intentFilter.addAction(CONSTS_BC_SpeechRecog.ACTION_GOOGLE_RECOG_STARTED);
			intentFilter.addAction(CONSTS_BC_SpeechRecog.ACTION_POCKETSPHINX_RECOG_STARTED);

			intentFilter.addAction(CONSTS_BC_SpeechRecog.ACTION_START_GOOGLE);
			intentFilter.addAction(CONSTS_BC_SpeechRecog.ACTION_START_POCKET_SPHINX);
			intentFilter.addAction(CONSTS_BC_SpeechRecog.ACTION_STOP_RECOGNITION);

			UtilsGeneral.getContext().registerReceiver(broadcastReceiver, intentFilter);
		} catch (final IllegalArgumentException ignored) {
		}

		infinity_thread.start();
	}

	private final Thread infinity_thread = new Thread(new Runnable() {
		@Override
		public void run() {
			while (true) {
				// Also keep updating the ValuesStorage values in case there is some issue and the services don't get to
				// update the value to false when they stop running (for example process killed by the system).

				if (!stop_speech_recognition) {
					if (GOOGLE_RECOGNIZER == current_recognizer) {
						final boolean is_running = UtilsServices.isServiceRunning(GoogleRecognition.class);

						// Update the Values Storage
						ValuesStorage.updateValue(CONSTS_ValueStorage.google_recog_available, String.valueOf(is_running));

						if (!is_running) {
							current_recognizer = NO_RECOGNIZER;
							wait_time = default_wait_time;
						}
					} else if (POCKETSPHINX_RECOGNIZER == current_recognizer) {
						final boolean is_running = UtilsServices.isServiceRunning(PocketSphinxRecognition.class);

						// Update the Values Storage
						ValuesStorage.updateValue(CONSTS_ValueStorage.google_recog_available, String.valueOf(is_running));

						if (!is_running) {
							current_recognizer = NO_RECOGNIZER;
							wait_time = default_wait_time;
						}
					}

					if (NO_RECOGNIZER == current_recognizer) {
						// Update the Values Storage
						ValuesStorage.updateValue(CONSTS_ValueStorage.pocketsphinx_recog_available, String.valueOf(false));
						ValuesStorage.updateValue(CONSTS_ValueStorage.google_recog_available, String.valueOf(false));

						UtilsSpeechRecognizers.terminateSpeechRecognizers();
						UtilsSpeechRecognizers.startPocketSphinxRecognition();
					}
				}

				try {
					Thread.sleep(wait_time);
				} catch (final InterruptedException ignored) {
					Thread.currentThread().interrupt();

					return;
				}
			}
		}
	});

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(@Nullable final Context context, @Nullable final Intent intent) {
			if (null == intent || null == intent.getAction()) {
				return;
			}

			System.out.println("PPPPPPPPPPPPPPPPPP-SpeechRecognitionCtrl - " + intent.getAction());

			switch (intent.getAction()) {
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////

				case CONSTS_BC_SpeechRecog.ACTION_GOOGLE_RECOG_STARTED: {
					current_recognizer = GOOGLE_RECOGNIZER;
					wait_time = 500L;

					break;
				}
				case CONSTS_BC_SpeechRecog.ACTION_POCKETSPHINX_RECOG_STARTED: {
					current_recognizer = POCKETSPHINX_RECOGNIZER;
					wait_time = default_wait_time;

					break;
				}


				case CONSTS_BC_SpeechRecog.ACTION_START_GOOGLE: {
					UtilsSpeechRecognizers.terminateSpeechRecognizers();
					UtilsSpeechRecognizers.startGoogleRecognition();

					stop_speech_recognition = false;
					wait_time = default_wait_time;

					break;
				}
				case CONSTS_BC_SpeechRecog.ACTION_START_POCKET_SPHINX: {
					UtilsSpeechRecognizers.terminateSpeechRecognizers();
					UtilsSpeechRecognizers.startPocketSphinxRecognition();

					stop_speech_recognition = false;
					wait_time = default_wait_time;

					break;
				}
				case CONSTS_BC_SpeechRecog.ACTION_STOP_RECOGNITION: {
					stop_speech_recognition = true;
					wait_time = default_wait_time;

					UtilsSpeechRecognizers.terminateSpeechRecognizers();

					break;
				}
				case CONSTS_BC_SpeechRecog.ACTION_TERMINATE_RECOGNIZERS: {
					//stop_speech_recognition = false; - this doesn't change with this call
					wait_time = default_wait_time;

					UtilsSpeechRecognizers.terminateSpeechRecognizers();

					break;
				}
			}

			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
		}
	};
}
