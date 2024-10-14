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

package com.edw590.visor_c_a.Modules.AudioRecorder;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.Nullable;

import com.edw590.visor_c_a.GlobalInterfaces.IModuleInst;
import com.edw590.visor_c_a.GlobalUtils.UtilsCheckHardwareFeatures;
import com.edw590.visor_c_a.GlobalUtils.UtilsContext;
import com.edw590.visor_c_a.GlobalUtils.UtilsGeneral;
import com.edw590.visor_c_a.GlobalUtils.UtilsMedia;
import com.edw590.visor_c_a.GlobalUtils.UtilsPermsAuths;
import com.edw590.visor_c_a.Registry.UtilsRegistry;
import com.edw590.visor_c_a.Registry.ValuesRegistry;
import com.edw590.visor_c_a.Modules.Speech.Speech2;
import com.edw590.visor_c_a.Modules.Speech.UtilsSpeech2BC;
import com.edw590.visor_c_a.Modules.SpeechRecognitionCtrl.UtilsSpeechRecognizersBC;
import com.edw590.visor_c_a.ModulesList;

import java.io.File;
import java.io.IOException;

/**
 * <p>The audio recorder module of the assistant.</p>
 */
public final class AudioRecorder implements IModuleInst {

	private final int element_index = ModulesList.getElementIndex(getClass());
	private final HandlerThread main_handlerThread = new HandlerThread((String) ModulesList.getElementValue(element_index,
			ModulesList.ELEMENT_NAME));
	private final Handler main_handler;

	@Nullable private MediaRecorder recorder = null;

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
		try {
			UtilsContext.getContext().unregisterReceiver(broadcastReceiver);
		} catch (final IllegalArgumentException ignored) {
		}
		UtilsGeneral.quitHandlerThread(main_handlerThread);

		stopRecording();

