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

import androidx.annotation.Nullable;

/**
 * <p>A class to be used as a "multi-type array" for the returning value of
 * {@link PhoneCallsProcessor#getCallPhase(int, String)}.</p>
 */
class NumAndPhase {

	@Nullable final String phone_number;
	final int call_phase;

	/**
	 * <p>Main class constructor.</p>
	 *
	 * @param phone_number the phone number directly from the intent extra (null or not --> directly)
	 * @param call_phase one of the {@code CALL_PHASE_} constants in {@link PhoneCallsProcessor}
	 */
	NumAndPhase(@Nullable final String phone_number, final int call_phase) {
		this.phone_number = phone_number;
		this.call_phase = call_phase;
	}
}
