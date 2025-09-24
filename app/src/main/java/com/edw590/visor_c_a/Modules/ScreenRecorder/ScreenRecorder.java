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

package com.edw590.visor_c_a.Modules.ScreenRecorder;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Range;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.edw590.visor_c_a.ActivitiesFragments.Activities.ActScrCapturePerm;
import com.edw590.visor_c_a.GlobalInterfaces.IModuleInst;
import com.edw590.visor_c_a.GlobalUtils.UtilsContext;
import com.edw590.visor_c_a.GlobalUtils.UtilsGeneral;
import com.edw590.visor_c_a.GlobalUtils.UtilsLogging;
import com.edw590.visor_c_a.GlobalUtils.UtilsMedia;
import com.edw590.visor_c_a.GlobalUtils.UtilsPermsAuths;
import com.edw590.visor_c_a.Modules.Speech.Speech2;
import com.edw590.visor_c_a.Modules.Speech.UtilsSpeech2BC;
import com.edw590.visor_c_a.Modules.SpeechRecognitionCtrl.UtilsSpeechRecognizersBC;
import com.edw590.visor_c_a.ModulesList;
import com.edw590.visor_c_a.Registry.RegistryKeys;
import com.edw590.visor_c_a.Registry.UtilsRegistry;

import java.io.File;
import java.io.IOException;

