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

package com.dadi590.assist_c_a.Modules.DeviceLocator;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;

import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.GlobalInterfaces.IModuleInst;
import com.dadi590.assist_c_a.GlobalUtils.AndroidSystem.UtilsAndroid;
import com.dadi590.assist_c_a.GlobalUtils.AndroidSystem.UtilsAndroidConnectivity;
import com.dadi590.assist_c_a.GlobalUtils.UtilsCheckHardwareFeatures;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.GlobalUtils.UtilsLocationRelative;
import com.dadi590.assist_c_a.GlobalUtils.UtilsNetwork;
import com.dadi590.assist_c_a.GlobalUtils.UtilsPermsAuths;
import com.dadi590.assist_c_a.ModulesList;
import com.dadi590.assist_c_a.ValuesStorage.ValuesStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * <p>The module responsible for locating the device as best and accurately as it can, absolutely (GPS coordinates, for
 * example, or realize it's at home, because of the Wi-Fi SSID) and relatively (the device is near the user's computer,
 * for example).</p>
 */
public final class DeviceLocator implements IModuleInst {

	// This below can be null if there's no Bluetooth adapter or there was some error, so Nullable for NPE warnings
	@Nullable final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	// This one can be null if there's no BLE on the device or there was some error, so NPE warnings are useful
	@Nullable final BluetoothLeScanner bluetoothLeScanner;

	@Nullable final WifiManager wifiManager = UtilsCheckHardwareFeatures.isWifiSupported() ?
			UtilsNetwork.getWifiManager() : null;


	private final DeviceObj current_device = new DeviceObj();

	private final int element_index = ModulesList.getElementIndex(this.getClass());
	private final HandlerThread main_handlerThread = new HandlerThread((String) ModulesList.getElementValue(element_index,
			ModulesList.ELEMENT_NAME));
	private Handler main_handler = null;

	boolean enabled_by_visor_bt = false;
	private static final long DISCOVER_BT_EACH = (long) (5.0 * 60000.0); // 5 minutes
	private static final long DISCOVER_BT_EACH_PS = DISCOVER_BT_EACH << 2; // 5 * 4 = 20 minutes
	long waiting_time_bt = DISCOVER_BT_EACH;
	long last_check_when_bt = 0L;

	private static final long CHECK_PUBLIC_IP_EACH = DISCOVER_BT_EACH; // 5 minutes
	private static final long CHECK_PUBLIC_IP_EACH_PS = CHECK_PUBLIC_IP_EACH; // 5 minutes (no battery is wasted with this)
	long waiting_time_public_ip = CHECK_PUBLIC_IP_EACH;
	long last_check_when_public_ip = 0L;

	boolean enabled_by_visor_wifi = false;
	private static final long SCAN_WIFI_EACH = (long) (2.5 * 60000.0); // 2.5 minutes
	private static final long SCAN_WIFI_EACH_PS = SCAN_WIFI_EACH << 2; // 2.5 * 4 = 10 minutes
	long waiting_time_wifi = SCAN_WIFI_EACH;
	long last_check_when_wifi = 0L;

	// The minimum check time of all check times (for the thread wait time)
	// EDIT: 30 seconds, so that if the Power Saver is disabled, after 30 seconds it will be noticed and the devices
	// will all be checked instead of possibly waiting the minimum time (2.5 min as of this writing).
	private static final long CHECK_TIME_MIN = 30_000L;

	public static final List<ExtDeviceObj> nearby_devices_bt = new ArrayList<>(64);
	public static final List<ExtDeviceObj> nearby_aps_wifi = new ArrayList<>(64);

	// todo KitKat may have problems when SEARCHING for Bluetooth devices --> WiFi will disconnect...
	//  Get it checking that and adapting or get it manual or something.
	// todo See also about this impeding BV9500 from sending files to other devices


	///////////////////////////////////////////////////////////////
	// IModuleInst stuff
	private boolean is_module_destroyed = false;

	@Override
	public boolean isFullyWorking() {
		if (is_module_destroyed) {
			return false;
		}

		return UtilsGeneral.isThreadWorking(main_handlerThread) && UtilsGeneral.isThreadWorking(infinity_thread);
	}

	@Override
	public void destroy() {
		infinity_thread.interrupt();
		try {
			UtilsGeneral.getContext().unregisterReceiver(broadcastReceiver);
		} catch (final IllegalArgumentException ignored) {
		}
		UtilsGeneral.quitHandlerThread(main_handlerThread);

		is_module_destroyed = true;
	}

