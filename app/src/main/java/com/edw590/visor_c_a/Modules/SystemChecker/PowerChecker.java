/*
 * Copyright 2023-2024 Edw590
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

package com.edw590.visor_c_a.Modules.SystemChecker;

import android.os.BatteryManager;
import android.os.Build;

import androidx.annotation.Nullable;

import com.edw590.visor_c_a.Modules.PreferencesManager.Registry.UtilsRegistry;
import com.edw590.visor_c_a.Modules.PreferencesManager.Registry.ValuesRegistry;
import com.edw590.visor_c_a.Modules.Speech.Speech2;
import com.edw590.visor_c_a.Modules.Speech.UtilsSpeech2BC;

public class PowerChecker {
	class BatteryInfo {
		int battery_percentage = -1;
		boolean battery_present = false;
		boolean power_connected = false;
	}

	final BatteryInfo bat_info = new BatteryInfo();

	// Minimum and maximum recommended battery percentage values to increase its life
	private static final int PERCENT_MIN = 20;
	private static final int PERCENT_MAX = 80;

	private static final int PERCENT_VERY_LOW = 5 + 1;
	private static final int PERCENT_LOW = PERCENT_MIN + 1;

	@Nullable private Boolean last_detected_power_connected = null;
	private int last_detected_percent = -1;
	boolean actions_power_mode_broadcast = false;
	boolean better_battery_present = false;

	/**
	 * <p>Processes changes in the device power mode.</p>
	 *
	 * @param power_connected true if external power was connected, false otherwise
	 */
	void processBatteryPwrChg(final boolean power_connected) {
		// Update the Values Storage
		UtilsRegistry.setValue(ValuesRegistry.Keys.POWER_CONNECTED, power_connected);

		if ((last_detected_percent == -1) ||
				// Only warn if the power state is new (can't warn every percentage increase... - only on power changes)
				(null != last_detected_power_connected && power_connected == last_detected_power_connected)) {
			return;
		}

		if (power_connected) {
			if (last_detected_percent > PERCENT_MAX) {
				final String speak = "Battery already above " + PERCENT_MAX + "%. Please disconnect the charger.";
				UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_LOW, 0, null);
			}
		} else {
			if (last_detected_percent < PERCENT_MIN) {
				final String speak = "Battery still below " + PERCENT_MIN + "%. Please reconnect the charger.";
				UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_LOW, 0, null);
			}
		}

		last_detected_power_connected = power_connected;
	}

	/**
	 * <p>Processes changes in the battery levels.</p>
	 *
	 * @param battery_status the value from {@link BatteryManager#EXTRA_STATUS}
	 * @param battery_lvl the value from {@link BatteryManager#EXTRA_LEVEL}
	 * @param battery_lvl_scale the value from {@link BatteryManager#EXTRA_SCALE}
	 * @param battery_present the value from {@link BatteryManager#EXTRA_STATUS}
	 */
	void processBatteryLvlChg(final int battery_status, final int battery_lvl, final int battery_lvl_scale,
									 @Nullable final Boolean battery_present) {
		// Don't if the EXTRA_STATUS is -1, in case it's wrong (can be - checked on miTab Advance).
		if (battery_lvl == -1 || battery_lvl_scale == -1) {
			return;
		}

		// Don't change the order of the operands below, unless you want to make casts (else, int/int*100 = 0).
		bat_info.battery_percentage = battery_lvl * 100 / battery_lvl_scale;

		// Update the Values Storage
		UtilsRegistry.setValue(ValuesRegistry.Keys.BATTERY_PERCENT, bat_info.battery_percentage);

		// If the EXTRA_PRESENT can be wrong, check if the battery level is different than 0 and 100, depending on the
		// device version, as documented here: https://source.android.com/docs/core/power/batteryless.
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
			better_battery_present = 100 != bat_info.battery_percentage;
		} else {
			better_battery_present = 0 != bat_info.battery_percentage;
		}
		// If better_battery_present is false, no conclusion can be taken, so in that specific case, trust
		// battery_present - unless battery_present is null, and in that case, nothing at all is known.
		if (better_battery_present) {
			// Update the Values Storage
			bat_info.battery_present = true;
			UtilsRegistry.setValue(ValuesRegistry.Keys.BATTERY_PRESENT, true);
		} else if (null != battery_present) {
			// Update the Values Storage
			bat_info.battery_present = battery_present;
			UtilsRegistry.setValue(ValuesRegistry.Keys.BATTERY_PRESENT, battery_present);
		}

		// The ACTION_POWER_CONNECTED and _DISCONNECTED may not be broadcast on the device (happens on miTab Advance),
		// so this is another check for power connected in case those broadcasts are not working (else, use them).
		// Don't use the EXTRA_STATUS. It can be broadcast with a wrong value like the EXTRA_PRESENT can (just checked
		// and there's someone on StackOverflow complaining about being wrong). With the percentages, there's no place
		// for mistakes - battery percentage up, it's charging, else it's discharging.
		// EDIT: never mind, sort of. The charger can be plugged but not supplying enough current to charge the device
		// and the percentage goes down, just slower. So use the percentages only in case the EXTRA_STATUS returns false
		// for all BATTERY_PLUGGED_ constants.
		// EDIT 2: one of the BATTERY_PLUGGED_ constants is returning true on miTab Advance, probably the _AC one, even
		// with no charger connected. Plan B: I'll assume these poor implementations were only below Lollipop, I guess
		// (it's much more advanced than KitKat, maybe poor implementations stopped(?)), and I'll AND it with with
		// checking if the ACTION_POWER_ broadcasts are sent or not.
		//
		// Aside from this, DO NOT DELETE THIS!!!!! It's still useful!!! If app just started and the charger is not
		// plugged for the entire day, no way of knowing it's plugged or not until the ACTION_POWER_ are broadcast! With
		// this here, a change in battery is enough. After that first change, go only with the ACTION_POWER_ broadcasts.
		if (!actions_power_mode_broadcast) {
			@Nullable final Boolean power_connected_not_ready;
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
				// Compare the percentages
				if (last_detected_percent == -1) {
					// No way of knowing - don't update the value.
					power_connected_not_ready = null;
				} else {
					if (bat_info.battery_percentage == last_detected_percent) {
						// No way of knowing anything if the percentage didn't change (no idea if the broadcast is sent
						// or not without the battery level changing, so keep this here).
						power_connected_not_ready = null;
					} else {
						power_connected_not_ready = bat_info.battery_percentage > last_detected_percent;
					}
				}
			} else {
				// Check the EXTRA_STATUS
				switch (battery_status) {
					case (BatteryManager.BATTERY_PLUGGED_ANY):
					case (BatteryManager.BATTERY_PLUGGED_AC):
					case (BatteryManager.BATTERY_PLUGGED_USB):
					case (BatteryManager.BATTERY_PLUGGED_WIRELESS): {
						power_connected_not_ready = true;

						break;
					}
					default: {
						power_connected_not_ready = false;

						break;
					}
				}
			}

			if (null != power_connected_not_ready) {
				// Update the Values Storage
				bat_info.power_connected = power_connected_not_ready;
				UtilsRegistry.setValue(ValuesRegistry.Keys.POWER_CONNECTED, power_connected_not_ready);
				processBatteryPwrChg(power_connected_not_ready);
			}
		}

		// If the module just started, store the current battery percentage and wait for the next change.
		if (last_detected_percent == -1) {
			last_detected_percent = bat_info.battery_percentage;
			return;
		}

		// Instead of checking with power_connected, this has direct connection with the percentages, since the
		// functions will warn based on them only --> then compare the values and be done with it.
		// Still, use the power_connected status, but only if it's different than null, just to be sure no weird devices
		// increase the percentage by mistake without being charging. This would warn in that case - now it won't.
		final Boolean power_connected = UtilsRegistry.getValue(ValuesRegistry.Keys.POWER_CONNECTED).getData();
		if (power_connected == null || power_connected) {
			// Don't do anything if by chance, another broadcast is sent without the battery level having changed.
			if (bat_info.battery_percentage > last_detected_percent) {
				warnCharging(bat_info.battery_percentage);
			} else if (bat_info.battery_percentage < last_detected_percent) {
				warnDischarging(bat_info.battery_percentage);
			}
		}

		last_detected_percent = bat_info.battery_percentage;
	}

	/**
	 * <p>Processes the battery percentage if it's charging.</p>
	 *
	 * @param battery_percentage the current battery percentage
	 */
	private void warnCharging(final int battery_percentage) {
		// Since I'm putting >= in the if statements, must be from greatest level to the lowest one.
		if (battery_percentage == 100 && last_detected_percent < 100) {
			final String speak = "Attention! Device fully charge! Please disconnect the charger.";
			UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_LOW, 0, null);
		} else if (battery_percentage > PERCENT_MAX && last_detected_percent <= PERCENT_MAX) {
			final String speak = "Attention! Above " + PERCENT_MAX + "% of battery reached! Please " +
					"disconnect the charger.";
			UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_MEDIUM, 0, null);
		}
	}

	/**
	 * <p>Processes the battery percentage if it's discharging.</p>
	 *
	 * @param battery_percentage the current battery percentage
	 */
	private void warnDischarging(final int battery_percentage) {
		// Since I'm putting <= in the if statements, must be from lowest level to the greatest one.
		if (battery_percentage < PERCENT_VERY_LOW && last_detected_percent >= PERCENT_VERY_LOW) {
			// If the battery percentage is VERY_LOW and the last percentage detected is greater than VERY_LOW (meaning
			// it just detected the change), warn about it. If the last detected percentage is less or equal than
			// VERY_LOW, then it already detected and warned about it.
			final String speak = "WARNING! EXTREMELY LOW BATTERY OF " + battery_percentage + "% REACHED! " +
					"Please connect the charger now!";
			UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, 0, null);
		} else if (battery_percentage < PERCENT_LOW && last_detected_percent >= PERCENT_LOW) {
			// Else in the same manner the LOW level.
			final String speak = "ATTENTION! Below " + PERCENT_MIN + "% of battery reached. Please connect " +
					"the charger.";
			UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_MEDIUM, 0, null);
		}
	}
}
