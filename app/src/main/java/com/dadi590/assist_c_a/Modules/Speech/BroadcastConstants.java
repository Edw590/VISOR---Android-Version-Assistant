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

package com.dadi590.assist_c_a.Modules.Speech;

/**
 * <p>Actions, extras, and classes to use to send a broadcast to this module.</p>
 * <br>
 * <p>Check the doc on the action string to know what to do.</p>
 */
final class BroadcastConstants {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private BroadcastConstants() {
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * <p>What it does: calls {@link Speech2#skipCurrentSpeech()}.</p>
	 * <p>Extras: none.</p>
	 */
	static final String ACTION_SKIP_SPEECH = "SPEECH2_ACTION_SKIP_SPEECH";

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * <p>Explanation: removes a speech from the lists based on its speech string. More info on
	 * {@link Speech2#getSpeechIdBySpeech(String, int, boolean)} and {@link Speech2#removeSpeechById(String)}.</p>
	 * <p>Main target: {@link Speech2#getSpeechIdBySpeech(String, int, boolean)}</p>
	 * <p>Extras (ordered parameters):</p>
	 * <p>- {@link #EXTRA_REMOVE_SPEECH_1}: mandatory</p>
	 * <p>- {@link #EXTRA_REMOVE_SPEECH_2}: mandatory</p>
	 * <p>- {@link #EXTRA_REMOVE_SPEECH_3}: mandatory</p>
	 */
	static final String ACTION_REMOVE_SPEECH = "SPEECH2_ACTION_REMOVE_SPEECH";
	static final String EXTRA_REMOVE_SPEECH_1 = "SPEECH2_EXTRA_REMOVE_SPEECH_1";
	static final String EXTRA_REMOVE_SPEECH_2 = "SPEECH2_EXTRA_REMOVE_SPEECH_2";
	static final String EXTRA_REMOVE_SPEECH_3 = "SPEECH2_EXTRA_REMOVE_SPEECH_3";

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * <p>Target: {@link Speech2#speak(String, int, int, Integer)}.</p>
	 * <p>Extras (ordered parameters):</p>
	 * <p>- {@link #EXTRA_CALL_SPEAK_1}: mandatory</p>
	 * <p>- {@link #EXTRA_CALL_SPEAK_2}: optional (default is {@link Speech2#NO_ADDITIONAL_COMMANDS})</p>
	 * <p>- {@link #EXTRA_CALL_SPEAK_3}: mandatory</p>
	 * <p>- {@link #EXTRA_CALL_SPEAK_4}: optional (default is null)</p>
	 */
	static final String ACTION_CALL_SPEAK = "SPEECH2_ACTION_CALL_SPEAK";
	static final String EXTRA_CALL_SPEAK_1 = "SPEECH2_EXTRA_CALL_SPEAK_1";
	static final String EXTRA_CALL_SPEAK_2 = "SPEECH2_EXTRA_CALL_SPEAK_2";
	static final String EXTRA_CALL_SPEAK_3 = "SPEECH2_EXTRA_CALL_SPEAK_3";
	static final String EXTRA_CALL_SPEAK_4 = "SPEECH2_EXTRA_CALL_SPEAK_4";
}