		is_module_destroyed = true;
	}
	@Override
	public int wrongIsSupported() {return 0;}
	/**.
	 * @return read all here {@link IModuleInst#wrongIsSupported()} */
	public static boolean isSupported() {
		final String[] min_required_permissions = {
				Manifest.permission.RECORD_AUDIO,
				Manifest.permission.WRITE_EXTERNAL_STORAGE,
		};
		return UtilsPermsAuths.checkSelfPermissions(min_required_permissions)
				&& UtilsCheckHardwareFeatures.isMicrophoneSupported();
	}
	// IModuleInst stuff
	///////////////////////////////////////////////////////////////

	/**
	 * <p>Main class constructor.</p>
	 */
	public AudioRecorder() {
		main_handlerThread.start();
		main_handler = new Handler(main_handlerThread.getLooper());

		// Update the Values Storage
		UtilsRegistry.setData(ValuesRegistry.K_IS_RECORDING_AUDIO_INTERNALLY, false, false);

		try {
			final IntentFilter intentFilter = new IntentFilter();

			intentFilter.addAction(CONSTS_BC_AudioRec.ACTION_RECORD_AUDIO);

			UtilsContext.getContext().registerReceiver(broadcastReceiver, new IntentFilter(intentFilter), null,
					main_handler);
		} catch (final IllegalArgumentException ignored) {
		}
	}

	/**
	 * <p>Method to call instead of calling directly {@link #startRecording(int)}.</p>
	 *
	 * @param start true to start recording, false to stop recording
	 * @param audio_source same as in {@link #startRecording(int)}, or as a standard, -1 if {@code start} is false (this
	 * parameter will be ignored if it's to stop recording).
	 * @param restart_pocketsphinx in case it's to stop the recorder, true to restart pocketsphinx, false not to restart
	 *                             pocketsphinx it. Outside that situation this parameter is ignored.
	 */
	void recordAudio(final boolean start, final int audio_source, final boolean restart_pocketsphinx) {
		final boolean is_recording = (boolean) UtilsRegistry.getData(ValuesRegistry.K_IS_RECORDING_AUDIO_INTERNALLY, true);

		if (start) {
			if (is_recording) {
				final String speak = "Already on it sir.";
				UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_USER_ACTION, 0, true, null);
			} else {
				final Runnable runnable = new Runnable() {
					@Override
					public void run() {
						if (startRecording(audio_source) != NO_ERRORS) {
							// In case of an error, restart the hotword recognition again.
							UtilsSpeechRecognizersBC.startPocketSphinxRecognition();
						}
					}
				};
				final String speak = "Starting now, sir.";
				UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_USER_ACTION, 0, true, runnable);
			}
		} else {
			if (is_recording) {
				stopRecording();
				final String speak = "Stopped, sir.";
				UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_USER_ACTION, 0, true, null);

				if (restart_pocketsphinx) {
					UtilsSpeechRecognizersBC.startPocketSphinxRecognition();
				}
			} else {
				final String speak = "Already stopped, sir.";
				UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_USER_ACTION, 0, true, null);
			}
		}
	}

	public static final int NO_ERRORS = 0;
	public static final int ERR_CREATE_FILE = 1;
	public static final int ERR_PREP_RECORDING = 2;
	public static final int ERR_PERM_CAP_AUDIO = 3;
	public static final int ERR_PERM_CAP_AUDIO_OR_MIC_BUSY = 4;
	/**
	 * <p>Starts an audio recording, recording from the given audio source, to default media output files, given by
	 * {@link UtilsMedia#getOutputMediaFile(int)}</p>.
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #NO_ERRORS} --> for the returning value: when the recording successfully started</p>
	 * <p>- {@link #ERR_CREATE_FILE} --> for the returning value: when there was an error creating the recording file</p>
	 * <p>- {@link #ERR_PREP_RECORDING} --> for the returning value: when there was an error preparing the recording</p>
	 * <p>- {@link #ERR_PERM_CAP_AUDIO} --> for the returning value: when the permission to record from the given audio
	 * source was not granted</p>
	 * <p>- {@link #ERR_PERM_CAP_AUDIO_OR_MIC_BUSY} --> for the returning value: when the permission to record from the
	 * given audio source was not granted, or if the microphone is already in use and the recording could not start</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param audioSource one of {@link MediaRecorder#setAudioSource(int)}'s parameters
	 * @return one of the constants
	 */
	int startRecording(final int audioSource) {
		recorder = new MediaRecorder();
		try {
			recorder.setAudioSource(audioSource);
		} catch (final RuntimeException e) {
			e.printStackTrace();
			stopRecording();

			final String speak = "Error 1 sir.";
			UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_USER_ACTION, 0, true, null);

			return ERR_PERM_CAP_AUDIO;
		}

		// Do NOT change the encoder and format settings. I've put those because they are compatible with all devices
		// that the app supports, and the sound is still very good.
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		final File file = UtilsMedia.getOutputMediaFile(UtilsMedia.AUDIO);
		final File folder = file.getParentFile();
		assert folder != null; // Won't be null, there's always a parent folder
		if (!folder.exists() && !file.getParentFile().mkdirs()) {
			stopRecording();

			final String speak = "Error 2 sir.";
			UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_USER_ACTION, 0, true, null);

			return ERR_CREATE_FILE;
		}
		recorder.setOutputFile(file.getAbsolutePath());
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);

		try {
			recorder.prepare();
		} catch (final IOException e) {
			e.printStackTrace();
			stopRecording();

			final String speak = "Error 3 sir.";
			UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_USER_ACTION, 0, true, null);
			file.delete();

			return ERR_PREP_RECORDING;
		}

		try {
			recorder.start();
		} catch (final IllegalStateException e) {
			e.printStackTrace();
			stopRecording();

			final String speak = "Error 4 sir.";
			UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_USER_ACTION, 0, true, null);
			file.delete();

			return ERR_PERM_CAP_AUDIO_OR_MIC_BUSY;
			/*int permission_status = UtilsPermissions.checkSelfPermission(Manifest.permission.CAPTURE_AUDIO_OUTPUT);
            if (permission_status == EPackageManager.PERMISSION_GRANTED) {
                return UtilsGeneral.FONTE_INDISPONIVEL;
            } else {
                return SEM_PERMISSAO_CAPTURE_AUDIO_OUTPUT;
            }*/
		}

		// Update the Values Storage
		UtilsRegistry.setData(ValuesRegistry.K_IS_RECORDING_AUDIO_INTERNALLY, true, false);

		return NO_ERRORS;
	}

	/**
	 * <p>Stops an ongoing audio recording.</p>
	 */
	private void stopRecording() {
		if (recorder != null) {
			try {
				recorder.stop();
			} catch (final IllegalStateException ignored) {
			}
			recorder.release();
			recorder = null;
		}

		// Update the Values Storage
		UtilsRegistry.setData(ValuesRegistry.K_IS_RECORDING_AUDIO_INTERNALLY, false, false);
	}

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(@Nullable final Context context, @Nullable final Intent intent) {
			if (intent == null || intent.getAction() == null) {
				return;
			}

			System.out.println("PPPPPPPPPPPPPPPPPP-AudioRecorder - " + intent.getAction());

			switch (intent.getAction()) {
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////

				case CONSTS_BC_AudioRec.ACTION_RECORD_AUDIO: {
					final boolean start = intent.getBooleanExtra(CONSTS_BC_AudioRec.EXTRA_RECORD_AUDIO_1, false);
					final int audio_source = intent.getIntExtra(CONSTS_BC_AudioRec.EXTRA_RECORD_AUDIO_2, -1);
					final boolean restart_pocketsphinx = intent.getBooleanExtra(CONSTS_BC_AudioRec.EXTRA_RECORD_AUDIO_3,
							true);
					recordAudio(start, audio_source, restart_pocketsphinx);

					break;
				}
			}

			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
		}
	};
}
