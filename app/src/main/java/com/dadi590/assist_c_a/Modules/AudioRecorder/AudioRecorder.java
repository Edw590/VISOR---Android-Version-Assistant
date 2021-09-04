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

package com.dadi590.assist_c_a.Modules.AudioRecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;

import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.GlobalUtils.GL_BC_CONSTS;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.GlobalUtils.UtilsMedia;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.Modules.Speech.UtilsSpeech2BC;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>The audio recorder module of the assistant.</p>
 */
public class AudioRecorder {

	@Nullable private MediaRecorder recorder = null;
	private boolean recording = false;

	final List<Runnable> runnables = new ArrayList<>(1);

	private static final String aud_src_tmp_file = "audioSourceCheck";

	/**
	 * <p>Main class constructor.</p>
	 */
	public AudioRecorder() {
		super();
	}

	/**
	 * <p>Method to call instead of calling directly {@link #startRecording(int, boolean)}.</p>
	 *  @param start true to start recording, false to stop recording
	 * @param audioSource same as in {@link #startRecording(int, boolean)}
	 */
	public final void record(final boolean start, final int audioSource) {
		if (start) {
			if (recording) {
				final String speak = "Already on it sir.";
				UtilsSpeech2BC.speak(speak, null, Speech2.PRIORITY_USER_ACTION, null);
			} else {
				final Runnable runnable = new Runnable() {
					@Override
					public void run() {
						startRecording(audioSource, false);
				/*if (audioSource == MediaRecorder.AudioSource.MIC && !recording) {
					// In case of an error and that the microphone is the audio source, start the background
					// recognition again.
					Utils_reconhecimentos_voz.iniciar_reconhecimento_pocketsphinx();
				}*/
					}
				};
				runnables.add(runnable);
				try {
					UtilsGeneral.getContext().registerReceiver(broadcastReceiver,
							new IntentFilter(GL_BC_CONSTS.ACTION_SPEECH2_AFTER_SPEAK_CODE));
				} catch (final IllegalArgumentException ignored) {
				}
				final String speak = "Starting now, sir.";
				UtilsSpeech2BC.speak(speak, null, Speech2.PRIORITY_USER_ACTION, runnable.hashCode());
				// todo Make it detect when the speech is finished to put the Runnable working
			}
		} else {
			if (recording) {
				stopRecording();
				final String speak = "Stopped, sir.";
				UtilsSpeech2BC.speak(speak, null, Speech2.PRIORITY_USER_ACTION, null);
			} else {
				final String speak = "Already stopped, sir.";
				UtilsSpeech2BC.speak(speak, null, Speech2.PRIORITY_USER_ACTION, null);
				// It's not supposed to be registered by now, but as a precaution.
				try {
					UtilsGeneral.getContext().unregisterReceiver(broadcastReceiver);
				} catch (final IllegalArgumentException ignored) {
				}
			}
		}
	}

	/**
	 * <p>The sole purpose of this register is detect when the speech module is ready so the Main Service can start
	 * everything else.</p>
	 */
	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (/*context == null ||*/ intent == null || intent.getAction() == null) {
				return;
			}

			System.out.println("PPPPPPPPPPPPPPPPPP-AudioRecorder - " + intent.getAction());

			if (intent.getAction().equals(GL_BC_CONSTS.ACTION_SPEECH2_AFTER_SPEAK_CODE)) {
				final int after_speak_code = intent.getIntExtra(GL_BC_CONSTS.EXTRA_SPEECH2_AFTER_SPEAK_CODE, -1);
				for (final Runnable runnable : runnables) {
					if (runnable.hashCode() == after_speak_code) {
						System.out.println("TTTTTTTTTT");
						runnable.run();
						try {
							UtilsGeneral.getContext().unregisterReceiver(this);
						} catch (final IllegalArgumentException ignored) {
						}
						return;
					}
				}
			}
		}
	};

	public static final int NO_ERRORS = 0;
	public static final int ERR_CREATE_FILE = 1;
	public static final int ERR_PREP_RECORDING = 2;
	public static final int ERR_PERM_CAP_AUDIO_OR_MIC_BUSY = 3;
	/**
	 * <p>Starts an audio recording, recording from the given audio source, to default media output files, given by
	 * {@link UtilsMedia#getOutputMediaFile(int)}</p>.
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #NO_ERRORS} --> for the returning value: when the recording successfully started</p>
	 * <p>- {@link #ERR_CREATE_FILE} --> for the returning value: when there was an error creating the recording file</p>
	 * <p>- {@link #ERR_PREP_RECORDING} --> for the returning value: when there was an error preparing the recording</p>
	 * <p>- {@link #ERR_PERM_CAP_AUDIO_OR_MIC_BUSY} --> for the returning value: when the permission to record from the
	 * given audio source was not granted, or if the microphone is already in use and the recording could not start</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param audioSource one of {@link MediaRecorder#setAudioSource(int)}'s parameters
	 * @param check_recording_possible true to check if it's possible to record from the given audio source, false
	 *                                 otherwise
	 * @return one of the constants
	 */
	int startRecording(final int audioSource, final boolean check_recording_possible) {
		// Do NOT change the coder and format settings. I've put those because they were compatible with all devices
		// that the app supports, and still the sound is very good.
		recorder = new MediaRecorder();
		recorder.setAudioSource(audioSource);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		final File file;
		if (check_recording_possible) {
			file = new File(UtilsGeneral.getContext().getCacheDir(), aud_src_tmp_file);
		} else {
			file = UtilsMedia.getOutputMediaFile(UtilsMedia.AUDIO);
		}
		if (file == null) {
			final String speak = "Error 1 sir.";
			UtilsSpeech2BC.speak(speak, Speech2.EXECUTOR_SOMETHING_SAID, Speech2.PRIORITY_USER_ACTION, null);

			recording = false;

			return ERR_CREATE_FILE;
		}
		final String fileName = file.getAbsolutePath();
		recorder.setOutputFile(fileName);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);

        /*if (!check_recording_possible) {
            Utils_reconhecimentos_voz.desativar_reconhecimentos_voz();
        }*/

		try {
			recorder.prepare();
		} catch (final IOException e) {
			recorder.release();
			recorder = null;
			if (!check_recording_possible) {
				final String speak = "Error 2 sir.";
				UtilsSpeech2BC.speak(speak, Speech2.EXECUTOR_SOMETHING_SAID, Speech2.PRIORITY_USER_ACTION, null);
			}
			file.delete();

			recording = false;

			return ERR_PREP_RECORDING;
		}

		try {
			recorder.start();
		} catch (final IllegalStateException e) {
			recorder.release();
			recorder = null;
			if (!check_recording_possible) {
				final String speak = "Error 3 sir.";
				UtilsSpeech2BC.speak(speak, Speech2.EXECUTOR_SOMETHING_SAID, Speech2.PRIORITY_USER_ACTION, null);
			}
			file.delete();

			recording = false;

			return ERR_PERM_CAP_AUDIO_OR_MIC_BUSY;
			/*int permission_status = UtilsPermissions.checkSelfPermission(Manifest.permission.CAPTURE_AUDIO_OUTPUT);
            if (permission_status == EPackageManager.PERMISSION_GRANTED) {
                return UtilsGeneral.FONTE_INDISPONIVEL;
            } else {
                return SEM_PERMISSAO_CAPTURE_AUDIO_OUTPUT;
            }*/
		}
		if (check_recording_possible) {
			recorder.release();
			recorder = null;
			file.delete();

			recording = false;

			return UtilsGeneral.FONTE_DISPONIVEL;
		}

		recording = true;

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
		recording = false;
	}
}
