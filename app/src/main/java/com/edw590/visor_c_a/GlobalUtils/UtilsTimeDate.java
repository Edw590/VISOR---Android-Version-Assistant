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

package com.edw590.visor_c_a.GlobalUtils;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * <p>Utilities related to date and time.</p>
 */
public final class UtilsTimeDate {

	public static final String CURRENT_TIME_FORMAT = "HH:mm:ss z";
	public static final String CURRENT_DATE_FORMAT = "EEEE dd-MM-yyyy";

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsTimeDate() {
	}

	/**
	 * <p>Gets the time in a string with the format of {@link #CURRENT_TIME_FORMAT}.</p>
	 *
	 * @param millis the milliseconds for the time or -1 to get the current time
	 *
	 * @return the formatted string
	 */
	@NonNull
	public static String getTimeStr(final long millis) {
		final SimpleDateFormat time = new SimpleDateFormat(CURRENT_TIME_FORMAT, Locale.getDefault());
		time.setTimeZone(TimeZone.getDefault());

		if (millis < 0) {
			return time.format(new Date());
		} else {
			return time.format(new Date(millis));
		}
	}

	/**
	 * <p>Gets the date in a string with the format of {@link #CURRENT_DATE_FORMAT}.</p>
	 *
	 * @param millis the milliseconds for the time or -1 to get the current time
	 *
	 * @return the formatted string
	 */
	@NonNull
	public static String getDateStr(final long millis) {
		// Keep the timezone in English here so he can say the weekday in English.
		final SimpleDateFormat date = new SimpleDateFormat(CURRENT_DATE_FORMAT, Locale.US);
		date.setTimeZone(TimeZone.getDefault());

		if (millis < 0) {
			return date.format(new Date());
		} else {
			return date.format(new Date(millis));
		}
	}

	/**
	 * <p>Gets the time and the date in a string with the format
	 * "{@link #CURRENT_DATE_FORMAT} -- {@link #CURRENT_TIME_FORMAT}".</p>
	 *
	 * @param millis the milliseconds for the time and date or -1 to get the current time and date
	 *
	 * @return the formatted string
	 */
	@NonNull
	public static String getTimeDateStr(final long millis) {
		return UtilsTimeDate.getDateStr(millis) + " -- " + UtilsTimeDate.getTimeStr(millis);
	}
}