	@Override
	public int wrongIsSupported() {return 0;}
	/**.
	 * @return read all here {@link IModuleInst#wrongIsSupported()} */
	public static boolean isSupported() {
		boolean location_is_supported_allowed = UtilsCheckHardwareFeatures.isLocationSupported() &&
				UtilsPermsAuths.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			location_is_supported_allowed &= UtilsPermsAuths.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
		}

		return UtilsCheckHardwareFeatures.isWifiSupported() || UtilsCheckHardwareFeatures.isBluetoothSupported() ||
				location_is_supported_allowed;
	}
	// IModuleInst stuff
	///////////////////////////////////////////////////////////////

	/**
	 * <p>Main class constructor.</p>
	 */
	public DeviceLocator() {
		main_handlerThread.start();
		main_handler = new Handler(main_handlerThread.getLooper());

		ValuesStorage.setValue(ValuesStorage.Keys.curr_network_type, UtilsNetwork.getCurrentNetworkType());

		if ((bluetoothAdapter != null) && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)) {
			bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
		} else {
			bluetoothLeScanner = null;
		}

		registerReceiver();

		infinity_thread.start();
	}

	private final Thread infinity_thread = new Thread(new Runnable() {
		@Override
		public void run() {
			while (true) {

				// Network type
				// Keep this check here!!!
				// That way it's not tempered with by VISOR enabling and disabling Wi-Fi, because the network type is
				// checked before any of that happens (and there's a delay, so the previous iteration won't impact here).
				// Or there are also no broadcast delays if the function call is right here.
				ValuesStorage.setValue(ValuesStorage.Keys.curr_network_type, UtilsNetwork.getCurrentNetworkType());


				// Public IP (must be next to the network type check - so that it's known where the IP belongs to)
				if (System.currentTimeMillis() >= last_check_when_public_ip + waiting_time_public_ip) {
					ValuesStorage.setValue(ValuesStorage.Keys.public_ip, UtilsNetwork.getExternalIpAddress());

					last_check_when_public_ip = System.currentTimeMillis();
				}


				// Bluetooth
				if (System.currentTimeMillis() >= last_check_when_bt + waiting_time_bt && null != bluetoothAdapter) {
					if (bluetoothAdapter.isEnabled()) {
						enabled_by_visor_bt = false;
						bluetoothAdapter.startDiscovery();
					} else {
						setBluetoothEnabled(true);
					}
				}


				// Wi-Fi
				if (System.currentTimeMillis() >= last_check_when_wifi + waiting_time_wifi && null != wifiManager) {
					if (wifiManager.isWifiEnabled()) {
						enabled_by_visor_wifi = false;
						if (UtilsPermsAuths.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
							wifiManager.startScan();
						}
					} else {
						if (UtilsPermsAuths.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
							setWifiEnabled(true);
						}
					}
				}


				try {
					Thread.sleep(CHECK_TIME_MIN);
				} catch (final InterruptedException ignored) {
					return;
				}
			}
		}
	});

	/**
	 * <p>Register the module's broadcast receiver.</p>
	 */
	private void registerReceiver() {
		final IntentFilter intentFilter = new IntentFilter();

		// Network type
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

		// todo Put the 3 types: Bluetooth, BLE, and Wi-Fi

		// Bluetooth
		if (null != bluetoothAdapter) {
			intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
			intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
			intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
			intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
			intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
		}

		// Wi-Fi
		if (null != wifiManager) {
			intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
			intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
			intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
			intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		}

		// Power Saver
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			intentFilter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
		}

		try {
			UtilsGeneral.getContext().registerReceiver(broadcastReceiver, intentFilter, null, main_handler);
		} catch (final IllegalArgumentException ignored) {
		}
	}

	void setWifiEnabled(final boolean enable) {
		if (UtilsAndroid.GEN_ERR != UtilsAndroidConnectivity.setWifiEnabled(enable)) {
			enabled_by_visor_wifi = enable;
		}
	}

	void setBluetoothEnabled(final boolean enable) {
		if (UtilsAndroid.GEN_ERR != UtilsAndroidConnectivity.setBluetoothEnabled(enable)) {
			enabled_by_visor_bt = enable;
		}
	}

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(@Nullable final Context context, @Nullable final Intent intent) {
			if (intent == null || intent.getAction() == null) {
				return;
			}

			System.out.println("PPPPPPPPPPPPPPPPPP-DeviceLocator - " + intent.getAction());

			switch (intent.getAction()) {
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////

				////////////////////////////////////////////////
				////////////////////////////////////////////////
				// Bluetooth
				case (BluetoothAdapter.ACTION_DISCOVERY_STARTED): {
					// Don't forget other apps can start the discovery...
					// In that case, use that advantage and don't start it for another period of time. Just listen to
					// the broadcasts.
					last_check_when_bt = System.currentTimeMillis();

					nearby_devices_bt.clear();

					break;
				}
				case (BluetoothDevice.ACTION_FOUND): {
					final long time_detection = System.currentTimeMillis();
					final BluetoothDevice bluetoothDevice =	intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					final short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MAX_VALUE);

					final String address = bluetoothDevice.getAddress().toUpperCase(Locale.getDefault());
					final int nearby_devices_size = nearby_devices_bt.size();
					for (int i = 0; i < nearby_devices_size; ++i) {
						final ExtDeviceObj device = nearby_devices_bt.get(i);
						if (device.type == ExtDeviceObj.TYPE_BLUETOOTH && device.address.equals(address)) {
							nearby_devices_bt.remove(i);

							break;
						}
					}
					nearby_devices_bt.add(new ExtDeviceObj(
							ExtDeviceObj.TYPE_BLUETOOTH,
							address,
							time_detection,
							UtilsLocationRelative.getRealDistanceRSSI((int) rssi, UtilsLocationRelative.DEFAULT_TX_POWER),
							bluetoothDevice.getName(),
							bluetoothDevice.getAlias(),
							BluetoothDevice.BOND_BONDED == bluetoothDevice.getBondState())
					);

					break;
				}
				case (BluetoothAdapter.ACTION_DISCOVERY_FINISHED): {
					assert null != bluetoothAdapter; // Won't be null if the *adapter's* state changed...

					// Again, as soon as the discovery stops, reset the count. If it's not reset, the assistant will
					// start the countdown as soon as the discovery started, and should be as soon as it finishes.
					last_check_when_bt = System.currentTimeMillis();

					if (enabled_by_visor_bt) {
						// If Bluetooth was not enabled when the discovery started, disable it again.
						setBluetoothEnabled(false);
					}

					break;
				}
				case (BluetoothAdapter.ACTION_STATE_CHANGED): {
					assert null != bluetoothAdapter; // Won't be null if the *adapter's* state changed...

					final int bluetooth_state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);

					if (BluetoothAdapter.STATE_ON == bluetooth_state) {
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

						if (bluetoothAdapter.startDiscovery()) {
							last_check_when_bt = System.currentTimeMillis();
						}
					} else if (BluetoothAdapter.STATE_TURNING_OFF == bluetooth_state ||
							BluetoothAdapter.STATE_OFF == bluetooth_state) {
						enabled_by_visor_bt = false;
					}

					break;
				}
				case (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED): {
					assert null != bluetoothAdapter; // Won't be null if the *adapter's* state changed...

					final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
					final BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					if (BluetoothAdapter.STATE_CONNECTING == state || BluetoothAdapter.STATE_CONNECTED == state) {
						if (enabled_by_visor_bt) {
							// If a device is at minimum attempting to connect, turn the adapter off instantly.
							// Reason why I don't "just" disconnect the device or stop it from even trying to connect in
							// the first place explained in ACTION_STATE_CHANGED's case.
							setBluetoothEnabled(false);

							//Só aqui? Periodicamente era melhor. Com o mesmo intervalo do WiFi por ser baixa energia? Ou talvez até a cada minuto ou algo do género?
							//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
							//	bluetoothLeScanner.startScan();
							//} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
							//	bluetoothAdapter.startLeScan();
							//}
						}
					}

					break;
				}


				////////////////////////////////////////////////
				////////////////////////////////////////////////
				// Wi-Fi
				case (WifiManager.RSSI_CHANGED_ACTION): {
					ValuesStorage.setValue(ValuesStorage.Keys.dist_router, UtilsLocationRelative.
							getRealDistanceRSSI(intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, -1),
									UtilsLocationRelative.DEFAULT_TX_POWER));

					break;
				}
				case (WifiManager.WIFI_STATE_CHANGED_ACTION): {
					assert null != wifiManager; // Change in Wi-Fi connection, so it's not null.

					final int wifi_state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
					if (WifiManager.WIFI_STATE_ENABLED == wifi_state) {
						boolean turn_off = false;
						if (UtilsPermsAuths.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
							if (!wifiManager.startScan() && enabled_by_visor_wifi) {
								turn_off = true;
							}
						} else if (enabled_by_visor_wifi) {
							turn_off = true;
						}

						if (turn_off) {
							setWifiEnabled(false);
						}
					} else if (WifiManager.WIFI_STATE_DISABLED == wifi_state) {
						ValuesStorage.setValue(ValuesStorage.Keys.dist_router, "-1");
						enabled_by_visor_wifi = false;
					}

					break;
				}
				case (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION): {
					assert null != wifiManager; // Change in Wi-Fi connection, so it's not null.

					System.out.println("YYYYYYYYYYYYYYYYYYYYYYYY1");
					System.out.println(enabled_by_visor_wifi);
					if (enabled_by_visor_wifi) {
						setWifiEnabled(false);
					}

					if (!intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, true)) {
						break;
					}

					nearby_aps_wifi.clear();

					// Checking again for the permission (aside from before calling startScan()) because the request may
					// have been done externally in the meantime, and we just go on the ride and use the results.
					if (UtilsPermsAuths.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
						System.out.println("OOOOOOOOOOOOOOOOOOOO");
						System.out.println(wifiManager.getScanResults().size());
						for (final ScanResult scanResult : wifiManager.getScanResults()) {
							final long time_detection = System.currentTimeMillis();

							final int distance;
							if (ScanResult.UNSPECIFIED == scanResult.distanceCm) {
								distance = UtilsLocationRelative.getRealDistanceRSSI((int) scanResult.level,
										UtilsLocationRelative.DEFAULT_TX_POWER);
							} else {
								distance = Math.toIntExact(Math.round((double) scanResult.distanceCm / 100.0));
							}

							final String address = scanResult.BSSID.toUpperCase(Locale.getDefault());

							final int nearby_aps_wifi_size = nearby_aps_wifi.size();
							for (int i = 0; i < nearby_aps_wifi_size; ++i) {
								final ExtDeviceObj device = nearby_aps_wifi.get(i);
								if (device.type == ExtDeviceObj.TYPE_BLUETOOTH && device.address.equals(address)) {
									nearby_aps_wifi.remove(i);

									break;
								}
							}
							nearby_aps_wifi.add(new ExtDeviceObj(
									ExtDeviceObj.TYPE_WIFI,
									address,
									time_detection,
									distance,
									scanResult.SSID,
									scanResult.SSID,
									!scanResult.untrusted)
							);
						}

						// After we got the results successfully
						last_check_when_wifi = System.currentTimeMillis();
					}

					break;
				}
				case (WifiManager.NETWORK_STATE_CHANGED_ACTION): {
					assert null != wifiManager; // Change in Wi-Fi connection, so it's not null.

					final NetworkInfo.State state = ((NetworkInfo) intent.
							getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO)).getState();
					if (NetworkInfo.State.CONNECTING == state || NetworkInfo.State.CONNECTED == state) {
						if (enabled_by_visor_wifi) {
							wifiManager.disconnect();
						}
					}

					break;
				}


				////////////////////////////////////////////////
				////////////////////////////////////////////////
				// Network type
				case (ConnectivityManager.CONNECTIVITY_ACTION): {
					ValuesStorage.setValue(ValuesStorage.Keys.curr_network_type, UtilsNetwork.getCurrentNetworkType());

					break;
				}


				////////////////////////////////////////////////
				////////////////////////////////////////////////
				// Power Saver mode
				case (PowerManager.ACTION_POWER_SAVE_MODE_CHANGED): {
					final PowerManager powerManager = (PowerManager) UtilsGeneral.getSystemService(Context.POWER_SERVICE);
					assert powerManager != null; // Broadcast received, so the service exists

					if (powerManager.isPowerSaveMode()) {
						waiting_time_bt = DISCOVER_BT_EACH_PS;
						waiting_time_public_ip = CHECK_PUBLIC_IP_EACH_PS;
						waiting_time_wifi = SCAN_WIFI_EACH_PS;
					} else {
						waiting_time_bt = DISCOVER_BT_EACH;
						waiting_time_public_ip = CHECK_PUBLIC_IP_EACH;
						waiting_time_wifi = SCAN_WIFI_EACH;
					}

					break;
				}
			}

			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
		}
	};

	final BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {

		}
	};
}
