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

import com.edw590.visor_c_a.TasksList;

import java.util.ArrayList;

/**
 * <p>Actions, extras, and classes to use to send a broadcast to this module.</p>
 * <br>
 * <p>Check the doc on the action string to know what to do.</p>
 */
public final class CONSTS_BC_Speech {

	/**
	 * <p>Explanation: warns when a speech that requested to broadcast a code after being finished, has finished.</p>
	 * <p>Is broadcast by the class(es): {@link Speech2}.</p>
	 * <p>To be received only by the class(es): any chosen class.</p>
	 * <p>Extras:</p>
	 * <p>- {@link #EXTRA_AFTER_SPEAK_ID_1} (String): the unique ID of the speech that took place</p>
	 */
	public static final String ACTION_AFTER_SPEAK_ID = "Speech_ACTION_AFTER_SPEAK_ID";
	public static final String EXTRA_AFTER_SPEAK_ID_1 = "Speech_EXTRA_AFTER_SPEAK_ID_1";

	/**
	 * <p>Explanation: warns when the speech module is ready for use.</p>
	 * <p>Is broadcast by the class(es): {@link Speech2}.</p>
	 * <p>To be received only by the class(es): any chosen class.</p>
	 * <p>Extras: none.</p>
	 */
	public static final String ACTION_READY = "Speech_ACTION_READY";

	/**
	 * <p>Explanation: calls {@link Speech2#skipCurrentSpeech()}.</p>
	 * <p>Is broadcast by the class(es): {@link UtilsSpeech2BC}.</p>
	 * <p>To be received only by the class(es): {@link Speech2}.</p>
	 * <p>Extras: none.</p>
	 */
	static final String ACTION_SKIP_SPEECH = "Speech_ACTION_SKIP_SPEECH";

	/**
	 * <p>Explanation: removes a speech from the lists based on its speech string. More info on
	 * {@link UtilsSpeech2#getSpeechIdBySpeech(String, int, boolean, ArrayList)} and
	 * {@link UtilsSpeech2#removeSpeechById(String, ArrayList)}.</p>
	 * <p>Main executed function: {@link UtilsSpeech2#getSpeechIdBySpeech(String, int, boolean, ArrayList)}.</p>
	 * <p>Is broadcast by the class(es): {@link UtilsSpeech2BC}.</p>
	 * <p>To be received only by the class(es): {@link Speech2}.</p>
	 * <p>Extras (ordered parameters):</p>
	 * <p>- {@link #EXTRA_REMOVE_SPEECH_1}: mandatory</p>
	 * <p>- {@link #EXTRA_REMOVE_SPEECH_2}: mandatory</p>
	 * <p>- {@link #EXTRA_REMOVE_SPEECH_3}: mandatory</p>
	 * <p>- Not required</p>
	 */
	static final String ACTION_REMOVE_SPEECH = "Speech_ACTION_REMOVE_SPEECH";
	static final String EXTRA_REMOVE_SPEECH_1 = "Speech_EXTRA_REMOVE_SPEECH_1";
	static final String EXTRA_REMOVE_SPEECH_2 = "Speech_EXTRA_REMOVE_SPEECH_2";
	static final String EXTRA_REMOVE_SPEECH_3 = "Speech_EXTRA_REMOVE_SPEECH_3";

	/**
	 * <p>Executed function: {@link Speech2#speak(String, int, int)}.</p>
	 * <p>Is broadcast by the class(es): {@link UtilsSpeech2BC}.</p>
	 * <p>To be received only by the class(es): {@link Speech2}.</p>
	 * <p>Extras (ordered parameters):</p>
	 * <p>- {@link #EXTRA_CALL_SPEAK_1}: mandatory</p>
	 * <p>- {@link #EXTRA_CALL_SPEAK_2}: optional (default is 0)</p>
	 * <p>- {@link #EXTRA_CALL_SPEAK_3}: mandatory</p>
	 * <p>- {@link #EXTRA_CALL_SPEAK_4}: optional (default is null) - in this case it must be the hash code of the
	 * runnable, runnable which must be manually added to the list through {@link TasksList#addTask(Runnable)}</p>
	 * <p>- {@link #EXTRA_CALL_SPEAK_5}: mandatory</p>
	 */
	static final String ACTION_CALL_SPEAK = "Speech_ACTION_CALL_SPEAK";
	static final String EXTRA_CALL_SPEAK_1 = "Speech_EXTRA_CALL_SPEAK_1";
	static final String EXTRA_CALL_SPEAK_2 = "Speech_EXTRA_CALL_SPEAK_2";
	static final String EXTRA_CALL_SPEAK_3 = "Speech_EXTRA_CALL_SPEAK_3";
	static final String EXTRA_CALL_SPEAK_4 = "Speech_EXTRA_CALL_SPEAK_4";
	static final String EXTRA_CALL_SPEAK_5 = "Speech_EXTRA_CALL_SPEAK_5";

	/**
	 * <p>Explanation: requests the last speech to be spoken again.</p>
	 * <p>Is broadcast by the class(es): {@link UtilsSpeech2BC}.</p>
	 * <p>To be received only by the class(es): {@link Speech2}.</p>
	 * <p>Extras: none.</p>
	 */
	static final String ACTION_SAY_AGAIN = "Speech_ACTION_SAY_AGAIN";

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private CONSTS_BC_Speech() {
	}
}
