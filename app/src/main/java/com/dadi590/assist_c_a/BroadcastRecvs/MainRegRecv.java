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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.telephony.TelephonyManager;

import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.GlobalUtils.UtilsApp;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.Modules.BatteryProcessor.UtilsBatteryProcessorBC;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.Modules.Speech.UtilsSpeech2BC;
import com.dadi590.assist_c_a.Modules.Telephony.PhoneCallsProcessor.UtilsPhoneCallsProcessorBC;
import com.dadi590.assist_c_a.Modules.Telephony.SmsMsgsProcessor.SmsMsgsProcessor;

import java.util.LinkedHashMap;

/**
 * <p>The class to be used to decide what to do with any broadcasts that need registering on the main app process.</p>
 * <p>This is used even with broadcasts sent to static receivers, because the assistant (at least currently) won't
 * process broadcasts sent when it's not running (as a start because it's supposed to be always running). So static
 * receivers are to wake the assistant in case something shut it down (like the system, for memory optimization, or some
 * error), and registered receivers handle the broadcasts normally.</p>
 */
public final class MainRegRecv {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private MainRegRecv() {
	}

	/**
	 * <p>Registers all the receivers in the class.</p>
	 *
	 */
	public static void registerReceivers() {
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

		// Battery / Power
		intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
		intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
		intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);

		try {
			UtilsGeneral.getContext().registerReceiver(mainRegBroadcastReceiver, intentFilter);
		} catch (final IllegalArgumentException ignored) {
			// Then it's already registered - imagine this is called again because the speech module was restarted.
		}
	}

	private static final BroadcastReceiver mainRegBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(@Nullable final Context context, @Nullable final Intent intent) {
			System.out.println("PPPPPPPPPPPPPPPPPP-MainRegRcv - " + (null != intent ? intent.getAction() : null));

			if (intent == null || intent.getAction() == null) {
				return;
			}

			switch (intent.getAction()) {
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////

				/////////////////////////////////////
				// Shut down and reboot
				case (CONSTS.ACTION_HTC_QCK_POFF):
				case (CONSTS.ACTION_ANDR_QCK_POFF):
				case (Intent.ACTION_SHUTDOWN): {
					UtilsApp.prepareShutdown();

					if (intent.getBooleanExtra(Intent.EXTRA_SHUTDOWN_USERSPACE_ONLY, false)) {
						final String speak = "Fast shut down detected.";
						UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, null);
					} else {
						final String speak = "Shut down detected.";
						UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, null);
					}
					// Note: must be very small speeches, since the phone will shut down fast.

					break;
				}
				case (Intent.ACTION_REBOOT): {
					UtilsApp.prepareShutdown();

					// No idea if this is supposed detected at all (might be stopped before it gets here by the system as
					// soon as it detects it or something).
					final String speak = "Reboot detected.";
					UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, null);
					// Note: must be a very small speech, since the phone will shut down fast.


					break;
				}

				/////////////////////////////////////
				// SMS and phone calls
				case (CONSTS.ACTION_SMS_RECEIVED_ALL_API): {
					SmsMsgsProcessor.smsMsgsProcessor(intent);

					break;
				}
				case (TelephonyManager.ACTION_PHONE_STATE_CHANGED): {
					if (intent.hasExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)) {
						// According to Android Developers website, having both READ_CALL_LOG and READ_PHONE_STATE
						// permissions (which I must, to receive the EXTRA_INCOMING_NUMBER), will make the app receive 2
						// broadcasts. One with and one without the EXTRA_INCOMING_NUMBER (the one without comes from the
						// READ_PHONE_STATE permission.
						// This below is to link the EXTRA_STATEs to the CALL_STATEs.
						final LinkedHashMap<String, Integer> map_EXTRA_STATE_CALL_STATE = new LinkedHashMap<>(3);
						map_EXTRA_STATE_CALL_STATE.put(TelephonyManager.EXTRA_STATE_RINGING, TelephonyManager.CALL_STATE_RINGING);
						map_EXTRA_STATE_CALL_STATE.put(TelephonyManager.EXTRA_STATE_IDLE, TelephonyManager.CALL_STATE_IDLE);
						map_EXTRA_STATE_CALL_STATE.put(TelephonyManager.EXTRA_STATE_OFFHOOK, TelephonyManager.CALL_STATE_OFFHOOK);

						final String state = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
						final String phone_number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
						final Integer call_state = map_EXTRA_STATE_CALL_STATE.get(state);
						if (call_state != null) {
							UtilsPhoneCallsProcessorBC.receiveCall(call_state, phone_number, false);
						}
						// Not sure what to do with the possible NPE of map_EXTRA_STATE_CALL_STATE. Shouldn't happen, I
						// guess, unless the call states are updated to include a new one or something.
					}

					break;
				}
				case (CONSTS.ACTION_PRECISE_CALL_STATE_CHANGED): {
					// todo See here for more: https://stackoverflow.com/questions/32821952/how-to-use-precisecallstate
					//MainSrv.getPhoneCallProcessor().phoneNumRecv(context, map_EXTRA_STATE_CALL_STATE.get(state), phoneNumber,
					//		true);

					// intent.getIntExtra(TelephonyManager.EXTRA_FOREGROUND_CALL_STATE, -2) --> why EXTRA_FOREGROUND_CALL_STATE?
					// Why not the BACKGROUND one, or the other one, which I don't remember?

					break;
				}

				/////////////////////////////////////
				// Battery / Power
				case (Intent.ACTION_BATTERY_CHANGED): {
					final int battery_status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
					final int battery_lvl = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
					final int battery_lvl_scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
					UtilsBatteryProcessorBC.processBatteryLvlChg(battery_status, battery_lvl, battery_lvl_scale);

					break;
				}
				case (Intent.ACTION_POWER_CONNECTED): {
					UtilsBatteryProcessorBC.processBatteryPwrChg(true);

					break;
				}
				case (Intent.ACTION_POWER_DISCONNECTED): {
					UtilsBatteryProcessorBC.processBatteryPwrChg(false);

					break;
				}
			}

			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
		}
	};
}
