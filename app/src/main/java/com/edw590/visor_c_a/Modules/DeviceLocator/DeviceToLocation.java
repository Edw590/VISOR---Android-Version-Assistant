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

package com.edw590.visor_c_a.Modules.DeviceLocator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * <p>Class to instantiate to map devices to locations.</p>
 */
public final class DeviceToLocation {
	public final int type;
	public final String address;
	public final double last_detection;
	public final int max_distance;
	public final String name;
	public final int location;

	/**
	 * <p>Main class constructor.</p>
	 *
	 * @param type one of the TYPE_ constants in {@link ExtDeviceObj} - the type of the device (useful as an additional
	 *             check in case the device address is not globally unique
	 * @param address the unique address of the device (MAC, BSSID...)
	 * @param last_detection the maximum amount of time in minutes without checking in which the device may be in the
	 *                       specified location
	 * @param max_distance one of the ABSTR_ constants in {@link UtilsLocationRelative} - the maximum distance in
	 *                       which the device is in the specified location
	 * @param name the name of the device (like the Bluetooth device name or the WiFi network SSID) - may be used as an
	 *             additional check in case it's known the the given address is not unique to the device (example,
	 *             AA:AA:AA:AA:AA:AA as an issue on the Raspberry Pi), and only in case it's never changed (to be
	 *             assumed as a permanent name), or null if it's not to be specified
	 * @param location one of the LOCATION_ constants in {@link DeviceObj} - the location where the device is inside the
	 *             given distance
	 */
	public DeviceToLocation(final int type, @NonNull final String address, final double last_detection,
							final int max_distance, @Nullable final String name, final int location) {
		this.type = type;
		this.address = address;
		this.last_detection = last_detection;
		this.max_distance = max_distance;
		this.name = name;
		this.location = location;
	}
}