/**
 * <p>The audio recorder module of the assistant.</p>
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public final class ScreenRecorder implements IModuleInst {

	@Nullable public static Intent token_data = null;

	private final int element_index = ModulesList.getElementIndex(getClass());
	private final HandlerThread main_handlerThread = new HandlerThread((String) ModulesList.getElementValue(element_index,
			ModulesList.ELEMENT_NAME));
	private final Handler main_handler;

	private MediaProjectionManager media_projection_manager = null;
	private MediaProjection media_projection = null;
	@Nullable private MediaRecorder media_recorder = null;
	private VirtualDisplay virtual_display = null;

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
		String[] min_required_permissions = {
				Manifest.permission.WRITE_EXTERNAL_STORAGE,
		};
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
				UtilsPermsAuths.checkSelfPermissions(min_required_permissions);
	}
	// IModuleInst stuff
	///////////////////////////////////////////////////////////////

	/**
	 * <p>Main class constructor.</p>
	 */
	public ScreenRecorder() {
		main_handlerThread.start();
		main_handler = new Handler(main_handlerThread.getLooper());

		// Update the Registry
		UtilsRegistry.setData(RegistryKeys.K_IS_RECORDING_SCREEN_INTERNALLY, false, false);

		media_projection_manager = (MediaProjectionManager)
				UtilsContext.getContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE);

		try {
			IntentFilter intentFilter = new IntentFilter();

			intentFilter.addAction(CONSTS_BC_ScreenRec.ACTION_RECORD_SCREEN);

			UtilsContext.getContext().registerReceiver(broadcastReceiver, new IntentFilter(intentFilter), null,
					main_handler);
		} catch (final IllegalArgumentException ignored) {
		}
	}

	/**
	 * <p>Method to call instead of calling directly {@link #startRecording()}.</p>
	 *
	 * @param start true to start recording, false to stop recording
	 * @param restart_pocketsphinx in case it's to stop the recorder, true to restart pocketsphinx, false not to restart
	 * 	 *                         pocketsphinx it. Outside that situation this parameter is ignored.
	 */
	void recordScreen(final boolean start, final boolean restart_pocketsphinx) {
		boolean is_recording = (boolean) UtilsRegistry.getData(RegistryKeys.K_IS_RECORDING_SCREEN_INTERNALLY, true);

		if (start) {
			if (is_recording) {
				String speak = "Already recording the screen sir.";
				UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_USER_ACTION, 0, UtilsSpeech2BC.SESSION_TYPE_NONE, false, null);
			} else {
				final Runnable runnable = () -> {
					if (startRecording() != NO_ERRORS) {
						// In case of an error, restart the hotword recognition again.
						UtilsSpeechRecognizersBC.startPocketSphinxRecognition();
					}
				};
				final String speak = "Starting to record the screen now, sir.";
				UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_USER_ACTION, 0, UtilsSpeech2BC.SESSION_TYPE_NONE, false, runnable);
			}
		} else {
			if (is_recording) {
				stopRecording();
				String speak = "Stopped recording the screen, sir.";
				UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_USER_ACTION, 0, UtilsSpeech2BC.SESSION_TYPE_NONE, false, null);

				if (restart_pocketsphinx) {
					UtilsSpeechRecognizersBC.startPocketSphinxRecognition();
				}
			} else {
				String speak = "Already stopped recording the screen, sir.";
				UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_USER_ACTION, 0, UtilsSpeech2BC.SESSION_TYPE_NONE, false, null);
			}
		}
	}

	public static final int NO_ERRORS = 0;
	public static final int ERR_CREATE_FILE = 1;
	public static final int ERR_PREP_RECORDING = 2;
	public static final int ERR_PERM_CAP_SCREEN = 3;
	/**
	 * <p>Starts a screen recording, recording to default media output files, given by
	 * {@link UtilsMedia#getOutputMediaFile(int)}</p>.
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #NO_ERRORS} --> for the returning value: when the recording successfully started</p>
	 * <p>- {@link #ERR_CREATE_FILE} --> for the returning value: when there was an error creating the recording file</p>
	 * <p>- {@link #ERR_PREP_RECORDING} --> for the returning value: when there was an error preparing the recording</p>
	 * <p>- {@link #ERR_PERM_CAP_SCREEN} --> for the returning value: when the permission to record the screen was not
	 * granted</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param audioSource one of {@link MediaRecorder#setAudioSource(int)}'s parameters
	 *
	 * @return one of the constants
	 */
	int startRecording() {
		File file = UtilsMedia.getOutputMediaFile(UtilsMedia.SCREENREC);
		File folder = file.getParentFile();
		assert folder != null; // Won't be null, there's always a parent folder
		if (!folder.exists() && !file.getParentFile().mkdirs()) {
			String speak = "Error creating screen recording file, sir.";
			UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_USER_ACTION, 0, UtilsSpeech2BC.SESSION_TYPE_NONE, false, null);

			return ERR_CREATE_FILE;
		}

		// Update the token_data
		UtilsContext.getContext().startActivity(new Intent(UtilsContext.getContext(), ActScrCapturePerm.class)
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		long start_time = System.currentTimeMillis();
		while (token_data == null && (System.currentTimeMillis() - start_time) < 10*1000) {
			try {
				Thread.sleep(500);
			} catch (final InterruptedException ignored) {
			}
		}
		// If after 10 seconds the token_data is still null, return error

		if (token_data == null) {
			String speak = "Error - no permission to record the screen.";
			UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_USER_ACTION, 0, UtilsSpeech2BC.SESSION_TYPE_NONE, false, null);

			return ERR_PERM_CAP_SCREEN;
		}
		media_projection = media_projection_manager.getMediaProjection(Activity.RESULT_OK, token_data);

		Point res_to_use = getResolutionToUse();

		if (!prepareMediaRecorder(file.getAbsolutePath(), res_to_use)) {
			String speak = "Error preparing the recording, sir.";
			UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_USER_ACTION, 0, UtilsSpeech2BC.SESSION_TYPE_NONE, false, null);

			return ERR_PREP_RECORDING;
		}

		// Use maxWidth and maxHeight for MediaRecorder and VirtualDisplay
		DisplayMetrics metrics = UtilsContext.getContext().getResources().getDisplayMetrics();
		virtual_display = media_projection.createVirtualDisplay(
				"ScreenRecord",
				res_to_use.x, res_to_use.y, metrics.densityDpi,
				DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
				media_recorder.getSurface(),
				null, null
		);

		media_recorder.start();

		// Update the Registry
		UtilsRegistry.setData(RegistryKeys.K_IS_RECORDING_SCREEN_INTERNALLY, true, false);

		return NO_ERRORS;
	}

	/**
	 * <p>Stops an ongoing screen recording.</p>
	 */
	private void stopRecording() {
		try {
			if (media_recorder != null) {
				media_recorder.stop();
				media_recorder.reset();
				media_recorder.release();
				media_recorder = null;
			}
			if (virtual_display != null) {
				virtual_display.release();
			}
			if (media_projection != null) {
				media_projection.stop();
			}
		} catch (final Exception ignored) {
		}

		// Update the Registry
		UtilsRegistry.setData(RegistryKeys.K_IS_RECORDING_SCREEN_INTERNALLY, false, false);
	}

	private boolean prepareMediaRecorder(final String file_path, @NonNull final Point res_to_use) {
		media_recorder = new MediaRecorder();
		media_recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		media_recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
		media_recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		media_recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
		media_recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
		media_recorder.setVideoEncodingBitRate(512 * 1000);
		media_recorder.setVideoFrameRate(30);
		media_recorder.setVideoSize(res_to_use.x, res_to_use.y);
		media_recorder.setOutputFile(file_path);
		try {
			media_recorder.prepare();

			return true;
		} catch (final IllegalStateException ignored) {
		} catch (final IOException ignored) {
		}

		return false;
	}

	@NonNull
	private Point getResolutionToUse() {
		// Get H.264 encoder capabilities
		MediaCodecList codecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
		MediaCodecInfo[] codecs = codecList.getCodecInfos();
		Point max_enc_res = new Point(0, 0);

		for (final MediaCodecInfo codec : codecs) {
			if (!codec.isEncoder()) {
				continue;
			}

			String[] types = codec.getSupportedTypes();
			for (final String type : types) {
				if (type.equalsIgnoreCase("video/avc")) {
					MediaCodecInfo.VideoCapabilities caps = codec.getCapabilitiesForType(type).getVideoCapabilities();
					Range<Integer> widths = caps.getSupportedWidths();
					Range<Integer> heights = caps.getSupportedHeights();
					max_enc_res.x = Math.max(max_enc_res.x, widths.getUpper());
					max_enc_res.y = Math.max(max_enc_res.y, heights.getUpper());

					break;
				}
			}
			if (max_enc_res.x > 0 && max_enc_res.y > 0) {
				break;
			}
		}

		//Falta pôres o áudio opcional --> com os novos intents

		DisplayMetrics metrics = UtilsContext.getContext().getResources().getDisplayMetrics();
		Point screen = new Point(metrics.widthPixels, metrics.heightPixels);

		int width = screen.x;
		int height = screen.y;

		if (width > max_enc_res.x || height > max_enc_res.y) {
			float ratio = Math.min(
					(float) max_enc_res.x / width,
					(float) max_enc_res.y / height
			);
			width = Math.round(width * ratio);
			height = Math.round(height * ratio);
		}

		return new Point(width, height);
	}

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(@Nullable final Context context, @Nullable final Intent intent) {
			if (intent == null || intent.getAction() == null) {
				return;
			}

			UtilsLogging.logLnInfo("PPPPPPPPPPPPPPPPPP-ScreenRecorder - " + intent.getAction());

			switch (intent.getAction()) {
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////

				case CONSTS_BC_ScreenRec.ACTION_RECORD_SCREEN: {
					boolean start = intent.getBooleanExtra(CONSTS_BC_ScreenRec.EXTRA_RECORD_SCREEN_1, false);
					boolean restart_pocketsphinx = intent.getBooleanExtra(CONSTS_BC_ScreenRec.EXTRA_RECORD_SCREEN_2, true);
					recordScreen(start, restart_pocketsphinx);

					break;
				}
			}

			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
		}
	};
}
