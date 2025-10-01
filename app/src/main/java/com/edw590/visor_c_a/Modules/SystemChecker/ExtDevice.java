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

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

/**
 * <p>The class to instantiate for each detected device (external devices).</p>
 */
public final class ExtDevice {
	static final int TYPE_BLUETOOTH = 0;
	static final int TYPE_WIFI = 1;

	/** Type of the device (one of the {@code TYPE_} constants). */
	public final int type;
	/** Unique address of the device (MAC, BSSID...), in upper case letters. */
	@NonNull public final String address;

	/** Milliseconds at the time of detection. */
	long last_detection;
	/** RSSI value of the device (like the Bluetooth RSSI or the WiFi RSSI). */
	int rssi;
	/** Name of the device (like the Bluetooth device name or the WiFi network SSID). */
	@NonNull String name;
	/** In case of Bluetooth, return value of {@link BluetoothDevice#getAlias()}, in other cases where this doesn't
	 * apply, the same as the {@code name} parameter. */
	@NonNull String given_name;
	/** True if the 2 devices related (like bond in Bluetooth or saved network in case of Wi-Fi device), false
	 * otherwise. */
	boolean is_linked;

	/**
	 * .
	 * @param type {@link #type}
	 * @param address {@link #address}
	 * @param last_detection {@link #last_detection}
	 * @param rssi {@link #distance}
	 * @param name {@link #name}
	 * @param given_name {@link #given_name}
	 * @param is_linked {@link #is_linked}
	 */
	ExtDevice(final int type, @NonNull final String address, final long last_detection, final int rssi,
			  @NonNull final String name, @NonNull final String given_name, final boolean is_linked) {
		this.type = type;
		this.address = address;
		this.last_detection = last_detection;
		this.rssi = rssi;
		this.name = name;
		this.given_name = given_name;
		this.is_linked = is_linked;
	}
}
