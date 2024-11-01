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

package com.edw590.visor_c_a.ActivitiesFragments.Activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.edw590.visor_c_a.Modules.AudioRecorder.UtilsAudioRecorderBC;
import com.edw590.visor_c_a.Registry.UtilsRegistry;
import com.edw590.visor_c_a.Registry.RegistryKeys;
import com.edw590.visor_c_a.Modules.SpeechRecognitionCtrl.UtilsSpeechRecognizersBC;

/**
 * <p>The activity used to assist the user.</p>
 */
public final class ActAssist extends AppCompatActivity {

	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if ((boolean) UtilsRegistry.getData(RegistryKeys.K_IS_RECORDING_AUDIO_INTERNALLY, true)) {
			// If it's recording audio, it must be stopped. So stop and start the hotword recognizer.
			UtilsAudioRecorderBC.recordAudio(false, -1, true);
		} else {
			// If it's not recording audio, start the commands recognizer.
			UtilsSpeechRecognizersBC.startCommandsRecognition();
		}

		finish();
	}
}
