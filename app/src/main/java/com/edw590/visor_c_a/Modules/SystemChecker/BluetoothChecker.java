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
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.Nullable;

import com.edw590.visor_c_a.GlobalUtils.AndroidSystem.UtilsAndroidConnectivity;
import com.edw590.visor_c_a.GlobalUtils.UtilsContext;
import com.edw590.visor_c_a.GlobalUtils.UtilsPermsAuths;
import com.edw590.visor_c_a.GlobalUtils.UtilsShell;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class BluetoothChecker {
	// This below can be null if there's no Bluetooth adapter or there was some error, so Nullable for NPE warnings
	@Nullable final BluetoothAdapter bluetooth_adapter = BluetoothAdapter.getDefaultAdapter();
	// This one can be null if there's no BLE on the device or there was some error, so NPE warnings are useful
	@Nullable BluetoothLeScanner bluetoothLeScanner = null;
	@Nullable BluetoothAdapter.LeScanCallback leScanCallback = null;

	@Nullable BluetoothHeadset bluetoothHeadset = null;
	@Nullable BluetoothA2dp bluetoothA2dp = null;

	boolean enabled_by_visor = false;
	static final long DISCOVER_BT_EACH = (long) (5.0 * 60000.0); // 5 minutes
	static final long DISCOVER_BT_EACH_PS = DISCOVER_BT_EACH << 2; // 5 * 4 = 20 minutes
	long waiting_time = DISCOVER_BT_EACH;
	long last_check_when = 0;

	int attempts = 0;

	public static final List<ExtDevice> nearby_devices_bt = new ArrayList<>(64);

	void setBluetoothEnabled(final boolean enable) {
		if (UtilsAndroidConnectivity.setBluetoothEnabled(enable) == UtilsShell.ErrCodes.NO_ERR) {
			enabled_by_visor = enable;
		}
	}

	void startBluetooth() {
		if (bluetooth_adapter != null) {
			bluetooth_adapter.getProfileProxy(UtilsContext.getContext(), serviceListener, BluetoothProfile.HEADSET);
			bluetooth_adapter.getProfileProxy(UtilsContext.getContext(), serviceListener, BluetoothProfile.A2DP);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				bluetoothLeScanner = bluetooth_adapter.getBluetoothLeScanner();
			} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
				leScanCallback = new BluetoothAdapter.LeScanCallback() {
					@Override
					public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {

					}
				};
			}
		}
	}

	void checkBluetooth() {
		if (System.currentTimeMillis() >= last_check_when + waiting_time && bluetooth_adapter != null) {
			if (bluetooth_adapter.isEnabled()) {
				enabled_by_visor = false;
				if (UtilsPermsAuths.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)) {
					bluetooth_adapter.startDiscovery();
				}
			} else {
				setBluetoothEnabled(true);
			}
		}
	}

	void powerSaverChanged(final boolean enabled) {
		if (enabled) {
			waiting_time = DISCOVER_BT_EACH_PS;
		} else {
			waiting_time = DISCOVER_BT_EACH;
		}
	}

	void discoveryStarted() {
		// Don't forget other apps can start the discovery...
		// In that case, use that advantage and don't start it for another period of time. Just listen to
		// the broadcasts.
		last_check_when = System.currentTimeMillis();

		nearby_devices_bt.clear();
	}

	void discoveryFinished() {
		assert bluetooth_adapter != null; // Won't be null if the *adapter's* state changed...

		// Again, as soon as the discovery stops, reset the count. If it's not reset, the assistant will
		// start the countdown as soon as the discovery started, and should be as soon as it finishes.
		last_check_when = System.currentTimeMillis();

		if (enabled_by_visor) {
			// If Bluetooth was not enabled when the discovery started, disable it again.
			setBluetoothEnabled(false);
		}
	}

	static void deviceFound(final Intent intent) {
		long time_detection = System.currentTimeMillis();
		BluetoothDevice bluetoothDevice =	intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MAX_VALUE);

		String address = bluetoothDevice.getAddress().toUpperCase(Locale.getDefault());
		int nearby_devices_size = nearby_devices_bt.size();
		for (int i = 0; i < nearby_devices_size; ++i) {
			ExtDevice device = nearby_devices_bt.get(i);
			if (device.type == ExtDevice.TYPE_BLUETOOTH && device.address.equals(address)) {
				nearby_devices_bt.remove(i);

				break;
			}
		}
		String alias = bluetoothDevice.getAlias();
		nearby_devices_bt.add(new ExtDevice(
				ExtDevice.TYPE_BLUETOOTH,
				address,
				time_detection,
				rssi,
				bluetoothDevice.getName(),
				alias == null ? "null" : alias,
				bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED)
		);
	}

	void bluetoothStateChanged(final Intent intent) {
		assert bluetooth_adapter != null; // Won't be null if the *adapter's* state changed...

		int bluetooth_state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);

		if (bluetooth_state == BluetoothAdapter.STATE_ON) {
			// I spent an entire day going through BluetoothDevice, BluetoothAdapter, BluetoothHeadset,
			// BluetoothA2dp, BluetoothManager, IBluetooth, IBluetoothHeadset, IBluetoothA2dp, and
			// IBluetoothManager. Found only a function that can stop a device from auto-connecting
			// below Android Pie (BluetoothAdapter.enableNoAutoConnect()) --> and it requires the caller to
			// have the NFC_UDI, so.... nope. Also no idea how to call it with ADB commands (if it's even
			// possible anyway).
			// On Android 10+ there are more functions. Like BluetoothDevice.setSilentMode(),
			// BluetoothAdapter/Headset/A2dp.setActiveDevice(), and
			// BluetoothHeadset/A2dp.setConnectionPolicy(), but I didn't test any of those (I only have
			// Oreo at most right now).
			// Also tried to re-route the audio with AudioSystem when turning on the adapter and while the
			// device is connecting, but Android tries to connect anyway.
			// So the current way for Pie- is to disable the Bluetooth as soon as Android attempts to
			// connect to any device, since we can't stop the connection while it's already being tried.
			//if (enabled_by_visor_bt) {
			//	// If the Bluetooth was turned on by VISOR, stop it from auto-connecting to any devices.
			//  // EDIT: this doesn't work for some reason. I don't get it. I've got the permission, but it
			//  // still auto-connects. Maybe audio devices are special? (I'm testing with my headphones)
			//  // EDIT 2: "nor connectable *from* remote devices" - yeah, the problem is the phone is
			//  // wanting itself to connect, not the other way around. Ugh. There goes the idea...
			//	//bluetoothAdapter.setScanMode(BluetoothAdapter.SCAN_MODE_NONE, 0);
			//}

			if (bluetooth_adapter.startDiscovery()) {
				last_check_when = System.currentTimeMillis();
			}
		} else if (bluetooth_state == BluetoothAdapter.STATE_TURNING_OFF ||
				bluetooth_state == BluetoothAdapter.STATE_OFF) {
			enabled_by_visor = false;
		}
	}

	void connectionStateChanged(final Intent intent) {
		assert bluetooth_adapter != null; // Won't be null if the *adapter's* state changed...

		int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
		BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		if (state == BluetoothAdapter.STATE_CONNECTING || state == BluetoothAdapter.STATE_CONNECTED) {
			if (enabled_by_visor) {
				// If a device is at minimum attempting to connect, turn the adapter off instantly.
				// Reason why I don't "just" disconnect the device or stop it from even trying to
				// connect in the first place explained in ACTION_STATE_CHANGED's case.
				// EDIT: it's now attempting to disconnect the device. Doesn't work on Oreo 8.1, but
				// maybe it works in some other version(s).
				// EDIT 2: removed it. on BV9500 it's not being fast enough and the headset connected a few
				// times. Back to instant-disable. The API says it must be in the connected state to
				// actually disconnect() anyway.
				setBluetoothEnabled(false);
			}
		}
	}

	BluetoothProfile.ServiceListener serviceListener = new BluetoothProfile.ServiceListener() {
		@Override
		public void onServiceConnected(final int profile, final BluetoothProfile proxy) {
			switch (profile) {
				case (BluetoothProfile.HEADSET): {
					bluetoothHeadset = (BluetoothHeadset) proxy;

					break;
				}
				case (BluetoothProfile.A2DP): {
					bluetoothA2dp = (BluetoothA2dp) proxy;

					break;
				}
			}
		}

		@Override
		public void onServiceDisconnected(final int profile) {
			switch (profile) {
				case (BluetoothProfile.HEADSET): {
					bluetoothHeadset = null;

					break;
				}
				case (BluetoothProfile.A2DP): {
					bluetoothA2dp = null;

					break;
				}
			}
		}
	};
}
