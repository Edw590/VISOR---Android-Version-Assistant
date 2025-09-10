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

package com.edw590.visor_c_a.Registry;

import UtilsSWA.UtilsSWA;

/**
 * <p>The static storage of all app global values.</p>
 */
public final class RegistryKeys {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private RegistryKeys() {
	}

	private static final String SETTINGS_PREFIX = "MANUAL_";

	/////////////////////////////////////////////////////////////////////////////
	// Automatic values

	// System Checker
	/** Type: boolean. */
	public static final String K_BATTERY_PRESENT = "BATTERY_PRESENT";
	/** Type: int. */
	public static final String K_BATTERY_LEVEL = "BATTERY_LEVEL";
	/** Type: boolean. */
	public static final String K_POWER_CONNECTED = "POWER_CONNECTED";
	/** Type: int. */
	public static final String K_DIST_ROUTER = "DIST_ROUTER";
	/** Type: int. */
	public static final String K_CURR_NETWORK_TYPE = "CURR_NETWORK_TYPE";
	/** Type: boolean. */
	public static final String K_AIRPLANE_MODE_ON = "AIRPLANE_MODE_ON";
	/** Type: int. */
	public static final String K_SCREEN_BRIGHTNESS = "SCREEN_BRIGHTNESS";
	/** Type: int. */
	public static final String K_SOUND_VOLUME = "SOUND_VOLUME";
	/** Type: boolean. */
	public static final String K_SOUND_MUTED = "SOUND_MUTED";
	/** Type: boolean. */
	public static final String K_DEVICE_IN_USE = "DEVICE_IN_USE";

	// Telephony - Phone calls
	/** Type: long. */
	public static final String K_LAST_PHONE_CALL_TIME = "LAST_PHONE_CALL_TIME";
	/** Type: String. */
	public static final String K_CURR_PHONE_CALL_NUMBER = "CURR_PHONE_CALL_NUMBER";

	// Telephony - SMS
	/** Type: long. */
	public static final String K_LAST_SMS_MSG_TIME = "LAST_SMS_MSG_TIME";
	/** Type: String. */
	public static final String K_LAST_SMS_MSG_NUMBER = "LAST_SMS_MSG_NUMBER";

	// Audio Recorder
	/** Type: boolean. */
	public static final String K_IS_RECORDING_AUDIO_INTERNALLY = "IS_RECORDING_AUDIO_INTERNALLY";

	// Screen Recorder
	/** Type: boolean. */
	public static final String K_IS_RECORDING_SCREEN_INTERNALLY = "IS_RECORDING_SCREEN_INTERNALLY";

	// Flashlight
	/** Type: boolean. */
	public static final String K_MAIN_FLASHLIGHT_ENABLED = "MAIN_FLASHLIGHT_ENABLED";

	// Speech recognizers
	/** Type: boolean. */
	public static final String K_COMMANDS_RECOG_AVAILABLE = "COMMANDS_RECOG_AVAILABLE";
	/** Type: boolean. */
	public static final String K_POCKETSPHINX_RECOG_AVAILABLE = "POCKETSPHINX_RECOG_AVAILABLE";
	/** Type: boolean. */
	public static final String K_POCKETSPHINX_REQUEST_STOP = "POCKETSPHINX_RECOG_STOPPED";

	/////////////////////////////////////////////////////////////////////////////
	// Manual values

	// Telephony
	/** Type: boolean. */
	public static final String K_CONTACTS_1ST_MATCH = SETTINGS_PREFIX + "CONTACTS_1ST_MATCH";
	/** Type: boolean. */
	public static final String K_CONTACTS_SIM_ONLY = SETTINGS_PREFIX + "CONTACTS_SIM_ONLY";

	// Speech
	/** Type: int. */
	public static final String K_SPEECH_NORMAL_VOL = SETTINGS_PREFIX + "SPEECH_NORMAL_VOL";
	/** Type: int. */
	public static final String K_SPEECH_CRITICAL_VOL = SETTINGS_PREFIX + "SPEECH_CRITICAL_VOL";
	/** Type: boolean. */
	public static final String K_SPEECH_ALWAYS_NOTIFY = SETTINGS_PREFIX + "SPEECH_ALWAYS_NOTIFY";

	// Permissions and authorizations
	/** Type: boolean. */
	public static final String K_PERMS_AUTHS_FORCE_ALL = SETTINGS_PREFIX + "PERMS_AUTHS_KEEP_FORCING";

	// Note: if the value is not being updated, remove it from the list

