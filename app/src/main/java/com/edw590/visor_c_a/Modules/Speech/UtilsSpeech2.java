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

package com.edw590.visor_c_a.Modules.Speech;

import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;

import com.edw590.visor_c_a.GlobalUtils.UtilsContext;
import com.edw590.visor_c_a.Modules.SpeechRecognitionCtrl.UtilsSpeechRecognizersBC;

/**
 * <p>Speech API v2 related utilities.</p>
 */
public final class UtilsSpeech2 {

	/** Simple runnable that calls {@link UtilsSpeechRecognizersBC#startCommandsRecognition()}. */
	public static final Runnable CALL_COMMANDS_RECOG = new Runnable() {
		@Override
		public void run() {
			UtilsSpeechRecognizersBC.startCommandsRecognition();
		}
	};

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsSpeech2() {
	}

	/**
	 * <p>Use this right after calling {@link UtilsSpeech2BC#speak(String, int, int, String, boolean, Runnable)} to know
	 * if VISOR <em>might</em> actually speak (means the conditions for speaking are met - that's what is checked here).</p>
	 * <p>"Might" because if he'll speak or not is checked right before the speech takes place, this is just an
	 * approximation (and that's why it should be called right after the broadcast function - to give a bit of time).</p>
	 *
	 * @return true if he might speak, false if he might not
	 */
	public static boolean mightSpeak() {
		final AudioManager audioManager = (AudioManager) UtilsContext.getSystemService(Context.AUDIO_SERVICE);
		if (audioManager == null) {
			return false;
		}

		if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			return false;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			NotificationManager notificationManager = (NotificationManager) UtilsContext.getSystemService(Context.NOTIFICATION_SERVICE);
			if (notificationManager == null) {
				return true;
			}
			int filter = notificationManager.getCurrentInterruptionFilter();
			boolean dnd_active = (filter == NotificationManager.INTERRUPTION_FILTER_NONE ||
					filter == NotificationManager.INTERRUPTION_FILTER_PRIORITY);

			return !dnd_active;
		}

		return true;
	}
}
