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

package com.dadi590.assist_c_a.ValuesStorage;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.dadi590.assist_c_a.GlobalUtils.UtilsApp;

/**
 * <p>The class where all global values are stored statically.</p>
 * <p>A null value means the value was not updated since the app startup.</p>
 */
public final class ValuesStorage<ValueType> {

	public static class Keys {

		// Add new keys to the values_list array below too.

		// Power Processor
		public static final String battery_present = "battery_present";
		public static final String battery_percent = "battery_percent";
		public static final String power_connected = "power_connected";
		// Device Locator
		public static final String dist_router = "dist_router_good";
		public static final String public_ip = "public_ip";
		public static final String curr_network_type = "curr_network_type";
		// Telephony - Phone calls
		public static final String last_phone_call_time = "last_phone_call_time";
		public static final String curr_phone_call_number = "curr_phone_call_number";
		// Telephony - SMS
		public static final String last_sms_msg_time = "last_sms_msg_time";
		public static final String last_sms_msg_number = "last_sms_msg_number";
		// Audio Recorder
		public static final String is_recording_audio_internally = "is_recording_audio_internally";
		// Flashlight
		public static final String main_flashlight_enabled = "main_flashlight_enabled";
		// Speech recognizers
		public static final String google_recog_available = "google_recog_available";
		public static final String pocketsphinx_recog_available = "pocketsphinx_recog_available";
	}

	// Only broadcast types are allowed so that the new values can be broadcast.
	private static final Class<?> TYPE_BOOLEAN = boolean.class;
	private static final Class<?> TYPE_INT = int.class;
	private static final Class<?> TYPE_DOUBLE = double.class;
	private static final Class<?> TYPE_LONG = long.class;
	private static final Class<?> TYPE_STRING = String.class;
	private static final ValueObj[] values_list = {
			// Note: if the value is not being updated, remove it from the list

			new ValueObj(Keys.battery_present, "Power - Battery Present", TYPE_BOOLEAN),
			new ValueObj(Keys.battery_percent, "Power - Battery Percentage", TYPE_INT),
			new ValueObj(Keys.power_connected, "Power - Power Connected", TYPE_BOOLEAN),
			// Device Locator
			new ValueObj(Keys.dist_router, "Location - Distance to Wi-Fi router (m)", TYPE_INT),
			new ValueObj(Keys.public_ip, "Location - Public IP", TYPE_STRING),
			new ValueObj(Keys.curr_network_type, "Location - Current network type", TYPE_INT),
			// Telephony - Phone calls
			new ValueObj(Keys.last_phone_call_time, "Telephony - Last call when (ms)", TYPE_LONG),
			new ValueObj(Keys.curr_phone_call_number, "Telephony - Number of current call", TYPE_STRING),
			// Telephony - SMS
			new ValueObj(Keys.last_sms_msg_time, "Telephony - Last SMS msg when (ms)", TYPE_LONG),
			new ValueObj(Keys.last_sms_msg_number, "Telephony - Number of last SMS msg sender", TYPE_STRING),
			// Audio Recorder
			new ValueObj(Keys.is_recording_audio_internally, "Audio Recorder - Recording internally", TYPE_BOOLEAN),
			// Flashlight
			new ValueObj(Keys.main_flashlight_enabled, "Camera - Main flashlight enabled (Android 6+)", TYPE_BOOLEAN),
			// Speech Recognizers
			new ValueObj(Keys.google_recog_available, "Speech recognition - Google available", TYPE_BOOLEAN),
			new ValueObj(Keys.pocketsphinx_recog_available, "Speech recognition - PocketSphinx available", TYPE_BOOLEAN),
	};

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private ValuesStorage() {
	}

	/**
	 * <p>Update the value of the given key.</p>
	 * <p>Don't forget the key must already exist on the list (must be hard-coded).</p>
	 * <br>
	 * <p>This function also broadcasts the new value through the app with an app-internal broadcast.</p>
	 *
	 * @param key the key
	 * @param new_value the new value
	 */
	public static void setValue(@NonNull final String key, @NonNull final Object new_value) {
		for (final ValueObj valueObj : values_list) {
			if (key.equals(valueObj.key)) {
				valueObj.setValue(new_value);

				final Intent intent = new Intent(CONSTS_BC_ValuesStorage.ACTION_VALUE_UPDATED);
				intent.putExtra(CONSTS_BC_ValuesStorage.EXTRA_VALUE_UPDATED_1, key);
				UtilsApp.sendInternalBroadcast(intent);

				break;
			}
		}
	}

	/**
	 * <p>Returns the {@link ValueObj} stored for the given key.</p>
	 *
	 * @param key the key associated with the wanted value
	 * @param def_value the value to return if the value for the given key is null (meaning, it has not yet been set
	 * since the app startup)
	 *
	 * @return the value for the given key, in the appropriate type, or the default value
	 */
	@NonNull
	public static ValueObj getValueObj(@NonNull final String key) {
		for (final ValueObj value : values_list) {
			if (value.key.equals(key)) {
				return value;
			}
		}

		// Won't get here. Just request a valid key.
		return null;
	}


		/**
		 * <p>Get a clone of {@link #values_list}.</p>
		 *
		 * @return .
		 */
	@NonNull
	public static ValueObj[] getValuesArrays() {
		return values_list.clone();
	}
}
