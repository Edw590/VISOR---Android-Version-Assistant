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

import android.Manifest;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.edw590.visor_c_a.GlobalUtils.AndroidSystem.UtilsAndroidConnectivity;
import com.edw590.visor_c_a.GlobalUtils.UtilsCheckHardwareFeatures;
import com.edw590.visor_c_a.GlobalUtils.UtilsNetwork;
import com.edw590.visor_c_a.GlobalUtils.UtilsPermsAuths;
import com.edw590.visor_c_a.GlobalUtils.UtilsReflection;
import com.edw590.visor_c_a.GlobalUtils.UtilsShell;
import com.edw590.visor_c_a.Registry.UtilsRegistry;
import com.edw590.visor_c_a.Registry.RegistryKeys;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import UtilsSWA.UtilsSWA;

final class WifiChecker {
	@Nullable final WifiManager wifi_manager = UtilsCheckHardwareFeatures.isWifiSupported() ?
			UtilsNetwork.getWifiManager() : null;

	boolean enabled_by_visor = false;
	static final long SCAN_WIFI_EACH_MS = (long) (2.5 * 60000.0); // 2.5 minutes
	static final long SCAN_WIFI_EACH_PS_MS = SCAN_WIFI_EACH_MS << 2; // 2.5 * 4 = 10 minutes
	long waiting_time_ms = SCAN_WIFI_EACH_MS;
	long last_check_when_ms = 0;

	int attempts = 0;

	static final List<ExtDevice> nearby_aps_wifi = new ArrayList<>(64);

	/**
	 * <p>Enables or disables Wi-Fi.</p>
	 *
	 * @param enable true to enable, false to disable
	 */
	void setWifiEnabled(final boolean enable) {
		if (UtilsAndroidConnectivity.setWifiEnabled(enable) == UtilsShell.ErrCodes.NO_ERR) {
			enabled_by_visor = enable;
		}
	}

	/**
	 * <p>In case Wi-Fi is disabled, enables it; on the next check, if it's enabled, starts a scan.</p>
	 *
	 * @param ignore_ap true to ignore if the AP is enabled (else it won't enable the Wi-Fi), false otherwise
	 */
	void checkWifi(final boolean ignore_ap) {
		if (System.currentTimeMillis() >= last_check_when_ms + waiting_time_ms && wifi_manager != null) {
			if (UtilsPermsAuths.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
				if (wifi_manager.isWifiEnabled()) {
					wifi_manager.startScan();
				} else {
					if (ignore_ap || !wifi_manager.isWifiApEnabled()) {
						setWifiEnabled(true);
					}
				}
			}
		}
	}

	/**
	 * <p>Changes the waiting time between Wi-Fi scans based on power saver mode.</p>
	 *
	 * @param enabled true if power saver is enabled, false otherwise
	 */
	void powerSaverChanged(final boolean enabled) {
		if (enabled) {
			waiting_time_ms = SCAN_WIFI_EACH_PS_MS;
		} else {
			waiting_time_ms = SCAN_WIFI_EACH_MS;
		}
	}

	/**
	 * <p>Updates the distance to the router based on the new RSSI value.</p>
	 *
	 * @param intent the intent containing the new RSSI value
	 */
	static void rssiChanged(@NonNull final Intent intent) {
		UtilsRegistry.setData(RegistryKeys.K_DIST_ROUTER, UtilsSWA.
				getRealDistanceRssiLOCRELATIVE(intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, -1),
						UtilsSWA.DEFAULT_TX_POWER), true);
	}

	/**
	 * <p>Handles changes in Wi-Fi state.</p>
	 *
	 * @param intent the intent containing the new Wi-Fi state
	 */
	void wifiStateChanged(@NonNull final Intent intent) {
		assert wifi_manager != null; // Change in Wi-Fi connection, so it's not null.


		int wifi_state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
		if (wifi_state == WifiManager.WIFI_STATE_ENABLED) {
			if (!wifi_manager.startScan() && enabled_by_visor) {
				setWifiEnabled(false);
			}
		} else if (wifi_state == WifiManager.WIFI_STATE_DISABLING ||
				wifi_state == WifiManager.WIFI_STATE_DISABLED) {
			UtilsRegistry.setData(RegistryKeys.K_DIST_ROUTER, "-1", false);
			enabled_by_visor = false;
		}
	}

	/**
	 * <p>Handles the availability of scan results.</p>
	 *
	 * @param intent the intent indicating that scan results are available
	 */
	void scanResultsAvailable(@NonNull final Intent intent) {
		assert wifi_manager != null; // Change in Wi-Fi connection, so it's not null.

		if (!intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, true)) {
			return;
		}

		// Checking again for the permission (aside from before calling startScan()) because the request may
		// have been done externally in the meantime, and we just go on the ride and use the results.
		if (!UtilsPermsAuths.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
			return;
		}

		Collection<ExtDevice> found_aps = new ArrayList<>(64);
		for (final ScanResult scanResult : wifi_manager.getScanResults()) {
			Boolean untrusted = (Boolean) UtilsReflection.getFieldValue(scanResult, "untrusted");
			if (untrusted == null) {
				untrusted = false;
			}
			found_aps.add(new ExtDevice(
					ExtDevice.TYPE_WIFI,
					scanResult.BSSID.toUpperCase(Locale.getDefault()),
					System.currentTimeMillis(),
					scanResult.level,
					scanResult.SSID,
					scanResult.SSID,
					!untrusted)
			);
		}

		if (found_aps.isEmpty() && attempts < 5) {
			// In case we didn't get any results, try at most 5 times to be sure it wasn't an internal error or
			// something (has happened. Networks in range and nothing returned).
			attempts++;
			wifi_manager.startScan();

			try {
				Thread.sleep(1000);
			} catch (final InterruptedException ignored) {
			}
		} else {
			attempts = 0;

			nearby_aps_wifi.clear();
			nearby_aps_wifi.addAll(found_aps);

			if (enabled_by_visor) {
				setWifiEnabled(false);
			}
		}

		// After we got the results successfully
		last_check_when_ms = System.currentTimeMillis();
	}

	/**
	 * <p>Handles changes in network state.</p>
	 *
	 * @param intent the intent containing the new network state
	 */
	void networkStateChanged(@NonNull final Intent intent) {
		assert wifi_manager != null; // Change in Wi-Fi connection, so it's not null.

		NetworkInfo.State state = ((NetworkInfo) intent.
				getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO)).getState();
		if (state == NetworkInfo.State.CONNECTING || state == NetworkInfo.State.CONNECTED) {
			if (enabled_by_visor) {
				if (!wifi_manager.disconnect()) {
					setWifiEnabled(false);
				}
			}
		}
	}
}
