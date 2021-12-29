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

import com.dadi590.assist_c_a.GlobalUtils.UtilsApp;

/**
 * <p>Functions to call to send information to {@link BatteryProcessor}, by using broadcasts.</p>
 */
public final class UtilsBatteryProcessorBC {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsBatteryProcessorBC() {
	}

	/**
	 * <p>Broadcasts a request to execute {@link BatteryProcessor#processBatteryPwrChg(boolean)}.</p>
	 *
	 * @param power_connected same as in the mentioned function
	 */
	public static void processBatteryPwrChg(final boolean power_connected) {
		final Intent broadcast_intent = new Intent(CONSTS_BC.ACTION_PROCESS_PWR_CHG);
		broadcast_intent.putExtra(CONSTS_BC.EXTRA_PROCESS_PWR_CHG_1, power_connected);

		UtilsApp.sendInternalBroadcast(broadcast_intent);
	}

	/**
	 * <p>Broadcasts a request to execute {@link BatteryProcessor#processBatteryLvlChg(int, int, int)}.</p>
	 *
	 * @param battery_status same as in the mentioned function
	 * @param battery_lvl same as in the mentioned function
	 * @param battery_lvl_scale same as in the mentioned function
	 */
	public static void processBatteryLvlChg(final int battery_status, final int battery_lvl, final int battery_lvl_scale) {
		final Intent broadcast_intent = new Intent(CONSTS_BC.ACTION_PROCESS_LVL_CHG);
		broadcast_intent.putExtra(CONSTS_BC.EXTRA_PROCESS_LVL_CHG_1, battery_status);
		broadcast_intent.putExtra(CONSTS_BC.EXTRA_PROCESS_LVL_CHG_2, battery_lvl);
		broadcast_intent.putExtra(CONSTS_BC.EXTRA_PROCESS_LVL_CHG_3, battery_lvl_scale);

		UtilsApp.sendInternalBroadcast(broadcast_intent);
	}
}
