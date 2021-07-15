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

package com.dadi590.assist_c_a.Modules.BatteryProcessor;

import android.content.Intent;
import android.os.BatteryManager;

import androidx.annotation.NonNull;

import com.dadi590.assist_c_a.MainSrv;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;

/**
 * <p>Processes changes in the battery levels or on battery power mode.</p>
 */
public class BatteryProcessor {

	// Minimum and maximum recommended battery percentage values to increase its life
	private static final int PERCENT_MIN = 20;
	private static final int PERCENT_MAX = 80;

	private static final int PERCENT_VERY_LOW = 5 + 1;
	private static final int PERCENT_LOW = PERCENT_MIN + 1;

	private int last_detected_percent = -1;

	/**
	 * <p>Processes changes in the device power mode.</p>
	 *
	 * @param power_connected true if external power was connected, false otherwise
	 */
	public final void processBatteryPwrChg(final boolean power_connected) {
		if (last_detected_percent == -1) {
			return;
		}

		if (power_connected) {
			if (last_detected_percent > PERCENT_MAX) {
				final String speak = "Battery already above " + PERCENT_MAX + "%. Please disconnect the charger.";
				MainSrv.getSpeech2().speak(speak, Speech2.NO_ADDITIONAL_COMMANDS, Speech2.PRIORITY_LOW, null);
			}
		} else {
			if (last_detected_percent < PERCENT_MIN) {
				final String speak = "Battery still below " + PERCENT_MIN + "%. Please reconnect the charger.";
				MainSrv.getSpeech2().speak(speak, Speech2.NO_ADDITIONAL_COMMANDS, Speech2.PRIORITY_LOW, null);
			}
		}
	}

	/**
	 * <p>Processes changes in the battery levels.</p>
	 *
	 * @param intent the intent
	 */
	public final void processBatteryLvlChg(@NonNull final Intent intent) {
		final int battery_status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		final int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		final int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		if (battery_status == -1 || level == -1 || scale == -1) {
			return;
		}

		final int battery_percentage = level * 100 / scale;

		// If the class was just instantiated, store the current battery percentage and wait for the next change.
		if (last_detected_percent == -1) {
			last_detected_percent = battery_percentage;
			return;
		}

		final boolean charger_connected;
		switch (battery_status) {
			case BatteryManager.BATTERY_PLUGGED_AC:
			case BatteryManager.BATTERY_PLUGGED_USB:
			case BatteryManager.BATTERY_PLUGGED_WIRELESS: {
				charger_connected = true;
				break;
			}
			default: {
				charger_connected = false;
				break;
			}
		}
		if (charger_connected) {
			processCharging(battery_percentage);
		} else {
			processDischarging(battery_percentage);
		}

		last_detected_percent = battery_percentage;
	}

	/**
	 * <p>Processes the battery percentage if it's discharging.</p>
	 *
	 * @param battery_percentage the current battery percentage
	 */
	private void processDischarging(final int battery_percentage) {
		// Since I'm putting <= in the if statements, must be from lowest level to the greatest one.
		if (battery_percentage < PERCENT_VERY_LOW && last_detected_percent >= PERCENT_VERY_LOW) {
			// If the battery percentage is VERY_LOW and the last percentage detected is greater than VERY_LOW (meaning
			// it just detected the change), warn about it. If the last detected percentage is less or equal than
			// VERY_LOW, then it already detected and warned about it.
			final String speak = "WARNING! EXTREMELY LOW BATTERY OF " + battery_percentage + "% REACHED! " +
					"Please connect the charger now!";
			MainSrv.getSpeech2().speak(speak, Speech2.NO_ADDITIONAL_COMMANDS, Speech2.PRIORITY_HIGH, null);
		} else if (battery_percentage < PERCENT_LOW && last_detected_percent >= PERCENT_LOW) {
			// Else in the same manner the LOW level.
			final String speak = "ATTENTION! Below " + PERCENT_MIN + "% of battery reached. Please connect " +
					"the charger.";
			MainSrv.getSpeech2().speak(speak, Speech2.NO_ADDITIONAL_COMMANDS, Speech2.PRIORITY_MEDIUM, null);
		}
	}

	/**
	 * <p>Processes the battery percentage if it's charging.</p>
	 *
	 * @param battery_percentage the current battery percentage
	 */
	private void processCharging(final int battery_percentage) {
		// Since I'm putting >= in the if statements, must be from greatest level to the lowest one.
		if (battery_percentage == 100 && last_detected_percent < 100) {
			final String speak = "Attention! Device fully charge! Please disconnect the charger.";
			MainSrv.getSpeech2().speak(speak, Speech2.NO_ADDITIONAL_COMMANDS, Speech2.PRIORITY_LOW, null);
		} else if (battery_percentage > PERCENT_MAX && last_detected_percent <= PERCENT_MAX) {
			final String speak = "Attention! Above " + PERCENT_MAX + "% of battery reached! Please " +
					"disconnect the charger.";
			MainSrv.getSpeech2().speak(speak, Speech2.NO_ADDITIONAL_COMMANDS, Speech2.PRIORITY_MEDIUM, null);
		}
	}
}
