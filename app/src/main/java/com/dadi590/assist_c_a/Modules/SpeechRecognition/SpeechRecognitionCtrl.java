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

package com.dadi590.assist_c_a.Modules.SpeechRecognition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.GlobalUtils.GL_BC_CONSTS;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.GlobalUtils.UtilsServices;
import com.dadi590.assist_c_a.GlobalUtils.UtilsSpeechRecognizers;

/**
 * <p>This is the module which controls the assistant's speech recognition.</p>
 * <p>It may control one or more speech recognizers, like PocketSphinx and/or Google Speech Recognition.</p>
 */
public class SpeechRecognitionCtrl {

	static final Class<?> NO_RECOGNIZER = null;
	static final Class<?> POCKETSPHINX_RECOGNIZER = PocketSphinxRecognition.class;
	static final Class<?> GOOGLE_RECOGNIZER = GoogleRecognition.class;

	@Nullable Class<?> current_recognizer = NO_RECOGNIZER;

	private static final long default_wait_time = 2_500L;
	long wait_time = default_wait_time;

	private Thread infinity_loop = new Thread(new Runnable() {
		@Override
		public void run() {
			while (true) {
				if (GOOGLE_RECOGNIZER == current_recognizer) {
					if (!UtilsServices.isServiceRunning(GoogleRecognition.class)) {
						current_recognizer = NO_RECOGNIZER;
						wait_time = default_wait_time;
					}
				} else if (POCKETSPHINX_RECOGNIZER == current_recognizer) {
					if (!UtilsServices.isServiceRunning(PocketSphinxRecognition.class)) {
						current_recognizer = NO_RECOGNIZER;
						wait_time = default_wait_time;
					}
				}

				if (NO_RECOGNIZER == current_recognizer) {
					UtilsSpeechRecognizers.startPocketSphinxRecognition();
					current_recognizer = POCKETSPHINX_RECOGNIZER;
				}

				try {
					Thread.sleep(wait_time);
				} catch (final InterruptedException ignored) {
					Thread.currentThread().interrupt();
				}
			}
		}
	});

	/**
	 * <p>Main class constructor.</p>
	 */
	public SpeechRecognitionCtrl() {
		try {
			UtilsGeneral.getContext().registerReceiver(broadcastReceiver,
					new IntentFilter(GL_BC_CONSTS.ACTION_GOOGLE_RECOG_STARTED));
		} catch (final IllegalArgumentException ignored) {
		}

		infinity_loop.start();
	}

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(@Nullable final Context context, @Nullable final Intent intent) {
			if (null == intent || null == intent.getAction()) {
				return;
			}

			System.out.println("PPPPPPPPPPPPPPPPPP-SpeechRecognitionCtrl - " + intent.getAction());

			switch (intent.getAction()) {
				case GL_BC_CONSTS.ACTION_GOOGLE_RECOG_STARTED: {
					current_recognizer = GOOGLE_RECOGNIZER;
					wait_time = 500L;

					break;
				}
				case GL_BC_CONSTS.ACTION_POCKETSPHINX_RECOG_STARTED: {
					current_recognizer = POCKETSPHINX_RECOGNIZER;
					wait_time = default_wait_time;

					break;
				}
			}
		}
	};
}
