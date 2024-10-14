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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.edw590.visor_c_a.GlobalInterfaces.IModuleInst;
import com.edw590.visor_c_a.GlobalUtils.AndroidSystem.UtilsAndroidConnectivity;
import com.edw590.visor_c_a.GlobalUtils.AndroidSystem.UtilsAndroidPower;
import com.edw590.visor_c_a.GlobalUtils.UtilsApp;
import com.edw590.visor_c_a.GlobalUtils.UtilsContext;
import com.edw590.visor_c_a.GlobalUtils.UtilsGeneral;
import com.edw590.visor_c_a.GlobalUtils.UtilsNetwork;
import com.edw590.visor_c_a.Modules.Speech.Speech2;
import com.edw590.visor_c_a.Modules.Speech.UtilsSpeech2BC;
import com.edw590.visor_c_a.ModulesList;
import com.edw590.visor_c_a.Registry.UtilsRegistry;
import com.edw590.visor_c_a.Registry.ValuesRegistry;

public class SystemChecker implements IModuleInst {

	private final HandlerThread main_handlerThread =
			new HandlerThread((String) ModulesList.getElementValue(ModulesList.getElementIndex(getClass()),
					ModulesList.ELEMENT_NAME));
	private final Handler main_handler;

	final PowerChecker power_checker = new PowerChecker();
	final WifiChecker wifi_checker = new WifiChecker();
	final BluetoothChecker bluetooth_checker = new BluetoothChecker();

	// These 2 below appeared in a StackOverflow answer. Maybe it's the same explanation as the POWERON one. Keep it.
	private static final String ACTION_HTC_QCK_POFF = "com.htc.intent.action.QUICKBOOT_POWEROFF";
	private static final String ACTION_ANDR_QCK_POFF = "android.intent.action.QUICKBOOT_POWEROFF";

	// The minimum check time of all check times (for the thread wait time)
	// EDIT: 30 seconds, so that if the Power Saver is disabled, after 30 seconds it will be noticed and the devices
	// will all be checked instead of possibly waiting the minimum time (2.5 min as of this writing).
	// EDIT 2: not sure what this above is about. 5 seconds now because of DeviceInfo.sendInfo() needing at most 5 secs
	// of delay between each info sent.
	// EDIT 3: no more need for the 5 seconds. But now it's just because I want it checking constantly.
	public static final long CHECK_TIME = 5_000;

	@NonNull final PowerManager power_manager = (PowerManager) UtilsContext.getSystemService(Context.POWER_SERVICE);

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
		try {
			UtilsContext.getContext().unregisterReceiver(broadcastReceiver);
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
		return true;
	}
	// IModuleInst stuff
	///////////////////////////////////////////////////////////////

	public SystemChecker() {
		main_handlerThread.start();
		main_handler = new Handler(main_handlerThread.getLooper());

		UtilsRegistry.setData(ValuesRegistry.K_AIRPLANE_MODE_ON, UtilsAndroidConnectivity.getAirplaneModeEnabled(),
				false);

		bluetooth_checker.startBluetooth();

		registerReceiver();

		infinity_thread.start();
	}

