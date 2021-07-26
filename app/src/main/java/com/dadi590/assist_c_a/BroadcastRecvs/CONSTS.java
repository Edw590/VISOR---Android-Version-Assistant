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

package com.dadi590.assist_c_a.BroadcastRecvs;

/**
 * <p>Constants related to the broadcast receivers.</p>
 */
public final class CONSTS {

	// These 2 below appeared in a StackOverflow answer. Maybe it's the same explanation as the POWERON one. Keep it.
	static final String ACTION_HTC_QCK_POFF = "com.htc.intent.action.QUICKBOOT_POWEROFF";
	static final String ACTION_ANDR_QCK_POFF = "android.intent.action.QUICKBOOT_POWEROFF";

	// Below Android KitKat, there's no constant, but the string appears to exist, even without constant,
	// so it should still work below KitKat. I'll leave it as a string, or the switch won't like 2 equal values.
	// I'll leave the new one here uncommented and unused, but only for Android Studio to throw a warning in case it
	// stops existing. EDIT: commented out now, or now idea if it throws errors in KitKat and below for only loading it
	//private static final String NO_USE = Telephony.Sms.Intents.SMS_RECEIVED_ACTION; --> API 19 (KitKat) and above
	static final String ACTION_SMS_RECEIVED_ALL_API = "android.provider.Telephony.SMS_RECEIVED";

	// TelephonyManager.ACTION_PRECISE_CALL_STATE_CHANGED - Android Studio doesn't find it, even though I see it there
	// (???)
	static final String ACTION_PRECISE_CALL_STATE_CHANGED = "android.intent.action.PRECISE_CALL_STATE";

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private CONSTS() {
	}
}
