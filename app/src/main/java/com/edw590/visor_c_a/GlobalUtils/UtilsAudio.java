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

package com.edw590.visor_c_a.GlobalUtils;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.os.Build;

import androidx.annotation.NonNull;

/**
 * <p>Audio-related utilities.</p>
 */
public final class UtilsAudio {
	/**
	 * <p>Checks if a {@link android.media.MediaRecorder.AudioSource} is available for immediate use.</p>
	 *
	 * @param audio_source the audio source to check
	 *
	 * @return true if it's available for immediate use, false otherwise (doesn't exist on the device or is busy) OR if
	 * there is no permission to record audio (check that before calling this function)
	 */
	public static boolean isAudioSourceAvailable(final int audio_source) {
		final int sample_rate = 44100;
		final int channel_config = AudioFormat.CHANNEL_IN_MONO;
		final int audio_format = AudioFormat.ENCODING_PCM_16BIT;

		if (!UtilsPermsAuths.checkSelfPermission(Manifest.permission.RECORD_AUDIO)) {
			return false;
		}

		final AudioRecord audioRecord;
		try {
			audioRecord = new AudioRecord(audio_source, sample_rate, channel_config, audio_format,
					AudioRecord.getMinBufferSize(sample_rate, channel_config, audio_format));
		} catch (final IllegalStateException ignored) {
			return false;
		}

		final boolean initialized = AudioRecord.STATE_INITIALIZED == audioRecord.getState();

		boolean success_recording = false;
		if (initialized) {
			try {
				audioRecord.startRecording();
				success_recording = audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING;
			} catch (final IllegalStateException ignored) {
			}
		}

		if (initialized) {
			// If it's initialized, stop it.
			audioRecord.stop();
		}
		audioRecord.release();

		return success_recording;
	}

	/**
	 * <p>Adjusts the volume of raw audio data, for example gotten from {@link AudioRecord}.</p>
	 * <p>Note that this function does not care about noise or anything at all - it just applies the same gain to ALL
	 * bytes. If the gain would generate an overflow, {@link Short#MAX_VALUE} is used instead.</p>
	 * <p>If gain is 1.0f, this function is a no-op.</p>
	 *
	 * @param audio_bytes the audio data
	 * @param gain the gain to apply to the data
	 */
	public static void adjustGainBuffer(@NonNull final short[] audio_bytes, final float gain) {
		if (0 == Float.compare(1.0f, gain)) {
			return;
		}

		final int audio_length = audio_bytes.length;
		for (int i = 0; i < audio_length; ++i) {
			audio_bytes[i] = (short)Math.min((int)((float) audio_bytes[i] * gain), (int)Short.MAX_VALUE);
		}
	}

	/**
	 * <p>Sets the audio modes of the device.</p>
	 *
	 * @param audio_stream the audio stream to set the volume of (use -1 to not change it) - use this in combination
	 * with {@code volume}
	 * @param volume the volume to set (use -1 to not change it)
	 * @param ringer_mode the ringer mode to set (use -1 to not change it)
	 * @param interruption_filter the interruption filter to set (use -1 to not change it)
	 */
	public static void setAudioModes(int audio_stream, int volume, int ringer_mode, int interruption_filter) {
		final NotificationManager notificationManager = UtilsContext.getNotificationManager();
		final AudioManager audioManager = (AudioManager) UtilsContext.getSystemService(Context.AUDIO_SERVICE);

		if (audio_stream != -1 && volume != -1 && audioManager != null) {
			audioManager.setStreamVolume(audio_stream, volume, 0);
		}

		if (ringer_mode != -1 && audioManager != null) {
			audioManager.setRingerMode(ringer_mode);
		}

		if (interruption_filter != -1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			notificationManager.setInterruptionFilter(interruption_filter);
		}
	}

	/**
	 * <p>Gets the audio modes of the device.</p>
	 * <p>Use only one of the parameters at a time.</p>
	 *
	 * @param audio_stream the audio stream to get the volume of (use -1 to not get it)
	 * @param ringer_mode get the ringer mode
	 * @param interruption_filter get the interruption filter
	 *
	 * @return the audio mode requested, or -1 if it wasn't requested or if it's not available (like no audio on device)
	 */
	public static int getAudioModes(int audio_stream, boolean ringer_mode, boolean interruption_filter) {
		final NotificationManager notificationManager = UtilsContext.getNotificationManager();
		final AudioManager audioManager = (AudioManager) UtilsContext.getSystemService(Context.AUDIO_SERVICE);

		if (audio_stream != -1 && audioManager != null) {
			return audioManager.getStreamVolume(audio_stream);
		}

		if (ringer_mode && audioManager != null) {
			return audioManager.getRingerMode();
		}

		if (interruption_filter && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			return notificationManager.getCurrentInterruptionFilter();
		}

		return -1;
	}
}
