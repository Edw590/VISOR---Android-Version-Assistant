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

package com.edw590.visor_c_a.Modules.CmdsExecutor;

/**
 * <p>Actions, extras, and classes to use to send a broadcast to this module.</p>
 * <br>
 * <p>Check the doc on the action string to know what to do.</p>
 */
final class CONSTS_BC_CmdsExec {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private CONSTS_BC_CmdsExec() {
	}

	/**
	 * <p>Executed function: {@link CmdsExecutor#processTask(String, boolean, boolean)}.</p>
	 * <p>Is broadcast by the class(es): {@link UtilsCmdsExecutorBC}.</p>
	 * <p>To be received only by the class(es): {@link CmdsExecutor}.</p>
	 * <p>Extras (ordered parameters):</p>
	 * <p>- {@link #EXTRA_CALL_PROCESS_TASK_1}: mandatory</p>
	 * <p>- {@link #EXTRA_CALL_PROCESS_TASK_2}: mandatory</p>
	 * <p>- {@link #EXTRA_CALL_PROCESS_TASK_3}: mandatory</p>
	 * <p>- {@link #EXTRA_CALL_PROCESS_TASK_4}: mandatory</p>
	 */
	static final String ACTION_CALL_PROCESS_TASK = "CmdsExecutor_CALL_PROCESS_TASK";
	static final String EXTRA_CALL_PROCESS_TASK_1 = "CmdsExecutor_EXTRA_CALL_PROCESS_TASK_1";
	static final String EXTRA_CALL_PROCESS_TASK_2 = "CmdsExecutor_EXTRA_CALL_PROCESS_TASK_2";
	static final String EXTRA_CALL_PROCESS_TASK_3 = "CmdsExecutor_EXTRA_CALL_PROCESS_TASK_3";
	static final String EXTRA_CALL_PROCESS_TASK_4 = "CmdsExecutor_EXTRA_CALL_PROCESS_TASK_4";
}