	/**
	 * <p>Registers all the keys in the registry.</p>
	 */
	public static void registerValues() {
		/////////////////////////////////////////////
		// Automatic values

		// System Checker
		UtilsSWA.registerValueREGISTRY(K_BATTERY_PRESENT, "System Checker - Battery Present",
				"Is the battery present?", UtilsSWA.TYPE_BOOL, "", true);
		UtilsSWA.registerValueREGISTRY(K_BATTERY_LEVEL, "System Checker - Battery Level",
				"The battery level", UtilsSWA.TYPE_INT, "", true);
		UtilsSWA.registerValueREGISTRY(K_POWER_CONNECTED, "System Checker - Power Connected",
				"Is the device connected to power?", UtilsSWA.TYPE_BOOL, "", true);
		UtilsSWA.registerValueREGISTRY(K_AIRPLANE_MODE_ON, "System Checker - Airplane mode On",
				"Whether the airplane mode is On", UtilsSWA.TYPE_BOOL, "", true);
		UtilsSWA.registerValueREGISTRY(K_CURR_NETWORK_TYPE, "System Checker - Current network type",
				"The current network type", UtilsSWA.TYPE_INT, "", true);
		UtilsSWA.registerValueREGISTRY(K_SCREEN_BRIGHTNESS, "System Checker - Screen Brightness",
				"The screen brightness", UtilsSWA.TYPE_INT, "", true);
		UtilsSWA.registerValueREGISTRY(K_SOUND_VOLUME, "System Checker - Sound Volume",
				"The sound volume", UtilsSWA.TYPE_INT, "", true);
		UtilsSWA.registerValueREGISTRY(K_SOUND_MUTED, "System Checker - Sound Muted",
				"Whether the sound is muted", UtilsSWA.TYPE_BOOL, "", true);
		UtilsSWA.registerValueREGISTRY(K_DEVICE_IN_USE, "System Checker - Device In Use",
				"Whether the device is being used", UtilsSWA.TYPE_BOOL, "", true);
		UtilsSWA.registerValueREGISTRY(K_DIST_ROUTER, "Location - Distance to Wi-Fi router (m)",
				"Distance in meters from the device to the Wi-Fi router of the current network", UtilsSWA.TYPE_INT, "",
				true);

		// Telephony - Phone calls
		UtilsSWA.registerValueREGISTRY(K_LAST_PHONE_CALL_TIME, "Telephony - Last call when (ms)",
				"Timestamp of the last phone call (in milliseconds)", UtilsSWA.TYPE_LONG, "", true);
		UtilsSWA.registerValueREGISTRY(K_CURR_PHONE_CALL_NUMBER, "Telephony - Number of current call",
				"Number of the last phone call", UtilsSWA.TYPE_STRING, "", true);

		// Telephony - SMS
		UtilsSWA.registerValueREGISTRY(K_LAST_SMS_MSG_TIME, "Telephony - Last SMS msg when (ms)",
				"Timestamp of the last SMS message (in milliseconds)", UtilsSWA.TYPE_LONG, "", true);
		UtilsSWA.registerValueREGISTRY(K_LAST_SMS_MSG_NUMBER, "Telephony - Number of last SMS msg sender",
				"Number of the last SMS message", UtilsSWA.TYPE_STRING, "", true);

		// Audio Recorder
		UtilsSWA.registerValueREGISTRY(K_IS_RECORDING_AUDIO_INTERNALLY, "Audio Recorder - Recording internally",
				"Is VISOR recording audio internally?", UtilsSWA.TYPE_BOOL, "", true);

		// Screen Recorder
		UtilsSWA.registerValueREGISTRY(K_IS_RECORDING_SCREEN_INTERNALLY, "Screen Recorder - Recording internally",
				"Is VISOR recording the screen internally?", UtilsSWA.TYPE_BOOL, "", true);

		// Flashlight
		UtilsSWA.registerValueREGISTRY(K_MAIN_FLASHLIGHT_ENABLED, "Camera - Main flashlight enabled (Android 6+)",
				"Is the main flashlight enabled? (Only available from Android Marshmallow onwards)", UtilsSWA.TYPE_BOOL,
				"", true);

		// Speech recognizers
		UtilsSWA.registerValueREGISTRY(K_COMMANDS_RECOG_AVAILABLE, "Speech recognition - Commands available",
				"Is the commands speech recognizer available?", UtilsSWA.TYPE_BOOL, "", true);
		UtilsSWA.registerValueREGISTRY(K_POCKETSPHINX_RECOG_AVAILABLE, "Speech recognition - Hotword available",
				"Is the hotword speech recognizer (PocketSphinx) available?", UtilsSWA.TYPE_BOOL, "", true);
		UtilsSWA.registerValueREGISTRY(K_POCKETSPHINX_REQUEST_STOP, "Speech recognition - Hotword requested to stop",
				"Was the hotword speech recognizer requested to stop?", UtilsSWA.TYPE_BOOL, "", true);

		/////////////////////////////////////////////
		// Manual values

		// Telephony
		UtilsSWA.registerValueREGISTRY(K_CONTACTS_1ST_MATCH, "Contacts - Use 1st name match",
				"Use the 1st match on the contacts when getting the name from a phone number (or else warn about multiple matches)",
				UtilsSWA.TYPE_BOOL, "true", false);
		UtilsSWA.registerValueREGISTRY(K_CONTACTS_SIM_ONLY, "Contacts - Only use SIM contacts",
				"Search only the SIM card contacts", UtilsSWA.TYPE_BOOL, "false", false);

		// Speech
		UtilsSWA.registerValueREGISTRY(K_SPEECH_NORMAL_VOL, "Speech - Normal speech volume",
				"The volume at which to speak non-critical speeches", UtilsSWA.TYPE_INT, "50", false);
		UtilsSWA.registerValueREGISTRY(K_SPEECH_CRITICAL_VOL, "Speech - Critical speech volume",
				"The volume at which to speak critical speeches", UtilsSWA.TYPE_INT, "100", false);
		UtilsSWA.registerValueREGISTRY(K_SPEECH_ALWAYS_NOTIFY, "Speech - Always notify",
				"Always notify speeches", UtilsSWA.TYPE_BOOL, "false", false);

		// Permissions and authorizations
		UtilsSWA.registerValueREGISTRY(K_PERMS_AUTHS_FORCE_ALL, "Permissions and authorizations - Force all",
				"Keep forcing all app permissions and authorizations", UtilsSWA.TYPE_BOOL, "false", false);


		/////////////////////////////////////////////
		// Clean the registry

		UtilsSWA.cleanRegistryREGISTRY();
	}
}
