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

package com.dadi590.assist_c_a.Modules.Telephony.PhoneCallsProcessor;

/**
 * <p>A class to be used as an "multi-type array" for the returning value of
 * {@link PhoneCallsProcessor#getCallPhase(int, String)}.</p>
 */
class NumAndPhase {

	final String phone_number;
	final int call_phase;

	/**
	 * <p>Main class constructor.</p>
	 *
	 * @param phoneNumber the phone number directly from the intent extra (null or not --> directly)
	 * @param callPhase one of the {@code CALL_PHASE_} constants in {@link PhoneCallsProcessor}
	 */
	NumAndPhase(final String phoneNumber, final int callPhase) {
		phone_number = phoneNumber;
		call_phase = callPhase;
	}
}
