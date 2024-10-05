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

import UtilsSWA.UtilsSWA;

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
	public static final String K_BATTERY_LEVEL = PREFIX + "BATTERY_LEVEL";
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
	/** Type: int. */
	public static final String K_SCREEN_BRIGHTNESS = PREFIX + "SCREEN_BRIGHTNESS";
	/** Type: int. */
	public static final String K_SOUND_VOLUME = PREFIX + "SOUND_VOLUME";
	/** Type: boolean. */
	public static final String K_SOUND_MUTED = PREFIX + "SOUND_MUTED";
	/** Type: boolean. */
	public static final String K_DEVICE_IN_USE = PREFIX + "DEVICE_IN_USE";

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
		UtilsSWA.registerValueREGISTRY(K_BATTERY_PRESENT, "System Checker - Battery Present",
				"Is the battery present?", UtilsSWA.TYPE_BOOL);
		UtilsSWA.registerValueREGISTRY(K_BATTERY_LEVEL, "System Checker - Battery Level",
				"The battery level", UtilsSWA.TYPE_INT);
		UtilsSWA.registerValueREGISTRY(K_POWER_CONNECTED, "System Checker - Power Connected",
				"Is the device connected to power?", UtilsSWA.TYPE_BOOL);
		UtilsSWA.registerValueREGISTRY(K_AIRPLANE_MODE_ON, "System Checker - Airplane mode On",
				"Whether the airplane mode is On", UtilsSWA.TYPE_INT);
		UtilsSWA.registerValueREGISTRY(K_CURR_NETWORK_TYPE, "System Checker - Current network type",
				"The current network type", UtilsSWA.TYPE_INT);
		UtilsSWA.registerValueREGISTRY(K_SCREEN_BRIGHTNESS, "System Checker - Screen Brightness",
				"The screen brightness", UtilsSWA.TYPE_INT);
		UtilsSWA.registerValueREGISTRY(K_SOUND_VOLUME, "System Checker - Sound Volume",
				"The sound volume", UtilsSWA.TYPE_INT);
		UtilsSWA.registerValueREGISTRY(K_SOUND_MUTED, "System Checker - Sound Muted",
				"Whether the sound is muted", UtilsSWA.TYPE_BOOL);
		UtilsSWA.registerValueREGISTRY(K_DEVICE_IN_USE, "System Checker - Device In Use",
				"Whether the device is being used", UtilsSWA.TYPE_BOOL);

		// TODO: User Locator
		UtilsSWA.registerValueREGISTRY(K_DIST_ROUTER, "Location - Distance to Wi-Fi router (m)",
				"Distance in meters from the device to the Wi-Fi router of the current network", UtilsSWA.TYPE_INT);
		UtilsSWA.registerValueREGISTRY(K_PUBLIC_IP, "Location - Public IP",
				"The public IP address from the current network connection", UtilsSWA.TYPE_STRING);

		// User Locator
		UtilsSWA.registerValueREGISTRY(K_CURR_USER_LOCATION, "User Locator - Current user location",
				"The current user location", UtilsSWA.TYPE_INT);
		UtilsSWA.registerValueREGISTRY(K_IS_USER_SLEEPING, "User Locator - Is user sleeping",
				"Is the user sleeping?", UtilsSWA.TYPE_BOOL);

		// Telephony - Phone calls
		UtilsSWA.registerValueREGISTRY(K_LAST_PHONE_CALL_TIME, "Telephony - Last call when (ms)",
				"Timestamp of the last phone call (in milliseconds)", UtilsSWA.TYPE_LONG);
		UtilsSWA.registerValueREGISTRY(K_CURR_PHONE_CALL_NUMBER, "Telephony - Number of current call",
				"Number of the last phone call", UtilsSWA.TYPE_STRING);

		// Telephony - SMS
		UtilsSWA.registerValueREGISTRY(K_LAST_SMS_MSG_TIME, "Telephony - Last SMS msg when (ms)",
				"Timestamp of the last SMS message (in milliseconds)", UtilsSWA.TYPE_LONG);
		UtilsSWA.registerValueREGISTRY(K_LAST_SMS_MSG_NUMBER, "Telephony - Number of last SMS msg sender",
				"Number of the last SMS message", UtilsSWA.TYPE_STRING);

		// Audio Recorder
		UtilsSWA.registerValueREGISTRY(K_IS_RECORDING_AUDIO_INTERNALLY, "Audio Recorder - Recording internally",
				"Is VISOR recording audio internally?", UtilsSWA.TYPE_BOOL);

		// Flashlight
		UtilsSWA.registerValueREGISTRY(K_MAIN_FLASHLIGHT_ENABLED, "Camera - Main flashlight enabled (Android 6+)",
				"Is the main flashlight enabled? (Only available from Android Marshmallow onwards)", UtilsSWA.TYPE_BOOL);

		// Speech recognizers
		UtilsSWA.registerValueREGISTRY(K_COMMANDS_RECOG_AVAILABLE, "Speech recognition - Commands available",
				"Is the commands speech recognizer available?", UtilsSWA.TYPE_BOOL);
		UtilsSWA.registerValueREGISTRY(K_POCKETSPHINX_RECOG_AVAILABLE, "Speech recognition - Hotword available",
				"Is the hotword speech recognizer (PocketSphinx) available?", UtilsSWA.TYPE_BOOL);
		UtilsSWA.registerValueREGISTRY(K_POCKETSPHINX_REQUEST_STOP, "Speech recognition - Hotword requested to stop",
				"Was the hotword speech recognizer requested to stop?", UtilsSWA.TYPE_BOOL);
	}
}
