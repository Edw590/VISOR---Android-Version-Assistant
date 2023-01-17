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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * <p>The class where all global values are stored statically.</p>
 * <p>A null value means the value was not updated since the app startup.</p>
 */
public final class ValuesStorage {

	// If a new one is added below, add it on the function that returns the values too!
	public static final String TYPE_BOOLEAN = "TYPE_BOOLEAN";
	public static final String TYPE_INTEGER = "TYPE_INTEGER";
	public static final String TYPE_DOUBLE = "TYPE_DOUBLE";
	public static final String TYPE_LONG = "TYPE_LONG";
	public static final String TYPE_STRING = "TYPE_STRING";
	private static final String[][] values_list = {
			// Note: the "Updating" means the value is currently being updated in some class, as opposite to no class at
			// all being updating the value (and is therefore waiting to be used).
			// Power
			{CONSTS_ValueStorage.battery_present, "Power - Battery Present", TYPE_BOOLEAN, null}, // Updating
			{CONSTS_ValueStorage.battery_percentage, "Power - Battery Percentage", TYPE_INTEGER, null}, // Updating
			{CONSTS_ValueStorage.power_connected, "Power - Power Connected", TYPE_BOOLEAN, null}, // Updating
			// Telephony - Phone calls
			{CONSTS_ValueStorage.last_phone_call_time, "Telephony - Last call when (ms)", TYPE_LONG, null}, // Updating
			{CONSTS_ValueStorage.curr_phone_call_number, "Telephony - Number of current call", TYPE_STRING, null}, // Updating
			// Telephony - SMS
			{CONSTS_ValueStorage.last_sms_msg_time, "Telephony - Last SMS msg when (ms)", TYPE_LONG, null}, // Updating
			{CONSTS_ValueStorage.last_sms_msg_number, "Telephony - Number of last SMS msg sender", TYPE_STRING, null}, // Updating
			// Weather
			{CONSTS_ValueStorage.loc_temp_for_the_day_c, "Location temperature for the day (C)", TYPE_DOUBLE, null},
			{CONSTS_ValueStorage.loc_temp_for_the_day_f, "Location temperature for the day (F)", TYPE_DOUBLE, null},
			{CONSTS_ValueStorage.loc_weather_for_the_day, "Location weather for the day", TYPE_STRING, null},
			// AudioRecorder
			{CONSTS_ValueStorage.is_recording_audio_internally, "Recording audio internally", TYPE_BOOLEAN, null}, // Updating
			// Flashlight
			{CONSTS_ValueStorage.main_flashlight_enabled, "Main flashlight enabled (Android 6+)", TYPE_BOOLEAN, null}, // Updating
			// Speech Recognizers
			{CONSTS_ValueStorage.google_recog_available, "Google speech recognition available", TYPE_BOOLEAN, null}, // Updating
			{CONSTS_ValueStorage.pocketsphinx_recog_available, "PocketSphinx speech recognition available", TYPE_BOOLEAN, null}, // Updating
	};

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private ValuesStorage() {
	}

	/**
	 * <p>Update the value of the given key.</p>
	 * <p>Don't forget the key must already exist on the list (must be hard-coded).</p>
	 *
	 * @param key the key
	 * @param new_value the new value
	 */
	public static void updateValue(@NonNull final String key, @NonNull final String new_value) {
		for (int i = 0, length = values_list.length; i < length; ++i) {
			if (values_list[i][0].equals(key)) {
				values_list[i][3] = new_value;

				break;
			}
		}
	}

	/**
	 * <p>Returns the value stored for the given key, in the correct type (check the type on the list of values).</p>
	 * <p>If the value (always stored as a {@link String}) stored for the given key came from a {@code boolean}, this
	 * function will return the value as a {@code boolean}, as it was originally, so just cast it and all is good.</p>
	 *
	 * @param key the key associated with the wanted value
	 *
	 * @return the value for the given key, in the appropriate type; null in case the key does not have a value set yet
	 */
	@Nullable
	public static Object getValue(@NonNull final String key) {
		for (final String[] value_array : values_list) {
			if (value_array[0].equals(key)) {
				final String value = value_array[3];
				if (null == value) {
					return null;
				}

				switch (value_array[2]) {
					case (TYPE_BOOLEAN): {
						return Boolean.parseBoolean(value);
					}
					case (TYPE_INTEGER): {
						return Integer.parseInt(value);
					}
					case (TYPE_DOUBLE): {
						return Double.parseDouble(value);
					}
					case (TYPE_LONG): {
						return Long.parseLong(value);
					}
					case (TYPE_STRING): {
						return value;
					}
				}
			}
		}

		return "Never happening - just don't be dumb and put everything right.";
	}

	/**
	 * <p>Get a clone of {@link #values_list}.</p>
	 *
	 * @return .
	 */
	@NonNull
	public static String[][] getValuesArrays() {
		return values_list.clone();
	}
}
