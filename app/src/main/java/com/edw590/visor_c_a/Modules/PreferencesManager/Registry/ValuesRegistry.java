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

package com.edw590.visor_c_a.Modules.PreferencesManager.Registry;

import androidx.annotation.NonNull;

/**
 * <p>The static storage of all app global values.</p>
 */
public final class ValuesRegistry {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private ValuesRegistry() {
	}

	static final String PREFIX = "VALUES_";

	public static class Keys {

		// Add new keys to the values_list array below too.

		// Power Processor
		public static final String BATTERY_PRESENT = PREFIX + "BATTERY_PRESENT";
		public static final String BATTERY_PERCENT = PREFIX + "BATTERY_PERCENT";
		public static final String POWER_CONNECTED = PREFIX + "POWER_CONNECTED";
		// Device Locator
		public static final String DIST_ROUTER = PREFIX + "DIST_ROUTER";
		public static final String PUBLIC_IP = PREFIX + "PUBLIC_IP";
		public static final String CURR_NETWORK_TYPE = PREFIX + "CURR_NETWORK_TYPE";
		// Telephony - Phone calls
		public static final String LAST_PHONE_CALL_TIME = PREFIX + "LAST_PHONE_CALL_TIME";
		public static final String CURR_PHONE_CALL_NUMBER = PREFIX + "CURR_PHONE_CALL_NUMBER";
		// Telephony - SMS
		public static final String LAST_SMS_MSG_TIME = PREFIX + "LAST_SMS_MSG_TIME";
		public static final String LAST_SMS_MSG_NUMBER = PREFIX + "LAST_SMS_MSG_NUMBER";
		// Audio Recorder
		public static final String IS_RECORDING_AUDIO_INTERNALLY = PREFIX + "IS_RECORDING_AUDIO_INTERNALLY";
		// Flashlight
		public static final String MAIN_FLASHLIGHT_ENABLED = PREFIX + "MAIN_FLASHLIGHT_ENABLED";
		// Speech recognizers
		public static final String COMMANDS_RECOG_AVAILABLE = PREFIX + "COMMANDS_RECOG_AVAILABLE";
		public static final String POCKETSPHINX_RECOG_AVAILABLE = PREFIX + "POCKETSPHINX_RECOG_AVAILABLE";
		public static final String POCKETSPHINX_REQUEST_STOP = PREFIX + "POCKETSPHINX_RECOG_STOPPED";
	}

	static final Value[] VALUES_LIST = {
			// Note: if the value is not being updated, remove it from the list

			// Power
			new Value(Keys.BATTERY_PRESENT, "Power - Battery Present", Value.TYPE_BOOLEAN,
					"Is the battery present?"),
			new Value(Keys.BATTERY_PERCENT, "Power - Battery Percentage", Value.TYPE_INTEGER,
					"The battery percentage"),
			new Value(Keys.POWER_CONNECTED, "Power - Power Connected", Value.TYPE_BOOLEAN,
					"Is the device connected to power?"),

			// Device Locator
			new Value(Keys.DIST_ROUTER, "Location - Distance to Wi-Fi router (m)", Value.TYPE_INTEGER,
					"Distance in meters from the device to the Wi-Fi router of the current network"),
			new Value(Keys.PUBLIC_IP, "Location - Public IP", Value.TYPE_STRING,
					"The public IP address from the current network connection"),
			new Value(Keys.CURR_NETWORK_TYPE, "Location - Current network type", Value.TYPE_INTEGER,
					"The current network type"),

			// Telephony - Phone calls
			new Value(Keys.LAST_PHONE_CALL_TIME, "Telephony - Last call when (ms)", Value.TYPE_LONG,
					"Timestamp of the last phone call (in milliseconds)"),
			new Value(Keys.CURR_PHONE_CALL_NUMBER, "Telephony - Number of current call", Value.TYPE_STRING,
					"Number of the last phone call"),

			// Telephony - SMS
			new Value(Keys.LAST_SMS_MSG_TIME, "Telephony - Last SMS msg when (ms)", Value.TYPE_LONG,
					"Timestamp of the last SMS message (in milliseconds)"),
			new Value(Keys.LAST_SMS_MSG_NUMBER, "Telephony - Number of last SMS msg sender", Value.TYPE_STRING,
					"Number of the last SMS message"),

			// Audio Recorder
			new Value(Keys.IS_RECORDING_AUDIO_INTERNALLY, "Audio Recorder - Recording internally", Value.TYPE_BOOLEAN, false,
					"Is VISOR recording audio internally?"),

			// Flashlight
			new Value(Keys.MAIN_FLASHLIGHT_ENABLED, "Camera - Main flashlight enabled (Android 6+)", Value.TYPE_BOOLEAN,
					"Is the main flashlight enabled? (Only available from Android Marshmallow onwards)"),

			// Speech Recognizers
			new Value(Keys.COMMANDS_RECOG_AVAILABLE, "Speech recognition - Commands available", Value.TYPE_BOOLEAN,
					"Is the commands speech recognizer available?"),
			new Value(Keys.POCKETSPHINX_RECOG_AVAILABLE, "Speech recognition - Hotword available", Value.TYPE_BOOLEAN,
					"Is the hotword speech recognizer (PocketSphinx) available?"),
			new Value(Keys.POCKETSPHINX_REQUEST_STOP, "Speech recognition - Hotword requested to stop", Value.TYPE_BOOLEAN, false,
					"Was the hotword speech recognizer requested to stop?"),
	};

	/**
	 * <p>Get a clone of {@link #VALUES_LIST}.</p>
	 *
	 * @return .
	 */
	@NonNull
	public static synchronized Value[] getArray() {
		return VALUES_LIST.clone();
	}
}
