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

package com.dadi590.assist_c_a.Modules.CmdsExecutor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.Modules.Speech.UtilsSpeech2BC;

/**
 * <p>Executor module specific utilities.</p>
 */
final class UtilsCmdsExecutor {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsCmdsExecutor() {
	}

	/**
	 * <p>Calls {@link UtilsSpeech2BC#speak(String, int, Integer)} with the given parameters, except the priority is
	 * always {@link Speech2#PRIORITY_USER_ACTION}.</p>
	 *
	 * @param speak same as in {@link UtilsSpeech2BC#speak(String, int, Integer)}
	 * @param bypass_no_sound same as in {@link UtilsSpeech2BC#speak(String, int, Integer)}
	 * @param after_speaking_code same as in {@link UtilsSpeech2BC#speak(String, int, Integer)}
	 */
	static void speak(@NonNull final String speak, final boolean bypass_no_sound,
					  @Nullable final Integer after_speaking_code) {
		UtilsSpeech2BC.speakExecutor(speak, Speech2.PRIORITY_USER_ACTION, bypass_no_sound, after_speaking_code);
	}
}
