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

import android.content.Intent;

import androidx.annotation.NonNull;

import com.dadi590.assist_c_a.GlobalUtils.UtilsApp;

/**
 * <p>Functions to call to communicate with {@link PhoneCallsProcessor}, by using broadcasts.</p>
 */
public final class UtilsPhoneCallsProcessorBC {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsPhoneCallsProcessorBC() {
	}

	/**
	 * <p>Broadcasts a request to execute {@link PhoneCallsProcessor#receiveCall(int, String, boolean)}.</p>
	 *
	 * @param call_state same as in the mentioned function
	 * @param phone_number same as in the mentioned function
	 * @param precise_call_state same as in the mentioned function
	 */
	public static void receiveCall(final int call_state, @NonNull final String phone_number,
								   final boolean precise_call_state) {
		final Intent broadcast_intent = new Intent(CONSTS_BC.ACTION_RECEIVE_CALL);
		broadcast_intent.putExtra(CONSTS_BC.EXTRA_RECEIVE_CALL_1, call_state);
		broadcast_intent.putExtra(CONSTS_BC.EXTRA_RECEIVE_CALL_2, phone_number);
		broadcast_intent.putExtra(CONSTS_BC.EXTRA_RECEIVE_CALL_3, precise_call_state);

		UtilsApp.sendInternalBroadcast(broadcast_intent);
	}
}
