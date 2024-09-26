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

package com.edw590.visor_c_a.Modules.SpeechRecognitionCtrl;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.speech.SpeechRecognizer;

import androidx.annotation.Nullable;

import com.edw590.visor_c_a.GlobalInterfaces.IModuleInst;
import com.edw590.visor_c_a.GlobalUtils.UtilsCheckHardwareFeatures;
import com.edw590.visor_c_a.GlobalUtils.UtilsContext;
import com.edw590.visor_c_a.GlobalUtils.UtilsGeneral;
import com.edw590.visor_c_a.GlobalUtils.UtilsPermsAuths;
import com.edw590.visor_c_a.GlobalUtils.UtilsServices;
import com.edw590.visor_c_a.Registry.ValuesRegistry;
import com.edw590.visor_c_a.Registry.UtilsRegistry;
import com.edw590.visor_c_a.ModulesList;
import com.edw590.visor_c_a.TasksList;

/**
 * <p>This is the module which controls the assistant's speech recognition.</p>
 * <p>It may control one or more speech recognizers, like PocketSphinx and/or the commands speech recognition.</p>
 */
public final class SpeechRecognitionCtrl implements IModuleInst {

	static final Class<?> NO_RECOGNIZER = null;
	static final Class<?> POCKETSPHINX_RECOGNIZER = PocketSphinxRecognition.class;
	static final Class<?> COMMANDS_RECOGNIZER = CommandsRecognition.class;

	// In case it's to stop the recognition. For example an audio will be recorded or the microphone is being needed
	// elsewhere.
	boolean stop_speech_recognition = false;

	@Nullable Class<?> current_recognizer = NO_RECOGNIZER;

	long cmds_recog_requested_when = 0;
	boolean cmds_recog_is_listening = false;

	private static final long DEFAULT_WAIT_TIME = 5_000;
	long wait_time = DEFAULT_WAIT_TIME;

	private final int element_index = ModulesList.getElementIndex(this.getClass());
	private final HandlerThread main_handlerThread = new HandlerThread((String) ModulesList.getElementValue(element_index,
			ModulesList.ELEMENT_NAME));
	private final Handler main_handler;

	///////////////////////////////////////////////////////////////
	// IModuleInst stuff
	private boolean is_module_destroyed = false;
	@Override
	public boolean isFullyWorking() {
		if (is_module_destroyed) {
			return false;
		}

		return UtilsGeneral.isThreadWorking(main_handlerThread) && UtilsGeneral.isThreadWorking(infinity_thread);
	}
	@Override
	public void destroy() {
		infinity_thread.interrupt();
		try {
			UtilsContext.getContext().unregisterReceiver(broadcastReceiver);
		} catch (final IllegalArgumentException ignored) {
		}
		UtilsGeneral.quitHandlerThread(main_handlerThread);

		is_module_destroyed = true;
	}
	@Override
	public int wrongIsSupported() {return 0;}
	/**.
	 * @return read all here {@link IModuleInst#wrongIsSupported()} */
	public static boolean isSupported() {
		final String[] min_required_permissions = {
				Manifest.permission.RECORD_AUDIO,
		};

		final boolean commands_recog_available = UtilsSpeechRecognizers.isCmdsRecogAppAvailable();

		// Update the Values Storage
		UtilsRegistry.setData(ValuesRegistry.K_COMMANDS_RECOG_AVAILABLE, commands_recog_available, false);

		return SpeechRecognizer.isRecognitionAvailable(UtilsContext.getContext()) &&
				UtilsPermsAuths.checkSelfPermissions(min_required_permissions) &&
				UtilsCheckHardwareFeatures.isMicrophoneSupported() && commands_recog_available;
	}
	// IModuleInst stuff
	///////////////////////////////////////////////////////////////

	/**
	 * <p>Main class constructor.</p>
	 */
	public SpeechRecognitionCtrl() {
		main_handlerThread.start();
		main_handler = new Handler(main_handlerThread.getLooper());

		try {
			final IntentFilter intentFilter = new IntentFilter();

			intentFilter.addAction(CONSTS_BC_SpeechRecog.ACTION_CMDS_RECOG_STARTING);
			intentFilter.addAction(CONSTS_BC_SpeechRecog.ACTION_CMDS_RECOG_STOPPED);

			intentFilter.addAction(CONSTS_BC_SpeechRecog.ACTION_START_CMDS_RECOG);
			intentFilter.addAction(CONSTS_BC_SpeechRecog.ACTION_START_POCKET_SPHINX);
			intentFilter.addAction(CONSTS_BC_SpeechRecog.ACTION_STOP_RECOGNITION);

			UtilsContext.getContext().registerReceiver(broadcastReceiver, intentFilter, null, main_handler);
		} catch (final IllegalArgumentException ignored) {
		}

		infinity_thread.start();
	}

