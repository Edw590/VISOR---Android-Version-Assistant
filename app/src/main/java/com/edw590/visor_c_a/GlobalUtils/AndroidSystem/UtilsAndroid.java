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

package com.edw590.visor_c_a.GlobalUtils.AndroidSystem;

import android.bluetooth.BluetoothAdapter;
import android.net.wifi.WifiManager;

import androidx.annotation.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>General utilities for the Android system utilities classes.</p>
 */
public final class UtilsAndroid {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsAndroid() {
	}

	public static final int NOT_AVAILABLE = 987673;

	public static final int ALREADY_ENABLED = -987570;
	public static final int ALREADY_ENABLING = -987571;
	public static final int ALREADY_DISABLED = -987572;
	public static final int ALREADY_DISABLING = -987573;

	public static final int MODE_NORMAL = -987470;
	public static final int MODE_SAFE = -987471;
	public static final int MODE_RECOVERY = -987472;
	public static final int MODE_BOOTLOADER = -987473;
	public static final int MODE_FAST = -987484;

	public static final int NO_CALL_EMERGENCY = -987380;
	public static final int NO_CALL_ANY = -987381;


	static final Map<Integer, Integer> map_STATEs_to_consts = new LinkedHashMap<Integer, Integer>() {
		private static final long serialVersionUID = 4089662568764366621L;
		@NonNull
		@Override public LinkedHashMap<Integer, Float> clone() throws AssertionError {
			throw new AssertionError();
		}

		{
			put(WifiManager.WIFI_STATE_ENABLED, ALREADY_ENABLED);
			put(WifiManager.WIFI_STATE_ENABLING, ALREADY_ENABLING);
			put(WifiManager.WIFI_STATE_DISABLED, ALREADY_DISABLED);
			put(WifiManager.WIFI_STATE_DISABLING, ALREADY_DISABLING);

			put(BluetoothAdapter.STATE_ON, ALREADY_ENABLED);
			put(BluetoothAdapter.STATE_TURNING_ON, ALREADY_ENABLING);
			put(BluetoothAdapter.STATE_OFF, ALREADY_DISABLED);
			put(BluetoothAdapter.STATE_TURNING_OFF, ALREADY_DISABLING);
		}
	};
}
