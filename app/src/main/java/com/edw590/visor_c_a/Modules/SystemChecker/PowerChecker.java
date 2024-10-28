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

package com.edw590.visor_c_a.Modules.SystemChecker;

import android.os.BatteryManager;
import android.os.Build;

import androidx.annotation.Nullable;

import com.edw590.visor_c_a.Registry.RegistryKeys;
import com.edw590.visor_c_a.Registry.UtilsRegistry;

public class PowerChecker {
	class BatteryInfo {
		int battery_percentage = -1;
		boolean battery_present = false;
		boolean power_connected = false;
	}

	final BatteryInfo bat_info = new BatteryInfo();

	private int last_detected_percent = -1;
	boolean actions_power_mode_broadcast = false;

	/**
	 * <p>Processes changes in the device power mode.</p>
	 *
	 * @param power_connected true if external power was connected, false otherwise
	 */
	static void processBatteryPwrChg(final boolean power_connected) {
		UtilsRegistry.setData(RegistryKeys.K_POWER_CONNECTED, power_connected, false);
	}

	/**
	 * <p>Processes changes in the battery levels.</p>
	 *
	 * @param battery_status the value from {@link BatteryManager#EXTRA_STATUS}
	 * @param battery_lvl the value from {@link BatteryManager#EXTRA_LEVEL}
	 * @param battery_lvl_scale the value from {@link BatteryManager#EXTRA_SCALE}
	 * @param battery_present the value from {@link BatteryManager#EXTRA_STATUS}
	 */
	final void processBatteryLvlChg(final int battery_status, final int battery_lvl, final int battery_lvl_scale,
									@Nullable final Boolean battery_present) {
		// Don't if the EXTRA_STATUS is -1, in case it's wrong (can be - checked on miTab Advance).
		if (battery_lvl == -1 || battery_lvl_scale == -1) {
			return;
		}

		// Don't change the order of the operands below, unless you want to make casts (else, int/int*100 = 0).
		bat_info.battery_percentage = battery_lvl * 100 / battery_lvl_scale;

		// Update the Values Storage
		UtilsRegistry.setData(RegistryKeys.K_BATTERY_LEVEL, bat_info.battery_percentage, false);

		// If the EXTRA_PRESENT can be wrong, check if the battery level is different than 0 and 100, depending on the
		// device version, as documented here: https://source.android.com/docs/core/power/batteryless.
		boolean better_battery_present;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
			better_battery_present = bat_info.battery_percentage != 100;
		} else {
			better_battery_present = bat_info.battery_percentage != 0;
		}
		// If better_battery_present is false, no conclusion can be taken, so in that specific case, trust
		// battery_present - unless battery_present is null, and in that case, nothing at all is known.
		if (better_battery_present) {
			// Update the Values Storage
			bat_info.battery_present = true;
			UtilsRegistry.setData(RegistryKeys.K_BATTERY_PRESENT, true, false);
		} else if (battery_present != null) {
			// Update the Values Storage
			bat_info.battery_present = battery_present;
			UtilsRegistry.setData(RegistryKeys.K_BATTERY_PRESENT, battery_present, false);
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

			if (power_connected_not_ready != null) {
				// Update the Values Storage
				bat_info.power_connected = power_connected_not_ready;
				UtilsRegistry.setData(RegistryKeys.K_POWER_CONNECTED, power_connected_not_ready, false);
				processBatteryPwrChg(power_connected_not_ready);
			}
		}

		last_detected_percent = bat_info.battery_percentage;
	}
}