	private final Thread infinity_thread = new Thread(new Runnable() {
		@Override
		public void run() {
			final int cmds_recog_module_index = ModulesList.getElementIndex(COMMANDS_RECOGNIZER);
			while (true) {
				UtilsServices.startService(CommandsRecognition.class, null, false, true);
				//final PocketSphinxRecognition instance = (PocketSphinxRecognition) ModulesList.getElementValue(
				//		pocket_sphinx_module_index, ModulesList.ELEMENT_INSTANCE);
				//if (instance != null) {
				//	instance.prepareRecognizer();
				//}

				if (!stop_speech_recognition) {
					if (current_recognizer == COMMANDS_RECOGNIZER) {
						if (!ModulesList.isElementFullyWorking(cmds_recog_module_index)) {
							cmds_recog_requested_when = 0;
							current_recognizer = NO_RECOGNIZER;
							wait_time = DEFAULT_WAIT_TIME;
						}
					} else if (current_recognizer == POCKETSPHINX_RECOGNIZER) {
						if (!PocketSphinxRecognition.isListening()) {
							cmds_recog_requested_when = 0;
							current_recognizer = NO_RECOGNIZER;
							wait_time = DEFAULT_WAIT_TIME;
						}
					}

					if (current_recognizer == NO_RECOGNIZER) {
						if (cmds_recog_requested_when == 0) {
							if (!(boolean) UtilsRegistry.getData(ValuesRegistry.K_POCKETSPHINX_REQUEST_STOP, true) ||
									PocketSphinxRecognition.getAudioSource() == MediaRecorder.AudioSource.HOTWORD) {
								// todo Falta no executor esta linha acima, ligar o PocketSphinx se for HOTWORD e tiver sido pedido para parar.
								// todo 		Além disso "visor come back" para o reiniciar (só disponível para HOTWORD) e falta
								// todo 		metê-lo a ouvir "visor" duas vezes em 7 segundos e metê-lo a dizer que foi pedido para parar
								// todo 		de ouvir.
								// todo 		3 vezes se calhar é melhor. senão para que parou de ouvir?
								// todo 		não, esquece. 2 vezes. 7 segundos max, ou 8. ou 3 em 12 segundos, mas 3 vezes é
								// todo 		demais.
								// If it's to stop recognizing in the background, only truly stop if the audio source is
								// MIC (give the mic to other apps). If it's HOTWORD, keep listening but only for
								// "visor come back".
								if (cmds_recog_is_listening) {
									UtilsSpeechRecognizers.stopCommandsRecognizer();
								}
								if (UtilsSpeechRecognizers.startPocketSphinxRecognition()) {
									current_recognizer = POCKETSPHINX_RECOGNIZER;
									wait_time = DEFAULT_WAIT_TIME;
								}
							}
						} else if (System.currentTimeMillis() > cmds_recog_requested_when + 2000) {
							// If the cmds recognizer was requested but could not be started for some reason (probably
							// some error starting the service, who knows), keep trying to start it.
							UtilsSpeechRecognizers.stopPocketSphinxRecognition();
							UtilsSpeechRecognizers.startCommandsRecognition();
						}
					}
				}

				try {
					Thread.sleep(wait_time);
				} catch (final InterruptedException ignored) {
					return;
				}
			}
		}
	});

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(@Nullable final Context context, @Nullable final Intent intent) {
			if (intent == null || intent.getAction() == null) {
				return;
			}

			System.out.println("PPPPPPPPPPPPPPPPPP-SpeechRecognitionCtrl - " + intent.getAction());

			switch (intent.getAction()) {
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////

				case (CONSTS_BC_SpeechRecog.ACTION_CMDS_RECOG_STARTING): {
					cmds_recog_is_listening = true;
					current_recognizer = COMMANDS_RECOGNIZER;
					wait_time = 500;

					break;
				}
				case (CONSTS_BC_SpeechRecog.ACTION_CMDS_RECOG_STOPPED): {
					cmds_recog_requested_when = 0;
					cmds_recog_is_listening = false;
					current_recognizer = NO_RECOGNIZER;
					wait_time = DEFAULT_WAIT_TIME;

					break;
				}


				case (CONSTS_BC_SpeechRecog.ACTION_START_CMDS_RECOG): {
					UtilsSpeechRecognizers.stopPocketSphinxRecognition();
					UtilsSpeechRecognizers.startCommandsRecognition();

					cmds_recog_requested_when = System.currentTimeMillis();
					stop_speech_recognition = false;
					wait_time = DEFAULT_WAIT_TIME;

					break;
				}
				case (CONSTS_BC_SpeechRecog.ACTION_START_POCKET_SPHINX): {
					if (cmds_recog_is_listening) {
						UtilsSpeechRecognizers.stopCommandsRecognizer();
					}
					if (!(boolean) UtilsRegistry.getData(ValuesRegistry.K_POCKETSPHINX_REQUEST_STOP, true)) {
						// Still stop. Just don't restart PocketSphinx.
						UtilsSpeechRecognizers.startPocketSphinxRecognition();
					}

					cmds_recog_requested_when = 0;
					stop_speech_recognition = false;
					wait_time = DEFAULT_WAIT_TIME;

					break;
				}
				case (CONSTS_BC_SpeechRecog.ACTION_STOP_RECOGNITION): {
					cmds_recog_requested_when = 0;
					stop_speech_recognition = true;
					wait_time = DEFAULT_WAIT_TIME;
					cmds_recog_is_listening = false;

					UtilsSpeechRecognizers.stopSpeechRecognizers();

					new Thread(TasksList.removeTask(
							intent.getIntExtra(CONSTS_BC_SpeechRecog.EXTRA_STOP_RECOGNITION_1, -1)).runnable).start();

					break;
				}
				case (CONSTS_BC_SpeechRecog.ACTION_TERMINATE_RECOGNIZERS): {
					//stop_speech_recognition = false; - this doesn't change with this call
					cmds_recog_requested_when = 0;
					wait_time = DEFAULT_WAIT_TIME;
					cmds_recog_is_listening = false;

					UtilsSpeechRecognizers.stopSpeechRecognizers();

					break;
				}
			}

			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
		}
	};
}
