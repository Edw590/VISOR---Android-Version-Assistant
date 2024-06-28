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

import android.content.Intent;

import androidx.annotation.Nullable;

import com.edw590.visor_c_a.GlobalUtils.UtilsApp;
import com.edw590.visor_c_a.TasksList;

/**
 * <p>Functions to call to send information to {@link SpeechRecognitionCtrl}, by using broadcasts.</p>
 */
public final class UtilsSpeechRecognizersBC {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsSpeechRecognizersBC() {
	}

	// Keep it checking if the controller is running. If it's not running, could mean it's not supported, not just that
	// it broke down - leave the manager to keep modules working only, not these failsafe measures that are not too good.

	/**
	 * <p>Broadcasts a request - more info on {@link CONSTS_BC_SpeechRecog#ACTION_START_CMDS_RECOG} and executes
	 * {@link UtilsSpeechRecognizers#startCommandsRecognition()} immediately.</p>
	 */
	public static void startCommandsRecognition() {
		final Intent broadcast_intent = new Intent(CONSTS_BC_SpeechRecog.ACTION_START_CMDS_RECOG);

		UtilsApp.sendInternalBroadcast(broadcast_intent);
	}

	/**
	 * <p>Broadcasts a request - more info on {@link CONSTS_BC_SpeechRecog#ACTION_START_POCKET_SPHINX} and executes
	 * {@link UtilsSpeechRecognizers#startPocketSphinxRecognition()} immediately.</p>
	 */
	public static void startPocketSphinxRecognition() {
		final Intent broadcast_intent = new Intent(CONSTS_BC_SpeechRecog.ACTION_START_POCKET_SPHINX);

		UtilsApp.sendInternalBroadcast(broadcast_intent);
	}

	/**
	 * <p>Broadcasts a request - more info on {@link CONSTS_BC_SpeechRecog#ACTION_STOP_RECOGNITION}.</p>
	 *
	 * @param runnable the runnable associated with {@link CONSTS_BC_SpeechRecog#EXTRA_STOP_RECOGNITION_1}
	 */
	public static void stopRecognition(@Nullable final Runnable runnable) {
		final Intent broadcast_intent = new Intent(CONSTS_BC_SpeechRecog.ACTION_STOP_RECOGNITION);
		if (runnable != null) {
			broadcast_intent.putExtra(CONSTS_BC_SpeechRecog.EXTRA_STOP_RECOGNITION_1, TasksList.addTask(runnable));
		}

		UtilsApp.sendInternalBroadcast(broadcast_intent);
	}

	/**
	 * <p>Broadcasts a request - more info on {@link CONSTS_BC_SpeechRecog#ACTION_TERMINATE_RECOGNIZERS}.</p>
	 */
	public static void terminateSpeechRecognizers() {
		final Intent broadcast_intent = new Intent(CONSTS_BC_SpeechRecog.ACTION_TERMINATE_RECOGNIZERS);

		UtilsApp.sendInternalBroadcast(broadcast_intent);
	}
}
