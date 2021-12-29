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

package com.dadi590.assist_c_a.ValuesStorage;

/**
 * <p>Constants related to the Values Storage module.</p>
 * <p>To know the type of the value, go to {@link ValuesStorage} and check the TYPE_-started constant on the wanted
 * value.</p>
 */
public final class CONSTS {

	/////////////////////////////////////////////////////////////////

	// Battery
	public static final String battery_percentage = "battery_percentage";
	public static final String power_connected = "power_connected";

	// Telephony - Phone calls
	public static final String last_phone_call_time = "last_phone_call_time";
	public static final String curr_phone_call_number = "curr_phone_call_number";
	// Telephony - SMS
	public static final String last_sms_msg_time = "last_sms_msg_time";
	public static final String last_sms_msg_number = "last_sms_msg_number";

	// Date and time
	public static final String current_time = "current_time";
	public static final String current_date = "current_date";

	// Weather
	public static final String loc_temp_for_the_day_c = "current_loc_temp_c";
	public static final String loc_temp_for_the_day_f = "current_loc_temp_f";
	public static final String loc_weather_for_the_day = "loc_weather_for_the_day";

	// Audio Recorder
	public static final String is_recording_audio = "is_recording_audio";

	// Flashlight
	public static final String main_flashlight_enabled = "main_flashlight_enabled";

	/////////////////////////////////////////////////////////////////

	public static final String CURRENT_TIME_FORMAT = "HH:mm:ss z";
	public static final String CURRENT_DATE_FORMAT = "EEEE dd-MM-yyyy";

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private CONSTS() {
	}
}
