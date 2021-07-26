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

package com.dadi590.assist_c_a.GlobalUtils;

/**
 * <p>Global broadcast actions that can be received by any component in the app.</p>
 */
public final class GL_BC_CONSTS {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private GL_BC_CONSTS() {
	}

	/**
	 * <p>Explanation: warns when a speech that requested to broadcast a code after being finished, has finished.</p>
	 * <p>Broadcast by: {@link com.dadi590.assist_c_a.Modules.Speech.Speech2}.</p>
	 * <p>Extras:</p>
	 * <p>- {@link #EXTRA_SPEECH2_AFTER_SPEAK_CODE} (int): unique ID of the {@link Runnable} to run</p>
	 */
	public static final String ACTION_SPEECH2_AFTER_SPEAK_CODE = "ACTION_SPEECH2_AFTER_SPEAK_CODE";
	public static final String EXTRA_SPEECH2_AFTER_SPEAK_CODE = "EXTRA_SPEECH2_AFTER_SPEAK_CODE";

	/**
	 * <p>Explanation: warns when the speech module is ready for use.</p>
	 * <p>Broadcast by: {@link com.dadi590.assist_c_a.Modules.Speech.Speech2}.</p>
	 * <p>Extras: none.</p>
	 */
	public static final String ACTION_SPEECH2_READY = "ACTION_SPEECH2_READY";
}