	private final Thread infinity_thread = new Thread(new Runnable() {
		@Override
		public void run() {
			assert power_manager != null; // It exists - it's the ---Power--- Manager

			while (true) {
				UtilsRegistry.setData(ValuesRegistry.K_DEVICE_IN_USE, power_manager.isScreenOn(), false);

				UtilsRegistry.setData(ValuesRegistry.K_SCREEN_BRIGHTNESS, UtilsAndroidPower.getScreenBrightness(),
						false);

				AudioManager audioManager = (AudioManager) UtilsContext.getContext().
						getSystemService(Context.AUDIO_SERVICE);
				UtilsRegistry.setData(ValuesRegistry.K_SOUND_VOLUME,
						audioManager.getStreamVolume(AudioManager.STREAM_RING), false);
				UtilsRegistry.setData(ValuesRegistry.K_SOUND_MUTED,
						audioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL, false);

				// Network type
				// Keep this check here!!!
				// That way it's not tempered with by VISOR enabling and disabling Wi-Fi, because the network type is
				// checked before any of that happens (and there's a delay, so the previous iteration won't impact here).
				// Or there are also no broadcast delays if the function call is right here.
				UtilsRegistry.setData(ValuesRegistry.K_CURR_NETWORK_TYPE, UtilsNetwork.getCurrentNetworkType(), false);


				// Bluetooth
				bluetooth_checker.checkBluetooth();

				// Wi-Fi
				wifi_checker.checkWifi();

				try {
					Thread.sleep(CHECK_TIME);
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
		try {
			final IntentFilter intentFilter = new IntentFilter();

			// Network type
			intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

			// todo Put the 3 types: Bluetooth, BLE, and Wi-Fi

			// Bluetooth
			if (bluetooth_checker.bluetooth_adapter != null) {
				intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
				intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
				intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
				intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
				intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
			}

			// Wi-Fi
			if (wifi_checker.wifi_manager != null) {
				intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
				intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
				intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
				intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
			}

			// Airplane mode
			intentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);

			// Power Saver
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				intentFilter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
			}

			// Battery and Power
			intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
			intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
			intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);

			// Shutdown and reboot
			intentFilter.addAction(Intent.ACTION_SHUTDOWN);
			intentFilter.addAction(Intent.ACTION_REBOOT);
			intentFilter.addAction(ACTION_HTC_QCK_POFF);
			intentFilter.addAction(ACTION_ANDR_QCK_POFF);

			UtilsContext.getContext().registerReceiver(broadcastReceiver, intentFilter, null, main_handler);
		} catch (final IllegalArgumentException ignored) {
		}
	}

	public final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(@Nullable final Context context, @Nullable final Intent intent) {
			if (intent == null || intent.getAction() == null) {
				return;
			}

			System.out.println("PPPPPPPPPPPPPPPPPP-SystemChecker - " + intent.getAction());

			switch (intent.getAction()) {
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////

				case (Intent.ACTION_BATTERY_CHANGED): {
					// Continue the execution even if battery_present is false. On miTab Advance, it's false and there
					// is a battery on it, and the percentage and power connected or disconnected are correctly
					// retrieved (weird).

					final int battery_status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
					final int battery_lvl = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
					final int battery_lvl_scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
					@Nullable final Boolean battery_present = intent.hasExtra(BatteryManager.EXTRA_PRESENT) ?
							intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false) : null;
					power_checker.processBatteryLvlChg(battery_status, battery_lvl, battery_lvl_scale, battery_present);

					break;
				}
				case (Intent.ACTION_POWER_CONNECTED): {
					power_checker.actions_power_mode_broadcast = true;
					power_checker.processBatteryPwrChg(true);

					break;
				}
				case (Intent.ACTION_POWER_DISCONNECTED): {
					power_checker.actions_power_mode_broadcast = true;
					power_checker.processBatteryPwrChg(false);

					break;
				}

				/////////////////////////////////////
				// Shutdown and reboot
				case (ACTION_HTC_QCK_POFF):
				case (ACTION_ANDR_QCK_POFF):
				case (Intent.ACTION_SHUTDOWN): {
					UtilsApp.prepareShutdown();

					if (intent.getBooleanExtra(Intent.EXTRA_SHUTDOWN_USERSPACE_ONLY, false)) {
						final String speak = "Fast shut down detected.";
						UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, 0, true, null);
					} else {
						final String speak = "Shut down detected.";
						UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, 0, true, null);
					}
					// Note: must be very small speeches, since the phone will shut down fast.

					break;
				}
				case (Intent.ACTION_REBOOT): {
					UtilsApp.prepareShutdown();

					// No idea if this is supposed detected at all (might be stopped before it gets here by the system
					// as soon as it detects it or something).
					final String speak = "Reboot detected.";
					UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, 0, true, null);
					// Note: must be a very small speech, since the phone will shut down fast.


					break;
				}

				/////////////////////////////////////
				// Wi-Fi
				case (WifiManager.RSSI_CHANGED_ACTION): {
					WifiChecker.rssiChanged(intent);

					break;
				}
				case (WifiManager.WIFI_STATE_CHANGED_ACTION): {
					wifi_checker.wifiStateChanged(intent);

					break;
				}
				case (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION): {
					wifi_checker.scanResultsAvailable(intent);

					break;
				}
				case (WifiManager.NETWORK_STATE_CHANGED_ACTION): {
					wifi_checker.networkStateChanged(intent);

					break;
				}


				/////////////////////////////////////
				// Bluetooth
				case (BluetoothAdapter.ACTION_DISCOVERY_STARTED): {
					bluetooth_checker.discoveryStarted();

					break;
				}
				case (BluetoothDevice.ACTION_FOUND): {
					BluetoothChecker.deviceFound(intent);

					break;
				}
				case (BluetoothAdapter.ACTION_DISCOVERY_FINISHED): {
					bluetooth_checker.discoveryFinished();

					break;
				}
				case (BluetoothAdapter.ACTION_STATE_CHANGED): {
					bluetooth_checker.bluetoothStateChanged(intent);

					break;
				}
				case (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED): {
					bluetooth_checker.connectionStateChanged(intent);

					break;
				}

				/////////////////////////////////////
				// Network type
				case (ConnectivityManager.CONNECTIVITY_ACTION): {
					UtilsRegistry.setData(ValuesRegistry.K_CURR_NETWORK_TYPE, UtilsNetwork.getCurrentNetworkType(),
							false);

					break;
				}

				/////////////////////////////////////
				// Airplane mode
				case (Intent.ACTION_AIRPLANE_MODE_CHANGED): {
					UtilsRegistry.setData(ValuesRegistry.K_AIRPLANE_MODE_ON,
							UtilsAndroidConnectivity.getAirplaneModeEnabled(), false);

					break;
				}

				/////////////////////////////////////
				// Power Saver mode
				case (PowerManager.ACTION_POWER_SAVE_MODE_CHANGED): {
					PowerManager powerManager = (PowerManager) UtilsContext.getSystemService(Context.POWER_SERVICE);
					assert powerManager != null; // Broadcast received, so the service exists

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // Linter is wtf and needs this here
						final boolean power_saver_enabled = powerManager.isPowerSaveMode();
						wifi_checker.powerSaverChanged(power_saver_enabled);
						bluetooth_checker.powerSaverChanged(power_saver_enabled);
					}

					break;
				}
			}

			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
		}
	};
}
