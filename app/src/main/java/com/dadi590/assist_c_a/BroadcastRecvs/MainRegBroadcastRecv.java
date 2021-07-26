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

package com.dadi590.assist_c_a.BroadcastRecvs;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;

import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;

/**
 * <p>The class to be used for all main broadcasts that need registering on the main app process.</p>
 */
public class MainRegBroadcastRecv {

	/**
	 * <p>Main class constructor.</p>
	 */
	public MainRegBroadcastRecv() {
	}

	/**
	 * <p>Registers all the receivers in the class.</p>
	 *
	 */
	public final void registerReceivers() {
		// Note: don't put as a constructor, since the receivers must be registered only after TTS is ready.

		final IntentFilter intentFilter = new IntentFilter();

		// Power
		intentFilter.addAction(Intent.ACTION_SHUTDOWN);
		intentFilter.addAction(Intent.ACTION_REBOOT);
		intentFilter.addAction(CONSTS.ACTION_HTC_QCK_POFF);
		intentFilter.addAction(CONSTS.ACTION_ANDR_QCK_POFF);

		// SMS and phone calls
		intentFilter.addAction(CONSTS.ACTION_SMS_RECEIVED_ALL_API);
		intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
		intentFilter.addAction(CONSTS.ACTION_PRECISE_CALL_STATE_CHANGED);

		// Battery
		intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
		intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
		intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);

		// Bluetooth
		intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
		intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

		UtilsGeneral.getContext().registerReceiver(mainRegBroadcastReceiver, intentFilter);
	}

	private final BroadcastReceiver mainRegBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			UtilsIntentWhatToDo.intentWhatToDo(intent);
		}
	};
}
