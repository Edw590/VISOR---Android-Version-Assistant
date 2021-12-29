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

import android.content.Intent;

import androidx.annotation.NonNull;

import com.dadi590.assist_c_a.GlobalUtils.UtilsApp;

/**
 * <p>Functions to call to communicate with {@link CmdsExecutor}, by using broadcasts (for example by a different process
 * than the main one - where the Executor is executed).</p>
 */
public final class UtilsCmdsExecutorBC {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsCmdsExecutorBC() {
	}

	/**
	 * <p>Broadcasts a request - more info on {@link CONSTS_BC#ACTION_CALL_PROCESS_TASK}.</p>
	 *
	 * @param sentence_str same as in {@link CmdsExecutor#processTask(String, boolean, boolean)}
	 * @param partial_results same as in {@link CmdsExecutor#processTask(String, boolean, boolean)}
	 * @param only_returning same as in {@link CmdsExecutor#processTask(String, boolean, boolean)}
	 */
	public static void processTask(@NonNull final String sentence_str, final boolean partial_results,
								   final boolean only_returning) {
		final Intent broadcast_intent = new Intent(CONSTS_BC.ACTION_CALL_PROCESS_TASK);
		broadcast_intent.putExtra(CONSTS_BC.EXTRA_CALL_PROCESS_TASK_1, sentence_str);
		broadcast_intent.putExtra(CONSTS_BC.EXTRA_CALL_PROCESS_TASK_2, partial_results);
		broadcast_intent.putExtra(CONSTS_BC.EXTRA_CALL_PROCESS_TASK_3, only_returning);

		UtilsApp.sendInternalBroadcast(broadcast_intent);
	}
}
