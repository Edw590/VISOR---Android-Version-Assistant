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
import android.os.Build;

import androidx.annotation.Nullable;

import com.edw590.visor_c_a.GlobalUtils.AndroidSystem.UtilsAndroidConnectivity;
import com.edw590.visor_c_a.GlobalUtils.UtilsCheckHardwareFeatures;
import com.edw590.visor_c_a.GlobalUtils.UtilsNetwork;
import com.edw590.visor_c_a.GlobalUtils.UtilsPermsAuths;
import com.edw590.visor_c_a.GlobalUtils.UtilsShell;
import com.edw590.visor_c_a.Registry.UtilsRegistry;
import com.edw590.visor_c_a.Registry.ValuesRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import UtilsSWA.UtilsSWA;

public class WifiChecker {
	@Nullable final WifiManager wifi_manager = UtilsCheckHardwareFeatures.isWifiSupported() ?
			UtilsNetwork.getWifiManager() : null;

	boolean enabled_by_visor = false;
	static final long SCAN_WIFI_EACH = (long) (2.5 * 60000.0); // 2.5 minutes
	static final long SCAN_WIFI_EACH_PS = SCAN_WIFI_EACH << 2; // 2.5 * 4 = 10 minutes
	long waiting_time = SCAN_WIFI_EACH;
	long last_check_when = 0;

	int attempts = 0;

	public static final List<ExtDevice> nearby_aps_wifi = new ArrayList<>(64);

	final void setWifiEnabled(final boolean enable) {
		if (UtilsAndroidConnectivity.setWifiEnabled(enable) == UtilsShell.ErrCodes.NO_ERR) {
			enabled_by_visor = enable;
		}
	}

	final void checkWifi() {
		System.out.println(System.currentTimeMillis() >= last_check_when + waiting_time);
		if (System.currentTimeMillis() >= last_check_when + waiting_time && wifi_manager != null) {
			if (wifi_manager.isWifiEnabled()) {
				enabled_by_visor = false;
				if (UtilsPermsAuths.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
					wifi_manager.startScan();
				}
			} else {
				if (UtilsPermsAuths.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
					setWifiEnabled(true);
				}
			}
		}
	}

	final void powerSaverChanged(final boolean enabled) {
		if (enabled) {
			waiting_time = SCAN_WIFI_EACH_PS;
		} else {
			waiting_time = SCAN_WIFI_EACH;
		}
	}

	static void rssiChanged(final Intent intent) {
		UtilsRegistry.setData(ValuesRegistry.K_DIST_ROUTER, UtilsSWA.
				getRealDistanceRssiLOCRELATIVE(intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, -1),
						UtilsSWA.DEFAULT_TX_POWER), true);
	}

	final void wifiStateChanged(final Intent intent) {
		assert wifi_manager != null; // Change in Wi-Fi connection, so it's not null.


		int wifi_state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
		if (wifi_state == WifiManager.WIFI_STATE_ENABLED) {
			if (!wifi_manager.startScan() && enabled_by_visor) {
				setWifiEnabled(false);
			}
		} else if (wifi_state == WifiManager.WIFI_STATE_DISABLING ||
				wifi_state == WifiManager.WIFI_STATE_DISABLED) {
			UtilsRegistry.setData(ValuesRegistry.K_DIST_ROUTER, "-1", false);
			enabled_by_visor = false;
		}
	}

	final void scanResultsAvailable(final Intent intent) {
		assert wifi_manager != null; // Change in Wi-Fi connection, so it's not null.

		if (!intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, true)) {
			return;
		}

		nearby_aps_wifi.clear();

		// Checking again for the permission (aside from before calling startScan()) because the request may
		// have been done externally in the meantime, and we just go on the ride and use the results.
		if (UtilsPermsAuths.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
			System.out.println("OOOOOOOOOOOOOOOOOOOO");
			System.out.println(wifi_manager.getScanResults().size());
			for (final ScanResult scanResult : wifi_manager.getScanResults()) {
				long time_detection = System.currentTimeMillis();

				String address = scanResult.BSSID.toUpperCase(Locale.getDefault());

				int nearby_aps_wifi_size = nearby_aps_wifi.size();
				for (int i = 0; i < nearby_aps_wifi_size; ++i) {
					ExtDevice device = nearby_aps_wifi.get(i);
					if (device.type == ExtDevice.TYPE_BLUETOOTH && device.address.equals(address)) {
						nearby_aps_wifi.remove(i);

						break;
					}
				}
				nearby_aps_wifi.add(new ExtDevice(
						ExtDevice.TYPE_WIFI,
						address,
						time_detection,
						scanResult.level,
						scanResult.SSID,
						scanResult.SSID,
						Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? !scanResult.untrusted : false)
				);
			}

			if (nearby_aps_wifi.isEmpty() && attempts < 5) {
				// In case we didn't get any results, try at most 5 times to be sure it wasn't an internal error or
				// something (has happened. Networks in range and nothing returned).
				System.out.println("RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR1");
				attempts++;
				wifi_manager.startScan();
			} else {
				attempts = 0;

				System.out.println("YYYYYYYYYYYYYYYYYYYYYYYY1");
				System.out.println(enabled_by_visor);
				if (enabled_by_visor) {
					setWifiEnabled(false);
				}
			}

			// After we got the results successfully
			last_check_when = System.currentTimeMillis();
		}
	}

	final void networkStateChanged(final Intent intent) {
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
