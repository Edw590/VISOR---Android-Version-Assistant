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

import android.content.Intent;

import com.edw590.visor_c_a.GlobalUtils.UtilsApp;
import com.edw590.visor_c_a.Modules.AudioRecorder.AudioRecorder;

/**
 * <p>Functions to call to send information to {@link AudioRecorder}, by using broadcasts.</p>
 */
public final class UtilsScreenRecorderBC {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsScreenRecorderBC() {
	}

	/**
	 * <p>Broadcasts a request - more info on {@link CONSTS_BC_ScreenRec#ACTION_RECORD_SCREEN}.</p>
	 *
	 * @param start read the action's documentation
	 */
	public static void recordScreen(final boolean start, final boolean restart_pocketsphinx) {
		final Intent broadcast_intent = new Intent(CONSTS_BC_ScreenRec.ACTION_RECORD_SCREEN);
		broadcast_intent.putExtra(CONSTS_BC_ScreenRec.EXTRA_RECORD_SCREEN_1, start);
		broadcast_intent.putExtra(CONSTS_BC_ScreenRec.EXTRA_RECORD_SCREEN_2, restart_pocketsphinx);

		UtilsApp.sendInternalBroadcast(broadcast_intent);
	}
}
