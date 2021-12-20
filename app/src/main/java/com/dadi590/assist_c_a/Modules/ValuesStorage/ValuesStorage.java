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

package com.dadi590.assist_c_a.Modules.ValuesStorage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * <p>The class where all public values are stored.</p>
 */
public final class ValuesStorage {

	public static final String UNDEFINED_VALUE = null;

	// If a new one is added below, add it on the function that returns the values too!
	public static final String TYPE_BOOLEAN = "TYPE_BOOLEAN";
	public static final String TYPE_INTEGER = "TYPE_INTEGER";
	public static final String TYPE_DOUBLE = "TYPE_DOUBLE";
	public static final String TYPE_LONG = "TYPE_LONG";
	public static final String TYPE_STRING = "TYPE_STRING";
	private static final String[][] values_arrays = {
			// Battery
			{CONSTS.battery_percentage, "Battery - Battery Percentage", TYPE_INTEGER, UNDEFINED_VALUE}, // Updating
			{CONSTS.power_connected, "Battery - Power Connected", TYPE_BOOLEAN, UNDEFINED_VALUE}, // Updating
			// Telephony - Phone calls
			{CONSTS.last_phone_call_time, "Telephony - Time of last call (milliseconds)", TYPE_LONG, UNDEFINED_VALUE}, // Updating
			{CONSTS.curr_phone_call_number, "Telephony - Number of current call", TYPE_STRING, UNDEFINED_VALUE}, // Updating
			// Telephony - SMS
			{CONSTS.last_sms_msg_time, "Telephony - Time of last SMS msg (milliseconds)", TYPE_LONG, UNDEFINED_VALUE}, // Updating
			{CONSTS.last_sms_msg_number, "Telephony - Number of last SMS msg sender", TYPE_STRING, UNDEFINED_VALUE}, // Updating
			// Date and time
			{CONSTS.current_time, "Current time (" + CONSTS.CURRENT_TIME_FORMAT + ")", TYPE_STRING, UNDEFINED_VALUE}, // Updating
			{CONSTS.current_date, "Current date (" + CONSTS.CURRENT_DATE_FORMAT + ")", TYPE_STRING, UNDEFINED_VALUE}, // Updating
			// Weather
			{CONSTS.loc_temp_for_the_day_c, "Location temperature for the day (C)", TYPE_DOUBLE, UNDEFINED_VALUE},
			{CONSTS.loc_temp_for_the_day_f, "Location temperature for the day (F)", TYPE_DOUBLE, UNDEFINED_VALUE},
			{CONSTS.loc_weather_for_the_day, "Location weather for the day", TYPE_STRING, UNDEFINED_VALUE},
			// AudioRecorder
			{CONSTS.recording_audio, "Recording audio", TYPE_BOOLEAN, UNDEFINED_VALUE}, // Updating
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
	public static void updateValue(@NonNull final String key, @Nullable final String new_value) {
		for (int i = 0, length = values_arrays.length; i < length; ++i) {
			if (values_arrays[i][0].equals(key)) {
				values_arrays[i][3] = new_value;
			}
		}
	}

	/**
	 * <p>Returns the value stored for the given key, in the correct type (check the type on the list of values).</p>
	 * <p>If the value (always stored as a {@link String}) stored for the given key came from a {@code boolean}, this
	 * function will return the value as a {@code boolean}, as it was original.</p>
	 *
	 * @param key the key associated with the wanted value
	 *
	 * @return the value for the given key, in the appropriate type; null in case the key does not have a value set
	 */
	@Nullable
	public static Object getValue(@NonNull final String key) {
		for (final String[] value_array : values_arrays) {
			if (value_array[0].equals(key)) {
				final String value = value_array[3];
				if (value == UNDEFINED_VALUE) {
					return UNDEFINED_VALUE;
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

		return "Never happening - just don't be stupid and put everything right.";
	}

	/* *
	 * <p>Get a clone of {@link #values_arrays}.</p>
	 *
	 * @return .
	 */
	/*
	Unused - if you change the format of the array, this way only this class must be updated
	@NonNull
	public static String[][] getValuesArrays() {
		return values_arrays.clone();
	}*/
}
