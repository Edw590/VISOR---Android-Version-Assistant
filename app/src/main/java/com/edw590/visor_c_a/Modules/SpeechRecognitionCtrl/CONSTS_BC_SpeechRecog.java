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

package com.edw590.visor_c_a.Modules.SpeechRecognitionCtrl;

import com.edw590.visor_c_a.TasksList;

/**
 * <p>Actions, extras, and classes to use to send a broadcast to this module.</p>
 * <br>
 * <p>Check the doc on the action string to know what to do.</p>
 */
final class CONSTS_BC_SpeechRecog {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private CONSTS_BC_SpeechRecog() {
	}

	/**
	 * <p>Explanation: commands {@link SpeechRecognitionCtrl} to start the commands recognition.</p>
	 * <p>Is broadcast by the class(es): {@link UtilsSpeechRecognizersBC}.</p>
	 * <p>To be received only by the class(es): {@link SpeechRecognitionCtrl}.</p>
	 * <p>Extras: none.</p>
	 */
	static final String ACTION_START_CMDS_RECOG = "SpeechRecognition_ACTION_START_CMDS_RECOG";

	/**
	 * <p>Explanation: commands {@link SpeechRecognitionCtrl} to start the PocketSphinx recognition.</p>
	 * <p>Is broadcast by the class(es): {@link UtilsSpeechRecognizersBC}.</p>
	 * <p>To be received only by the class(es): {@link SpeechRecognitionCtrl}.</p>
	 * <p>Extras: none.</p>
	 */
	static final String ACTION_START_POCKET_SPHINX = "SpeechRecognition_ACTION_START_POCKET_SPHINX";

	/**
	 * <p>Explanation: commands {@link SpeechRecognitionCtrl} to stop and keep stopped all speech recognizers.</p>
	 * <p>Is broadcast by the class(es): {@link UtilsSpeechRecognizersBC}.</p>
	 * <p>To be received only by the class(es): {@link SpeechRecognitionCtrl}.</p>
	 * <p>Extras:</p>
	 * <p>- {@link #EXTRA_STOP_RECOGNITION_1} (optional): the ID of a {@link TasksList.Task} to execute after stopping.</p>
	 */
	static final String ACTION_STOP_RECOGNITION = "SpeechRecognition_ACTION_STOP_RECOGNITION";
	static final String EXTRA_STOP_RECOGNITION_1 = "SpeechRecognition_EXTRA_STOP_RECOGNITION_1";

	/**
	 * <p>Explanation: commands {@link SpeechRecognitionCtrl} to terminate all speech recognizers.</p>
	 * <p>Is broadcast by the class(es): {@link UtilsSpeechRecognizersBC}.</p>
	 * <p>To be received only by the class(es): {@link SpeechRecognitionCtrl}.</p>
	 * <p>Extras: none.</p>
	 */
	static final String ACTION_TERMINATE_RECOGNIZERS = "SpeechRecognition_ACTION_TERMINATE_RECOGNIZERS";

	/**
	 * <p>Explanation: informs that the commands speech recognizer is initializing.</p>
	 * <p>Is broadcast by the class(es): {@link CommandsRecognition}.</p>
	 * <p>To be received only by the class(es): any chosen class.</p>
	 * <p>Extras: none.</p>
	 */
	public static final String ACTION_CMDS_RECOG_STARTING = "SpeechRecognition_ACTION_CMDS_RECOG_STARTING";

	/**
	 * <p>Explanation: informs that the commands speech recognizer stopped listening.</p>
	 * <p>Is broadcast by the class(es): {@link CommandsRecognition}.</p>
	 * <p>To be received only by the class(es): any chosen class.</p>
	 * <p>Extras: none.</p>
	 */
	public static final String ACTION_CMDS_RECOG_STOPPED = "SpeechRecognition_ACTION_CMDS_RECOG_STOPPED";
}
