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

package com.dadi590.assist_c_a.Modules.Telephony.PhoneCallsProcessor;

/**
 * <p>Constants directly related to the Phone Calls Processor module.</p>
 */
public final class CONSTS_PhCallsProc {

	// TelephonyManager.ACTION_PRECISE_CALL_STATE_CHANGED - Android Studio doesn't find it, even though I see it there
	// (???)
	static final String ACTION_PRECISE_CALL_STATE_CHANGED = "android.intent.action.PRECISE_CALL_STATE";
	static final String EXTRA_RINGING_CALL_STATE = "ringing_state";
	static final String EXTRA_FOREGROUND_CALL_STATE = "foreground_state";
	static final String EXTRA_BACKGROUND_CALL_STATE = "background_state";

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private CONSTS_PhCallsProc() {
	}
}
