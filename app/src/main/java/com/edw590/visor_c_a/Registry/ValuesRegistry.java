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

package com.edw590.visor_c_a.Registry;

import Registry.Registry;

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

	// Note: if the value is not being updated, remove it from the list

	// System Checker
	/** Type: boolean. */
	public static final String K_BATTERY_PRESENT = PREFIX + "BATTERY_PRESENT";
	/** Type: int. */
	public static final String K_BATTERY_PERCENT = PREFIX + "BATTERY_PERCENT";
	/** Type: boolean. */
	public static final String K_POWER_CONNECTED = PREFIX + "POWER_CONNECTED";
	/** Type: int. */
	public static final String K_DIST_ROUTER = PREFIX + "DIST_ROUTER";
	/** Type: String. */
	public static final String K_PUBLIC_IP = PREFIX + "PUBLIC_IP";
	/** Type: int. */
	public static final String K_CURR_NETWORK_TYPE = PREFIX + "CURR_NETWORK_TYPE";
	/** Type: boolean. */
	public static final String K_AIRPLANE_MODE_ON = PREFIX + "AIRPLANE_MODE_ON";

	// User Locator
	/** Type: int. */
	public static final String K_CURR_USER_LOCATION = PREFIX + "CURR_USER_LOCATION";
	/** Type: boolean. */
	public static final String K_IS_USER_SLEEPING = PREFIX + "IS_USER_SLEEPING";

	// Telephony - Phone calls
	/** Type: long. */
	public static final String K_LAST_PHONE_CALL_TIME = PREFIX + "LAST_PHONE_CALL_TIME";
	/** Type: String. */
	public static final String K_CURR_PHONE_CALL_NUMBER = PREFIX + "CURR_PHONE_CALL_NUMBER";

	// Telephony - SMS
	/** Type: long. */
	public static final String K_LAST_SMS_MSG_TIME = PREFIX + "LAST_SMS_MSG_TIME";
	/** Type: String. */
	public static final String K_LAST_SMS_MSG_NUMBER = PREFIX + "LAST_SMS_MSG_NUMBER";

	// Audio Recorder
	/** Type: boolean. */
	public static final String K_IS_RECORDING_AUDIO_INTERNALLY = PREFIX + "IS_RECORDING_AUDIO_INTERNALLY";

	// Flashlight
	/** Type: boolean. */
	public static final String K_MAIN_FLASHLIGHT_ENABLED = PREFIX + "MAIN_FLASHLIGHT_ENABLED";

	// Speech recognizers
	/** Type: boolean. */
	public static final String K_COMMANDS_RECOG_AVAILABLE = PREFIX + "COMMANDS_RECOG_AVAILABLE";
	/** Type: boolean. */
	public static final String K_POCKETSPHINX_RECOG_AVAILABLE = PREFIX + "POCKETSPHINX_RECOG_AVAILABLE";
	/** Type: boolean. */
	public static final String K_POCKETSPHINX_REQUEST_STOP = PREFIX + "POCKETSPHINX_RECOG_STOPPED";

	/**
	 * <p>Registers all the keys in the registry.</p>
	 */
	public static void registerRegistryKeys() {
		// System Checker
		Registry.registerValue(K_BATTERY_PRESENT, "Power - Battery Present",
				"Is the battery present?", Registry.TYPE_BOOL);
		Registry.registerValue(K_BATTERY_PERCENT, "Power - Battery Percentage",
				"The battery percentage", Registry.TYPE_INT);
		Registry.registerValue(K_POWER_CONNECTED, "Power - Power Connected",
				"Is the device connected to power?", Registry.TYPE_BOOL);
		Registry.registerValue(K_DIST_ROUTER, "Location - Distance to Wi-Fi router (m)",
				"Distance in meters from the device to the Wi-Fi router of the current network", Registry.TYPE_INT);
		Registry.registerValue(K_PUBLIC_IP, "Location - Public IP",
				"The public IP address from the current network connection", Registry.TYPE_STRING);
		Registry.registerValue(K_CURR_NETWORK_TYPE, "Location - Current network type",
				"The current network type", Registry.TYPE_INT);
		Registry.registerValue(K_AIRPLANE_MODE_ON, "Location - Airplane mode On",
				"If the airplane mode is On", Registry.TYPE_INT);

		// User Locator
		Registry.registerValue(K_CURR_USER_LOCATION, "User Locator - Current user location",
				"The current user location", Registry.TYPE_INT);
		Registry.registerValue(K_IS_USER_SLEEPING, "User Locator - Is user sleeping",
				"Is the user sleeping?", Registry.TYPE_BOOL);

		// Telephony - Phone calls
		Registry.registerValue(K_LAST_PHONE_CALL_TIME, "Telephony - Last call when (ms)",
				"Timestamp of the last phone call (in milliseconds)", Registry.TYPE_LONG);
		Registry.registerValue(K_CURR_PHONE_CALL_NUMBER, "Telephony - Number of current call",
				"Number of the last phone call", Registry.TYPE_STRING);

		// Telephony - SMS
		Registry.registerValue(K_LAST_SMS_MSG_TIME, "Telephony - Last SMS msg when (ms)",
				"Timestamp of the last SMS message (in milliseconds)", Registry.TYPE_LONG);
		Registry.registerValue(K_LAST_SMS_MSG_NUMBER, "Telephony - Number of last SMS msg sender",
				"Number of the last SMS message", Registry.TYPE_STRING);

		// Audio Recorder
		Registry.registerValue(K_IS_RECORDING_AUDIO_INTERNALLY, "Audio Recorder - Recording internally",
				"Is VISOR recording audio internally?", Registry.TYPE_BOOL);

		// Flashlight
		Registry.registerValue(K_MAIN_FLASHLIGHT_ENABLED, "Camera - Main flashlight enabled (Android 6+)",
				"Is the main flashlight enabled? (Only available from Android Marshmallow onwards)", Registry.TYPE_BOOL);

		// Speech recognizers
		Registry.registerValue(K_COMMANDS_RECOG_AVAILABLE, "Speech recognition - Commands available",
				"Is the commands speech recognizer available?", Registry.TYPE_BOOL);
		Registry.registerValue(K_POCKETSPHINX_RECOG_AVAILABLE, "Speech recognition - Hotword available",
				"Is the hotword speech recognizer (PocketSphinx) available?", Registry.TYPE_BOOL);
		Registry.registerValue(K_POCKETSPHINX_REQUEST_STOP, "Speech recognition - Hotword requested to stop",
				"Was the hotword speech recognizer requested to stop?", Registry.TYPE_BOOL);
	}
}
